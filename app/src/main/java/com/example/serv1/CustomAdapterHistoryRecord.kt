package com.example.serv1

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CustomAdapterHistoryRecord(private val context: Context, val mList: ArrayList<HistoryRecord>) : RecyclerView.Adapter<CustomAdapterHistoryRecord.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomAdapterHistoryRecord.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_record_card, parent, false)

        return ViewHolder(view)
    }


    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val record = mList[position]

        holder.idView.text = (position+1).toString()
        holder.timeView.text = record.currentTime
        holder.diseaseView.text = record.disease
        holder.accuracyView.text = record.accuracy
    }



    override fun getItemCount(): Int {
        return mList.size
    }


    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val idView: TextView = itemView.findViewById(R.id.id)
        val timeView: TextView = itemView.findViewById(R.id.time)
        val diseaseView: TextView = itemView.findViewById(R.id.disease)
        val accuracyView: TextView = itemView.findViewById(R.id.accuracy)
    }
}
