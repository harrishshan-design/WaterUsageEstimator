package com.example.waterusageestimator

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.waterusageestimator.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WaterUsageRecord(val date: Date, val usage: Double)

class HistoryAdapter(private val records: List<WaterUsageRecord>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(records[position])
    }

    override fun getItemCount() = records.size

    class HistoryViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(record: WaterUsageRecord) {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.tvHistoryDate.text = sdf.format(record.date)
            binding.tvHistoryUsage.text = "${record.usage.toInt()} L"
        }
    }
}