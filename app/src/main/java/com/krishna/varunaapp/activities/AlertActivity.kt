package com.krishna.varunaapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.krishna.varunaapp.adapters.AlertAdapter
import com.krishna.varunaapp.databinding.ActivityAlertBinding
import com.krishna.varunaapp.models.Alert

class AlertActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlertBinding
    private lateinit var db: FirebaseFirestore
    private val alertList = mutableListOf<Alert>()
    private lateinit var adapter: AlertAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        setSupportActionBar(binding.toolbarAlerts)
        binding.toolbarAlerts.setNavigationOnClickListener { finish() }

        setupRecycler()
        listenForAlerts()  // auto updates
    }

    private fun setupRecycler() {
        adapter = AlertAdapter(alertList)
        binding.recyclerAlerts.layoutManager = LinearLayoutManager(this)
        binding.recyclerAlerts.adapter = adapter
    }

    private fun listenForAlerts() {
        db.collection("alerts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->

                if (error != null || snapshots == null) return@addSnapshotListener

                alertList.clear()

                for (doc in snapshots.documents) {
                    val alert = doc.toObject(Alert::class.java)
                    if (alert != null) alertList.add(alert)
                }

                adapter.notifyDataSetChanged()
            }
    }
}


//package com.krishna.varunaapp.activities
//
//import android.os.Bundle
//import android.view.View
//import android.widget.EditText
//import android.widget.Toast
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.Query
//import com.krishna.varunaapp.adapters.AlertAdapter
//import com.krishna.varunaapp.databinding.ActivityAlertBinding
//import com.krishna.varunaapp.models.Alert
//
//class AlertActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityAlertBinding
//    private lateinit var db: FirebaseFirestore
//    private lateinit var auth: FirebaseAuth
//    private lateinit var adapter: AlertAdapter
//
//    private val alerts = mutableListOf<Alert>()
//    private var userRole = "GeneralUser"
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityAlertBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        db = FirebaseFirestore.getInstance()
//        auth = FirebaseAuth.getInstance()
//
//        setSupportActionBar(binding.toolbarAlerts)
//        binding.toolbarAlerts.setNavigationOnClickListener { finish() }
//
//        setupRecycler()
//        loadUserRole()
//        listenForAlerts()
//
//        binding.btnAddAlert.setOnClickListener { openAlertDialog() }
//    }
//
//    private fun setupRecycler() {
//        adapter = AlertAdapter(alerts)
//        binding.recyclerAlerts.layoutManager = LinearLayoutManager(this)
//        binding.recyclerAlerts.adapter = adapter
//    }
//
//    private fun loadUserRole() {
//        val uid = auth.currentUser?.uid ?: return
//        db.collection("users").document(uid).get()
//            .addOnSuccessListener {
//                userRole = it.getString("role") ?: "GeneralUser"
//                if (userRole == "GeneralUser") binding.btnAddAlert.visibility = View.GONE
//            }
//    }
//
//    private fun listenForAlerts() {
//        db.collection("alerts")
//            .orderBy("createdAt", Query.Direction.DESCENDING)
//            .addSnapshotListener { snap, err ->
//                if (err != null) return@addSnapshotListener
//
//                alerts.clear()
//                for (doc in snap!!.documents) {
//                    val alert = doc.toObject(Alert::class.java)
//                    if (alert != null) alerts.add(alert)
//                }
//                adapter.notifyDataSetChanged()
//
//                binding.tvNoAlerts.visibility = if (alerts.isEmpty()) View.VISIBLE else View.GONE
//            }
//    }
//
//    private fun openAlertDialog() {
//        val dialogView = layoutInflater.inflate(android.R.layout.simple_list_item_1, null)
//        val et = EditText(this)
//        et.hint = "Write alert message"
//
//        AlertDialog.Builder(this)
//            .setTitle("Create Alert")
//            .setView(et)
//            .setPositiveButton("Save") { _, _ ->
//                val message = et.text.toString().trim()
//                if (message.isEmpty()) {
//                    Toast.makeText(this, "Enter message", Toast.LENGTH_SHORT).show()
//                } else {
//                    saveAlert(message)
//                }
//            }
//            .setNegativeButton("Cancel", null)
//            .show()
//    }
//
//    private fun saveAlert(message: String) {
//        val id = db.collection("alerts").document().id
//        val alert = Alert(
//            id = id,
//            title = "New Health Update",
//            message = message,
//            village = "All",
//            type = "General",
//            severity = "Medium",
//            createdAt = System.currentTimeMillis(),
//            createdBy = auth.currentUser?.uid ?: ""
//        )
//
//        db.collection("alerts").document(id)
//            .set(alert)
//            .addOnSuccessListener {
//                Toast.makeText(this, "Alert added", Toast.LENGTH_SHORT).show()
//            }
//    }
//}
