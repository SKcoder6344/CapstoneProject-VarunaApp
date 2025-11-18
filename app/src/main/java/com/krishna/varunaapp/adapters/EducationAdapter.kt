package com.krishna.varunaapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.krishna.varunaapp.databinding.ItemEducationBinding
import com.krishna.varunaapp.models.EducationMaterial

class EducationAdapter(
    private val list: List<EducationMaterial>,
    private val onClick: (EducationMaterial) -> Unit
) : RecyclerView.Adapter<EducationAdapter.EducationViewHolder>() {

    inner class EducationViewHolder(val binding: ItemEducationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: EducationMaterial) {
            binding.tvTitle.text = item.title
            binding.tvDescription.text = item.description
            binding.tvType.text = item.fileType.uppercase()

            binding.cardMaterial.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EducationViewHolder {
        val binding = ItemEducationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EducationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EducationViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size
}
