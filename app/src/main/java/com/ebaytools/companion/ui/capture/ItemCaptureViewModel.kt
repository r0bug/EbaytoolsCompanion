package com.ebaytools.companion.ui.capture

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ebaytools.companion.data.AppDatabase
import com.ebaytools.companion.data.models.Item
import com.ebaytools.companion.data.models.ItemImage
import kotlinx.coroutines.launch
import java.util.Date

class ItemCaptureViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val itemDao = database.itemDao()
    private val itemImageDao = database.itemImageDao()
    private val queueDao = database.queueDao()
    
    private var queueId: Long = 0
    
    private val _currentItemId = MutableLiveData<Long?>()
    val currentItemId: LiveData<Long?> = _currentItemId
    
    private val _currentItemName = MutableLiveData<String>()
    val currentItemName: LiveData<String> = _currentItemName
    
    private val _currentItemImages = MutableLiveData<List<ItemImage>>(emptyList())
    val currentItemImages: LiveData<List<ItemImage>> = _currentItemImages
    
    private val _totalItemCount = MutableLiveData<Int>(0)
    val totalItemCount: LiveData<Int> = _totalItemCount
    
    fun setQueueId(id: Long) {
        queueId = id
        loadItemCount()
    }
    
    fun setCurrentItemName(name: String) {
        _currentItemName.value = name
        viewModelScope.launch {
            // Create new item
            val item = Item(
                queueId = queueId,
                name = name,
                createdAt = Date(),
                updatedAt = Date()
            )
            val itemId = itemDao.insertItem(item)
            _currentItemId.value = itemId
            _currentItemImages.value = emptyList()
            loadItemCount()
            updateQueueTimestamp()
        }
    }
    
    suspend fun addImageToCurrentItem(imagePath: String) {
        val itemId = _currentItemId.value ?: return
        
        val currentImages = _currentItemImages.value ?: emptyList()
        val newImage = ItemImage(
            itemId = itemId,
            imagePath = imagePath,
            orderIndex = currentImages.size,
            createdAt = Date()
        )
        
        itemImageDao.insertImage(newImage)
        _currentItemImages.value = currentImages + newImage
        updateQueueTimestamp()
    }
    
    private fun loadItemCount() {
        viewModelScope.launch {
            _totalItemCount.value = itemDao.getItemCountForQueue(queueId)
        }
    }
    
    private suspend fun updateQueueTimestamp() {
        val queue = queueDao.getQueueById(queueId)
        queue?.let {
            queueDao.updateQueue(it.copy(updatedAt = Date(), isSynced = false))
        }
    }
}