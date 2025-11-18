package com.krishna.varunaapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.krishna.varunaapp.R
import com.krishna.varunaapp.activities.VillageActivity

class VillageListAdapter(
    private val list: List<String>,
    private val context: Context
) : RecyclerView.Adapter<VillageListAdapter.VillageHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VillageHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_village, parent, false)
        return VillageHolder(view)
    }

    override fun onBindViewHolder(holder: VillageHolder, position: Int) {
        val name = list[position]
        holder.title.text = name

        holder.itemView.setOnClickListener {
            val i = Intent(context, VillageActivity::class.java)
            i.putExtra("villageName", name)
            context.startActivity(i)
        }
    }

    override fun getItemCount(): Int = list.size

    class VillageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvVillageName)
    }
}
