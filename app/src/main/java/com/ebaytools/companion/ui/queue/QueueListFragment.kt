package com.ebaytools.companion.ui.queue

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ebaytools.companion.databinding.FragmentQueueListBinding

class QueueListFragment : Fragment() {
    
    private var _binding: FragmentQueueListBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: QueueViewModel by viewModels()
    private lateinit var adapter: QueueListAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQueueListBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeQueues()
    }
    
    private fun setupRecyclerView() {
        adapter = QueueListAdapter { queueWithStats ->
            findNavController().navigate(
                QueueListFragmentDirections.actionQueueListToItemCapture(queueWithStats.queue.id)
            )
        }
        
        binding.recyclerViewQueues.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@QueueListFragment.adapter
        }
    }
    
    private fun observeQueues() {
        viewModel.allQueuesWithStats.observe(viewLifecycleOwner) { queues ->
            adapter.submitList(queues)
            
            if (queues.isEmpty()) {
                binding.textViewEmpty.visibility = View.VISIBLE
                binding.recyclerViewQueues.visibility = View.GONE
            } else {
                binding.textViewEmpty.visibility = View.GONE
                binding.recyclerViewQueues.visibility = View.VISIBLE
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}