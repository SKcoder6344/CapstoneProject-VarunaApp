package com.krishna.varunaapp.activities

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.krishna.varunaapp.R
import com.krishna.varunaapp.adapters.VillageListAdapter
import com.krishna.varunaapp.databinding.ActivityVillageListBinding

class VillageListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVillageListBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val villages = mutableListOf<String>()
    private lateinit var adapter: VillageListAdapter
    private var userRole = "GeneralUser"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVillageListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setSupportActionBar(binding.toolbarVillageList)
        supportActionBar?.title = "Villages"
        binding.toolbarVillageList.setNavigationOnClickListener { finish() }

        adapter = VillageListAdapter(villages, this)
        binding.recyclerVillage.adapter = adapter
        binding.recyclerVillage.layoutManager = LinearLayoutManager(this)

        binding.btnAddVillage.setOnClickListener { openAddVillageDialog() }

        loadUserRole()
        loadVillages()
    }

    private fun loadUserRole() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                userRole = doc.getString("role") ?: "GeneralUser"

                // Hide Add Village button for regular users
                if (userRole == "GeneralUser") {
                    binding.btnAddVillage.visibility = View.GONE
                }
            }
    }

    private fun openAddVillageDialog() {
        val et = EditText(this)
        et.hint = "Enter village name"
        et.setPadding(50, 30, 50, 30)

        AlertDialog.Builder(this)
            .setTitle("Add New Village")
            .setMessage("Enter the village name to add")
            .setView(et)
            .setPositiveButton("Save") { _, _ ->
                val name = et.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(this, "Please enter a village name", Toast.LENGTH_SHORT).show()
                } else {
                    saveVillage(name)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveVillage(name: String) {
        // Check if village already exists
        db.collection("villages").document(name).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    Toast.makeText(this, "Village '$name' already exists", Toast.LENGTH_SHORT).show()
                } else {
                    // Create new village document
                    db.collection("villages").document(name)
                        .set(mapOf(
                            "name" to name,
                            "createdAt" to System.currentTimeMillis(),
                            "createdBy" to auth.currentUser?.uid
                        ))
                        .addOnSuccessListener {
                            Toast.makeText(this, "Village '$name' added successfully", Toast.LENGTH_SHORT).show()
                            loadVillages()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }

    private fun loadVillages() {
        binding.progressBar.visibility = View.VISIBLE

        db.collection("villages")
            .orderBy("name")
            .get()
            .addOnSuccessListener { snap ->
                villages.clear()
                for (doc in snap) {
                    val name = doc.getString("name") ?: continue
                    villages.add(name)
                }
                adapter.notifyDataSetChanged()

                binding.progressBar.visibility = View.GONE

                if (villages.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Error loading villages", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        loadVillages() // Refresh list when returning from VillageActivity
    }
}

//package com.krishna.varunaapp.activities
//
//import android.os.Bundle
//import android.widget.EditText
//import android.widget.Toast
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.google.firebase.firestore.FirebaseFirestore
//import com.krishna.varunaapp.adapters.VillageListAdapter
//import com.krishna.varunaapp.databinding.ActivityVillageListBinding
//
//class VillageListActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityVillageListBinding
//    private lateinit var db: FirebaseFirestore
//    private val villages = mutableListOf<String>()
//    private lateinit var adapter: VillageListAdapter
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityVillageListBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        db = FirebaseFirestore.getInstance()
//
//        adapter = VillageListAdapter(villages, this)
//        binding.recyclerVillage.adapter = adapter
//        binding.recyclerVillage.layoutManager = LinearLayoutManager(this)
//
//        binding.btnAddVillage.setOnClickListener { openAddVillageDialog() }
//
//        loadVillages()
//    }
//
//    private fun openAddVillageDialog() {
//        val et = EditText(this)
//        AlertDialog.Builder(this)
//            .setTitle("Add Village")
//            .setView(et)
//            .setPositiveButton("Save") { _, _ ->
//                val name = et.text.toString().trim()
//                if (name.isNotEmpty()) saveVillage(name)
//            }
//            .setNegativeButton("Cancel", null)
//            .show()
//    }
//
//    private fun saveVillage(name: String) {
//        db.collection("villages").document(name).set(mapOf("name" to name))
//            .addOnSuccessListener {
//                Toast.makeText(this, "Village added", Toast.LENGTH_SHORT).show()
//                loadVillages()
//            }
//    }
//
//    private fun loadVillages() {
//        db.collection("villages")
//            .orderBy("name")
//            .get()
//            .addOnSuccessListener { snap ->
//                villages.clear()
//                for (doc in snap) {
//                    val name = doc.getString("name") ?: continue
//                    villages.add(name)
//                }
//                adapter.notifyDataSetChanged()
//            }
//    }
//}
