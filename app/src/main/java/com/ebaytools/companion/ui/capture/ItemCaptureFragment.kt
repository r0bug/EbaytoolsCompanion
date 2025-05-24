package com.ebaytools.companion.ui.capture

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ebaytools.companion.R
import com.ebaytools.companion.databinding.FragmentItemCaptureBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ItemCaptureFragment : Fragment() {
    
    private var _binding: FragmentItemCaptureBinding? = null
    private val binding get() = _binding!!
    
    private val args: ItemCaptureFragmentArgs by navArgs()
    private val viewModel: ItemCaptureViewModel by viewModels()
    
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    
    private val speechRecognizerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val results = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            results?.firstOrNull()?.let { itemName ->
                viewModel.setCurrentItemName(itemName)
                showCameraView()
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemCaptureBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        outputDirectory = getOutputDirectory()
        viewModel.setQueueId(args.queueId)
        
        setupUI()
        observeViewModel()
        
        // Start with item name input
        if (viewModel.currentItemId.value == null) {
            showItemNameInput()
        } else {
            showCameraView()
        }
    }
    
    private fun setupUI() {
        // New Item Button
        binding.buttonNewItem.setOnClickListener {
            showItemNameInput()
        }
        
        // Take Photo Button
        binding.buttonCapture.setOnClickListener {
            takePhoto()
        }
        
        // Done Button
        binding.buttonDone.setOnClickListener {
            findNavController().navigateUp()
        }
        
        // Voice Input Button in item name dialog
        binding.buttonVoiceInput.setOnClickListener {
            if (checkRecordAudioPermission()) {
                startVoiceRecognition()
            }
        }
        
        // Text Submit Button
        binding.buttonSubmitText.setOnClickListener {
            val itemName = binding.editTextItemName.text?.toString()?.trim()
            if (!itemName.isNullOrEmpty()) {
                viewModel.setCurrentItemName(itemName)
                showCameraView()
            } else {
                Toast.makeText(context, "Please enter an item name", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun observeViewModel() {
        viewModel.currentItemName.observe(viewLifecycleOwner) { name ->
            binding.textViewCurrentItem.text = "Current Item: $name"
        }
        
        viewModel.currentItemImages.observe(viewLifecycleOwner) { images ->
            binding.textViewImageCount.text = "${images.size} photos"
        }
        
        viewModel.totalItemCount.observe(viewLifecycleOwner) { count ->
            binding.textViewItemCount.text = "$count items in queue"
        }
    }
    
    private fun showItemNameInput() {
        binding.layoutItemNameInput.visibility = View.VISIBLE
        binding.layoutCamera.visibility = View.GONE
        binding.editTextItemName.setText("")
        binding.editTextItemName.requestFocus()
    }
    
    private fun showCameraView() {
        binding.layoutItemNameInput.visibility = View.GONE
        binding.layoutCamera.visibility = View.VISIBLE
        
        if (checkCameraPermission()) {
            startCamera()
        }
    }
    
    private fun checkCameraPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                PERMISSION_REQUEST_CAMERA
            )
            false
        } else {
            true
        }
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
    
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }
            
            imageCapture = ImageCapture.Builder()
                .build()
            
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to start camera", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }
    
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )
        
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    lifecycleScope.launch {
                        viewModel.addImageToCurrentItem(photoFile.absolutePath)
                    }
                    Toast.makeText(context, "Photo saved", Toast.LENGTH_SHORT).show()
                }
                
                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(context, "Failed to save photo", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    
    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say the item name or description")
        }
        
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Speech recognition not available", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun getOutputDirectory(): File {
        val mediaDir = requireContext().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else requireContext().filesDir
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera()
                }
            }
            PERMISSION_REQUEST_RECORD_AUDIO -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startVoiceRecognition()
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PERMISSION_REQUEST_CAMERA = 100
        private const val PERMISSION_REQUEST_RECORD_AUDIO = 200
    }
}