package com.ebaytools.companion.ui.queue

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ebaytools.companion.data.AppDatabase
import com.ebaytools.companion.data.models.Queue
import com.ebaytools.companion.data.models.QueueWithStats
import kotlinx.coroutines.launch
import java.util.Date

class QueueViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val queueDao = database.queueDao()
    
    val allQueuesWithStats: LiveData<List<QueueWithStats>> = queueDao.getAllQueuesWithStats().asLiveData()
    
    suspend fun createQueue(name: String): Long {
        val queue = Queue(
            name = name,
            createdAt = Date(),
            updatedAt = Date()
        )
        return queueDao.insertQueue(queue)
    }
    
    fun updateQueue(queue: Queue) {
        viewModelScope.launch {
            queueDao.updateQueue(queue.copy(updatedAt = Date()))
        }
    }
    
    fun deleteQueue(queue: Queue) {
        viewModelScope.launch {
            queueDao.deleteQueue(queue)
        }
    }
    
    fun markQueueAsSynced(queueId: Long) {
        viewModelScope.launch {
            queueDao.updateSyncStatus(queueId, true, System.currentTimeMillis())
        }
    }
}