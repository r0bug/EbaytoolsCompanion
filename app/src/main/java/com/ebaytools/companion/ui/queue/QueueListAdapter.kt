package com.ebaytools.companion.ui.queue

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ebaytools.companion.R
import com.ebaytools.companion.data.models.QueueWithStats
import com.ebaytools.companion.databinding.ItemQueueBinding
import java.text.SimpleDateFormat
import java.util.Locale

class QueueListAdapter(
    private val onQueueClick: (QueueWithStats) -> Unit
) : ListAdapter<QueueWithStats, QueueListAdapter.ViewHolder>(QueueDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemQueueBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onQueueClick)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class ViewHolder(
        private val binding: ItemQueueBinding,
        private val onQueueClick: (QueueWithStats) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        
        fun bind(queueWithStats: QueueWithStats) {
            val queue = queueWithStats.queue
            
            binding.textViewQueueName.text = queue.name
            binding.textViewItemCount.text = "${queueWithStats.itemCount} items"
            binding.textViewImageCount.text = "${queueWithStats.imageCount} images"
            binding.textViewCreatedDate.text = "Created: ${dateFormat.format(queue.createdAt)}"
            
            if (queue.isSynced) {
                binding.imageViewSyncStatus.setImageResource(R.drawable.ic_cloud_done)
                binding.textViewSyncStatus.text = "Synced"
            } else {
                binding.imageViewSyncStatus.setImageResource(R.drawable.ic_cloud_off)
                binding.textViewSyncStatus.text = "Not synced"
            }
            
            binding.root.setOnClickListener {
                onQueueClick(queueWithStats)
            }
        }
    }
    
    class QueueDiffCallback : DiffUtil.ItemCallback<QueueWithStats>() {
        override fun areItemsTheSame(oldItem: QueueWithStats, newItem: QueueWithStats): Boolean {
            return oldItem.queue.id == newItem.queue.id
        }
        
        override fun areContentsTheSame(oldItem: QueueWithStats, newItem: QueueWithStats): Boolean {
            return oldItem == newItem
        }
    }
}