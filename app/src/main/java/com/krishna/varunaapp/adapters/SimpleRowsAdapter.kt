package com.krishna.varunaapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.krishna.varunaapp.databinding.ItemParameterDisplayRowBinding
import com.krishna.varunaapp.models.WaterParameter

class SimpleRowsAdapter(
    private val rows: List<WaterParameter>
) : RecyclerView.Adapter<SimpleRowsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemParameterDisplayRowBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemParameterDisplayRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = rows.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = rows[position]
        holder.binding.tvParam.text = item.parameter
        holder.binding.tvS1.text = item.sample1
        holder.binding.tvS2.text = item.sample2
        holder.binding.tvEpa.text = item.epaStandard
    }
}
