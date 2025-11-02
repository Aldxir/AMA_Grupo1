package com.example.alden.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.alden.models.RegistroAcceso
import java.time.format.DateTimeFormatter
import com.example.alden.R

class RegistroAdapter : RecyclerView.Adapter<RegistroAdapter.VH>() {
    private val data = mutableListOf<RegistroAcceso>()
    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun submit(list: List<RegistroAcceso>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_registro, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = data[position]
        holder.tv.text = "${r.hora.format(fmt)} • ${r.usuario.nombre} • ${r.accion} • ${r.ubicacion}"
    }

    override fun getItemCount() = data.size

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tv: TextView = v.findViewById(R.id.tvLinea)
    }
}