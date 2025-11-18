package com.krishna.varunaapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.krishna.varunaapp.databinding.ItemCsvWaterBinding
import com.krishna.varunaapp.models.WaterParameter

class WaterTableRowsAdapter(private val list: List<WaterParameter>) :
    RecyclerView.Adapter<WaterTableRowsAdapter.VH>() {

    inner class VH(val binding: ItemCsvWaterBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemCsvWaterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        holder.binding.tvParameter.text = item.parameter
        holder.binding.tvSample1.text = "Sample1: ${item.sample1}"
        holder.binding.tvSample2.text = "Sample2: ${item.sample2}"
        holder.binding.tvEpa.text = "Standard: ${item.epaStandard}"
    }
}
