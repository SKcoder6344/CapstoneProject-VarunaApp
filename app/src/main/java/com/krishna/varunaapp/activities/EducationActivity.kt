package com.krishna.varunaapp.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.krishna.varunaapp.adapters.EducationAdapter
import com.krishna.varunaapp.databinding.ActivityEducationBinding
import com.krishna.varunaapp.models.EducationMaterial

class EducationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEducationBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private val materials = mutableListOf<EducationMaterial>()
    private lateinit var adapter: EducationAdapter

    private var userRole = "GeneralUser"

    private val FILE_PICKER = 2001
    private var selectedFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEducationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        setSupportActionBar(binding.toolbarEducation)
        binding.toolbarEducation.setNavigationOnClickListener { finish() }

        setupRecycler()
        loadUserRole()
        loadMaterials()

        binding.btnSelectFile.setOnClickListener { openFilePicker() }
        binding.btnUpload.setOnClickListener { uploadMaterial() }
    }

    private fun openMaterial(item: EducationMaterial) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(
            Uri.parse(item.fileUrl),
            if (item.fileType == "pdf") "application/pdf" else "image/*"
        )
        startActivity(intent)
    }

    private fun setupRecycler() {
        adapter = EducationAdapter(materials) { material ->
            openMaterial(material)
        }
        binding.recyclerMaterials.layoutManager = LinearLayoutManager(this)
        binding.recyclerMaterials.adapter = adapter
    }



    private fun loadUserRole() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener {
                userRole = it.getString("role") ?: "GeneralUser"

                if (userRole == "GeneralUser") {
                    binding.layoutUploadSection.visibility = View.GONE
                }
            }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf", "image/*"))
        startActivityForResult(intent, FILE_PICKER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_PICKER && resultCode == Activity.RESULT_OK) {
            selectedFileUri = data?.data
            binding.tvSelectedFileName.text = selectedFileUri?.lastPathSegment ?: "File Selected"
        }
    }

    private fun uploadMaterial() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (title.isEmpty() || selectedFileUri == null) {
            Toast.makeText(this, "Title and file required", Toast.LENGTH_SHORT).show()
            return
        }

        val fileUri = selectedFileUri!!
        val fileType = if (fileUri.toString().contains("pdf")) "pdf" else "image"

        val storageRef = storage.reference.child("education/${System.currentTimeMillis()}.$fileType")

        binding.progressUpload.visibility = View.VISIBLE

        storageRef.putFile(fileUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { url ->
                    saveMaterialToFirestore(title, description, url.toString(), fileType)
                }
            }
            .addOnFailureListener {
                binding.progressUpload.visibility = View.GONE
                Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveMaterialToFirestore(
        title: String,
        desc: String,
        fileUrl: String,
        fileType: String
    ) {
        val id = db.collection("education_materials").document().id

        val material = EducationMaterial(
            id = id,
            title = title,
            description = desc,
            fileUrl = fileUrl,
            fileType = fileType,
            uploadedAt = System.currentTimeMillis(),
            uploadedBy = auth.currentUser?.uid ?: ""
        )

        db.collection("education_materials").document(id)
            .set(material)
            .addOnSuccessListener {
                binding.progressUpload.visibility = View.GONE
                Toast.makeText(this, "Uploaded Successfully", Toast.LENGTH_SHORT).show()
                clearUploadFields()
                loadMaterials()
            }
            .addOnFailureListener {
                binding.progressUpload.visibility = View.GONE
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadMaterials() {
        db.collection("education_materials")
            .orderBy("uploadedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, error ->
                if (snap == null || error != null) return@addSnapshotListener

                materials.clear()
                for (doc in snap.documents) {
                    val m = doc.toObject(EducationMaterial::class.java)
                    if (m != null) materials.add(m)
                }

                adapter.notifyDataSetChanged()
            }
    }

    private fun clearUploadFields() {
        binding.etTitle.text?.clear()
        binding.etDescription.text?.clear()
        binding.tvSelectedFileName.text = "No file selected"
        selectedFileUri = null
    }
}
