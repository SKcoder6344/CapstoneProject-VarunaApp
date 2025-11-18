package com.krishna.varunaapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.krishna.varunaapp.databinding.ItemAlertBinding
import com.krishna.varunaapp.models.Alert
import java.text.SimpleDateFormat
import java.util.*

class AlertAdapter(private val list: List<Alert>) :
    RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    inner class AlertViewHolder(val binding: ItemAlertBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(a: Alert) {

            binding.tvTitle.text = a.title
            binding.tvMessage.text = a.message
            binding.tvType.text = "Type: ${a.type}"
            binding.tvSeverity.text = "Severity: ${a.severity}"

            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            binding.tvDate.text = sdf.format(Date(a.createdAt))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val binding = ItemAlertBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size
}
