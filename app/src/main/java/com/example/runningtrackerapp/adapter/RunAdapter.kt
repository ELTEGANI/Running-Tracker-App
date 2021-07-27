package com.example.runningtrackerapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.runningtrackerapp.databinding.ItemRunBinding
import com.example.runningtrackerapp.db.Run

class RunAdapter :  ListAdapter<Run, RunAdapter.ViewHolder>(
    ItemDiffCallback()
){
    override fun onCreateViewHolder(parent: ViewGroup, viewType:Int): ViewHolder {
        return ViewHolder.from(parent)
    }


    override fun onBindViewHolder(holder: ViewHolder, position:Int){
        holder.bind(getItem(position))
    }


    class ViewHolder private constructor(private var itemsRunBinding: ItemRunBinding):
        RecyclerView.ViewHolder(itemsRunBinding.root) {
        fun bind(
            run: Run
        ) {
            itemsRunBinding.items = run
            itemsRunBinding.executePendingBindings()
        }


        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemRunBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

    }
}


class ItemDiffCallback: DiffUtil.ItemCallback<Run>(){
    override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
        return oldItem.id == newItem.id
    }
    override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
        return oldItem == newItem
    }
}