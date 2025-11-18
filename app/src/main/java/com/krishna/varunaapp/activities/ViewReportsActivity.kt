package com.krishna.varunaapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.krishna.varunaapp.adapters.ReportsAdapter
import com.krishna.varunaapp.databinding.ActivityViewReportsBinding

class ViewReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewReportsBinding
    private lateinit var db: FirebaseFirestore
    private var villageName = ""
    private val reports = mutableListOf<Map<String, Any>>()
    private lateinit var adapter: ReportsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        villageName = intent.getStringExtra("villageName") ?: ""

        adapter = ReportsAdapter(reports)
        binding.recyclerReports.layoutManager = LinearLayoutManager(this)
        binding.recyclerReports.adapter = adapter

        loadReports()
    }

    private fun loadReports() {
        db.collection("villages")
            .document(villageName)
            .collection("health_reports")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { snap ->
                reports.clear()
                for (doc in snap.documents) {
                    doc.data?.let { reports.add(it) }
                }
                adapter.notifyDataSetChanged()
            }
    }
}
