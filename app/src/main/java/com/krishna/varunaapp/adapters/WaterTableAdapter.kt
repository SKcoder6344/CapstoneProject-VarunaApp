package com.krishna.varunaapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.krishna.varunaapp.databinding.ItemWaterTableBinding
import com.krishna.varunaapp.databinding.ItemParameterDisplayRowBinding
import com.krishna.varunaapp.models.WaterTestTable
import java.text.SimpleDateFormat
import java.util.*

class WaterTableAdapter(private val tables: List<WaterTestTable>) :
    RecyclerView.Adapter<WaterTableAdapter.TableViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableViewHolder {
        val binding = ItemWaterTableBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TableViewHolder(binding)
    }

    override fun getItemCount() = tables.size

    override fun onBindViewHolder(holder: TableViewHolder, position: Int) {
        holder.bind(tables[position])
    }

    inner class TableViewHolder(private val binding: ItemWaterTableBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(table: WaterTestTable) {
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            binding.tvDate.text = sdf.format(Date(table.createdAt))

            binding.containerRowsDisplay.removeAllViews()

            table.rows.forEach { row ->
                val rowBinding = ItemParameterDisplayRowBinding.inflate(
                    LayoutInflater.from(binding.root.context),
                    binding.containerRowsDisplay,
                    false
                )

                rowBinding.tvParam.text = row.parameter
                rowBinding.tvS1.text = row.sample1
                rowBinding.tvS2.text = row.sample2
                rowBinding.tvEpa.text = row.epaStandard

                binding.containerRowsDisplay.addView(rowBinding.root)
            }
        }
    }
}
