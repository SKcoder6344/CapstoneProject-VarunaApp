package com.krishna.varunaapp.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.krishna.varunaapp.R
import com.krishna.varunaapp.models.HealthReport
import java.text.SimpleDateFormat
import java.util.*

class HealthReportAdapter(
    private val reports: List<HealthReport>,
    private val onStatusClick: (HealthReport) -> Unit
) : RecyclerView.Adapter<HealthReportAdapter.ReportViewHolder>() {

    inner class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvPatientName: TextView = itemView.findViewById(R.id.tvPatientName)
        private val tvAge: TextView = itemView.findViewById(R.id.tvAge)
        private val tvGender: TextView = itemView.findViewById(R.id.tvGender)
        private val tvSeverity: TextView = itemView.findViewById(R.id.tvSeverity)
        private val tvWaterSource: TextView = itemView.findViewById(R.id.tvWaterSource)
        private val tvSymptoms: TextView = itemView.findViewById(R.id.tvSymptoms)
        private val tvSymptomDate: TextView = itemView.findViewById(R.id.tvSymptomDate)
        private val tvCreatedDate: TextView = itemView.findViewById(R.id.tvCreatedDate)
        private val tvAdditionalNotes: TextView = itemView.findViewById(R.id.tvAdditionalNotes)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val cardStatus: MaterialCardView = itemView.findViewById(R.id.cardStatus)

        fun bind(report: HealthReport) {
            tvPatientName.text = report.patientName
            tvAge.text = "Age: ${report.age}"
            tvGender.text = "Gender: ${report.gender}"
            tvSeverity.text = "Severity: ${report.severity}"
            tvWaterSource.text = "Water Source: ${report.waterSource}"

            // Symptoms
            tvSymptoms.text = "Symptoms: ${report.symptoms.joinToString(", ")}"

            // Date
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            tvSymptomDate.text = "Started: ${sdf.format(Date(report.symptomStartDate))}"
            tvCreatedDate.text = "Reported: ${sdf.format(Date(report.createdAt))}"

            // Additional notes
            if (report.additionalNotes.isNotEmpty()) {
                tvAdditionalNotes.text = "Notes: ${report.additionalNotes}"
            } else {
                tvAdditionalNotes.text = "Notes: None"
            }

            // Status
            tvStatus.text = report.status
            when (report.status) {
                "Cured" -> {
                    tvStatus.setTextColor(Color.parseColor("#4CAF50"))
                    cardStatus.setCardBackgroundColor(Color.parseColor("#E8F5E9"))
                }
                "Not Cured" -> {
                    tvStatus.setTextColor(Color.parseColor("#F44336"))
                    cardStatus.setCardBackgroundColor(Color.parseColor("#FFEBEE"))
                }
            }

            // Severity color
            when (report.severity) {
                "Mild" -> tvSeverity.setTextColor(Color.parseColor("#FFC107"))
                "Moderate" -> tvSeverity.setTextColor(Color.parseColor("#FF9800"))
                "Severe" -> tvSeverity.setTextColor(Color.parseColor("#F44336"))
            }

            // Click to toggle status
            cardStatus.setOnClickListener {
                onStatusClick(report)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_health_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(reports[position])
    }

    override fun getItemCount() = reports.size
}