package com.example.tugasakhir.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.tugasakhir.R
import com.example.tugasakhir.data.Transportasi

class TransportasiAdapter(
    private val items: List<Transportasi>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<TransportasiAdapter.TransportasiViewHolder>() {

    inner class TransportasiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.imgTransportasi)
        val text: TextView = itemView.findViewById(R.id.tvNamaTransportasi)
        val root: View = itemView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransportasiViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transportasi, parent, false)
        return TransportasiViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransportasiViewHolder, position: Int) {
        val item = items[position]
        holder.img.setImageResource(item.imageResId)
        holder.text.text = item.nama

        // Highlight jika dipilih
        holder.root.setBackgroundResource(
            if (item.isSelected) R.drawable.bg_card_selected else R.drawable.bg_card_unselected
        )

        holder.root.setOnClickListener {
            onItemClick(position)
        }
    }

    override fun getItemCount(): Int = items.size
}
