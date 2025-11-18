package com.krishna.varunaapp.activities

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.krishna.varunaapp.R
import com.krishna.varunaapp.adapters.HealthReportAdapter
import com.krishna.varunaapp.models.HealthReport

class HealthReportsListActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    // Views
    private lateinit var toolbar: Toolbar
    private lateinit var tvVillageName: TextView
    private lateinit var tvTotalPatients: TextView
    private lateinit var tvCuredPatients: TextView
    private lateinit var tvActivePatients: TextView
    private lateinit var recyclerViewReports: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView

    private lateinit var adapter: HealthReportAdapter
    private val reports = mutableListOf<HealthReport>()

    private var villageName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_health_reports_list)

        db = FirebaseFirestore.getInstance()

        villageName = intent.getStringExtra("villageName") ?: ""

        initViews()
        setupToolbar()
        setupRecyclerView()
        loadReports()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbarReports)
        tvVillageName = findViewById(R.id.tvVillageName)
        tvTotalPatients = findViewById(R.id.tvTotalPatients)
        tvCuredPatients = findViewById(R.id.tvCuredPatients)
        tvActivePatients = findViewById(R.id.tvActivePatients)
        recyclerViewReports = findViewById(R.id.recyclerViewReports)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyState = findViewById(R.id.tvEmptyState)

        tvVillageName.text = "Health Reports - $villageName"
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = HealthReportAdapter(reports) { report ->
            updateReportStatus(report)
        }
        recyclerViewReports.layoutManager = LinearLayoutManager(this)
        recyclerViewReports.adapter = adapter
    }

    private fun loadReports() {
        progressBar.visibility = View.VISIBLE

        db.collection("villages")
            .document(villageName)
            .collection("health_reports")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                progressBar.visibility = View.GONE

                if (error != null) {
                    tvEmptyState.visibility = View.VISIBLE
                    tvEmptyState.text = "Error loading reports: ${error.message}"
                    return@addSnapshotListener
                }

                reports.clear()
                snapshots?.documents?.forEach { doc ->
                    val report = doc.toObject(HealthReport::class.java)
                    if (report != null) {
                        reports.add(report)
                    }
                }

                if (reports.isEmpty()) {
                    tvEmptyState.visibility = View.VISIBLE
                    recyclerViewReports.visibility = View.GONE
                } else {
                    tvEmptyState.visibility = View.GONE
                    recyclerViewReports.visibility = View.VISIBLE
                }

                updateStatistics()
                adapter.notifyDataSetChanged()
            }
    }

    private fun updateStatistics() {
        val totalPatients = reports.size
        val curedPatients = reports.count { it.status == "Cured" }
        val activePatients = reports.count { it.status == "Not Cured" }

        tvTotalPatients.text = "Total: $totalPatients"
        tvCuredPatients.text = "Cured: $curedPatients"
        tvActivePatients.text = "Active: $activePatients"
    }

    private fun updateReportStatus(report: HealthReport) {
        val newStatus = if (report.status == "Cured") "Not Cured" else "Cured"

        db.collection("villages")
            .document(villageName)
            .collection("health_reports")
            .document(report.id)
            .update("status", newStatus)
            .addOnSuccessListener {
                // Update is handled by snapshot listener
            }
    }
}