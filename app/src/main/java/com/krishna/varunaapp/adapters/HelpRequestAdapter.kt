package com.krishna.varunaapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.krishna.varunaapp.databinding.ItemHelpRequestBinding
import com.krishna.varunaapp.models.HelpRequest

class HelpRequestAdapter(
    private val list: List<HelpRequest>,
    private val onApprove: (HelpRequest) -> Unit,
    private val onDelete: (HelpRequest) -> Unit
) : RecyclerView.Adapter<HelpRequestAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemHelpRequestBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHelpRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.tvMessage.text = item.message
        holder.binding.tvStatus.text = "Status: ${item.status}"
        holder.binding.btnApprove.setOnClickListener { onApprove(item) }
        holder.binding.btnDelete.setOnClickListener { onDelete(item) }
    }
}
