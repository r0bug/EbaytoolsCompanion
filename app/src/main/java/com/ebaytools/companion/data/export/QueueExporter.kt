package com.ebaytools.companion.data.export

import android.content.Context
import com.ebaytools.companion.data.AppDatabase
import com.ebaytools.companion.data.models.Queue
import com.ebaytools.companion.data.models.Item
import com.ebaytools.companion.data.models.ItemImage
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

data class ExportQueue(
    val id: Long,
    val name: String,
    val createdAt: String,
    val updatedAt: String,
    val isSynced: Boolean,
    val items: List<ExportItem>
)

data class ExportItem(
    val id: Long,
    val name: String,
    val description: String?,
    val createdAt: String,
    val updatedAt: String,
    val images: List<ExportImage>
)

data class ExportImage(
    val id: Long,
    val imagePath: String,
    val orderIndex: Int
)

data class ExportData(
    val version: String = "2.0",
    val exportDate: String,
    val deviceInfo: Map<String, String>,
    val queues: List<ExportQueue>
)

class QueueExporter(private val context: Context) {
    
    private val database = AppDatabase.getDatabase(context)
    private val queueDao = database.queueDao()
    private val itemDao = database.itemDao()
    private val itemImageDao = database.itemImageDao()
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    
    suspend fun exportAllQueues(): File = withContext(Dispatchers.IO) {
        // Get all queues
        val queues = queueDao.getAllQueues().value ?: emptyList()
        
        // Build export data
        val exportQueues = queues.map { queue ->
            val items = itemDao.getItemsByQueueId(queue.id).value ?: emptyList()
            
            val exportItems = items.map { item ->
                val images = itemImageDao.getImagesByItemId(item.id).value ?: emptyList()
                
                ExportItem(
                    id = item.id,
                    name = item.name,
                    description = item.description,
                    createdAt = dateFormat.format(item.createdAt),
                    updatedAt = dateFormat.format(item.updatedAt),
                    images = images.map { image ->
                        ExportImage(
                            id = image.id,
                            imagePath = image.imagePath,
                            orderIndex = image.orderIndex
                        )
                    }
                )
            }
            
            ExportQueue(
                id = queue.id,
                name = queue.name,
                createdAt = dateFormat.format(queue.createdAt),
                updatedAt = dateFormat.format(queue.updatedAt),
                isSynced = queue.isSynced,
                items = exportItems
            )
        }
        
        // Create export data
        val exportData = ExportData(
            exportDate = dateFormat.format(Date()),
            deviceInfo = mapOf(
                "manufacturer" to android.os.Build.MANUFACTURER,
                "model" to android.os.Build.MODEL,
                "androidVersion" to android.os.Build.VERSION.RELEASE
            ),
            queues = exportQueues
        )
        
        // Create JSON
        val gson = GsonBuilder()
            .setPrettyPrinting()
            .create()
        
        val json = gson.toJson(exportData)
        
        // Save to file
        val exportDir = File(context.getExternalFilesDir(null), "exports")
        exportDir.mkdirs()
        
        val exportFile = File(exportDir, "ebaytools_export_${System.currentTimeMillis()}.json")
        FileWriter(exportFile).use { writer ->
            writer.write(json)
        }
        
        exportFile
    }
    
    suspend fun exportQueue(queueId: Long): File = withContext(Dispatchers.IO) {
        // Get specific queue
        val queue = queueDao.getQueueById(queueId) ?: throw IllegalArgumentException("Queue not found")
        
        // Get items for this queue
        val items = itemDao.getItemsByQueueId(queue.id).value ?: emptyList()
        
        val exportItems = items.map { item ->
            val images = itemImageDao.getImagesByItemId(item.id).value ?: emptyList()
            
            ExportItem(
                id = item.id,
                name = item.name,
                description = item.description,
                createdAt = dateFormat.format(item.createdAt),
                updatedAt = dateFormat.format(item.updatedAt),
                images = images.map { image ->
                    ExportImage(
                        id = image.id,
                        imagePath = image.imagePath,
                        orderIndex = image.orderIndex
                    )
                }
            )
        }
        
        val exportQueue = ExportQueue(
            id = queue.id,
            name = queue.name,
            createdAt = dateFormat.format(queue.createdAt),
            updatedAt = dateFormat.format(queue.updatedAt),
            isSynced = queue.isSynced,
            items = exportItems
        )
        
        // Create export data with single queue
        val exportData = ExportData(
            exportDate = dateFormat.format(Date()),
            deviceInfo = mapOf(
                "manufacturer" to android.os.Build.MANUFACTURER,
                "model" to android.os.Build.MODEL,
                "androidVersion" to android.os.Build.VERSION.RELEASE
            ),
            queues = listOf(exportQueue)
        )
        
        // Create JSON
        val gson = GsonBuilder()
            .setPrettyPrinting()
            .create()
        
        val json = gson.toJson(exportData)
        
        // Save to file
        val exportDir = File(context.getExternalFilesDir(null), "exports")
        exportDir.mkdirs()
        
        val exportFile = File(exportDir, "ebaytools_queue_${queue.name}_${System.currentTimeMillis()}.json")
        FileWriter(exportFile).use { writer ->
            writer.write(json)
        }
        
        exportFile
    }
}