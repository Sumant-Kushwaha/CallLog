package com.amigo.calllog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CallLogAdapter(private val callLogs: List<CallLogItem>) :
    RecyclerView.Adapter<CallLogAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvName)
        val number: TextView = view.findViewById(R.id.tvNumber)
        val duration: TextView = view.findViewById(R.id.tvDuration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_call_log, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val callLog = callLogs[position]
        holder.name.text = callLog.name ?: "Unknown"
        holder.number.text = callLog.number
        holder.duration.text = "${callLog.duration} sec"
    }

    override fun getItemCount(): Int = callLogs.size
}