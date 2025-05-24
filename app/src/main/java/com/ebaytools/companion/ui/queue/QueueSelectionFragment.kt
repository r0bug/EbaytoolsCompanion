package com.ebaytools.companion.ui.queue

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ebaytools.companion.R
import com.ebaytools.companion.databinding.FragmentQueueSelectionBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.util.Locale

class QueueSelectionFragment : Fragment() {
    
    private var _binding: FragmentQueueSelectionBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: QueueViewModel by viewModels()
    
    private val speechRecognizerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val results = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            results?.firstOrNull()?.let { queueName ->
                createNewQueue(queueName)
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQueueSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }
    
    private fun setupUI() {
        binding.buttonNewQueue.setOnClickListener {
            showNewQueueDialog()
        }
        
        binding.buttonExistingQueue.setOnClickListener {
            findNavController().navigate(
                QueueSelectionFragmentDirections.actionQueueSelectionToQueueList()
            )
        }
    }
    
    private fun showNewQueueDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_new_queue, null)
        val editTextName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextQueueName)
        val buttonVoice = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonVoiceInput)
        
        buttonVoice.setOnClickListener {
            if (checkRecordAudioPermission()) {
                startVoiceRecognition()
            }
        }
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Create New Queue")
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val queueName = editTextName.text?.toString()?.trim()
                if (!queueName.isNullOrEmpty()) {
                    createNewQueue(queueName)
                } else {
                    Toast.makeText(context, "Please enter a queue name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun checkRecordAudioPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSION_REQUEST_RECORD_AUDIO
            )
            false
        } else {
            true
        }
    }
    
    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say the queue name")
        }
        
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Speech recognition not available", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun createNewQueue(name: String) {
        lifecycleScope.launch {
            val queueId = viewModel.createQueue(name)
            findNavController().navigate(
                QueueSelectionFragmentDirections.actionQueueSelectionToItemCapture(queueId)
            )
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecognition()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        private const val PERMISSION_REQUEST_RECORD_AUDIO = 200
    }
}