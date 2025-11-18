package com.krishna.varunaapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.krishna.varunaapp.R

class ReportsAdapter(
    private val list: List<Map<String, Any>>
) : RecyclerView.Adapter<ReportsAdapter.ReportViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val item = list[position]

        holder.name.text = item["name"].toString()
        holder.symptoms.text = "Symptoms: " + item["symptoms"].toString()
        holder.severity.text = "Severity: " + item["severity"].toString()
        holder.status.text = "Status: " + item["status"].toString()
    }

    override fun getItemCount(): Int = list.size

    class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvPatientName)
        val symptoms: TextView = itemView.findViewById(R.id.tvSymptoms)
        val severity: TextView = itemView.findViewById(R.id.tvSeverity)
        val status: TextView = itemView.findViewById(R.id.tvStatus)
    }
}
