package com.ebaytools.companion.data.dao

import androidx.room.*
import com.ebaytools.companion.data.models.Queue
import com.ebaytools.companion.data.models.QueueWithStats
import kotlinx.coroutines.flow.Flow

@Dao
interface QueueDao {
    @Query("SELECT * FROM queues ORDER BY updatedAt DESC")
    fun getAllQueues(): Flow<List<Queue>>
    
    @Query("""
        SELECT q.*, 
        COUNT(DISTINCT i.id) as itemCount, 
        COUNT(img.id) as imageCount 
        FROM queues q 
        LEFT JOIN items i ON q.id = i.queueId 
        LEFT JOIN item_images img ON i.id = img.itemId 
        GROUP BY q.id 
        ORDER BY q.updatedAt DESC
    """)
    fun getAllQueuesWithStats(): Flow<List<QueueWithStats>>
    
    @Query("SELECT * FROM queues WHERE id = :queueId")
    suspend fun getQueueById(queueId: Long): Queue?
    
    @Insert
    suspend fun insertQueue(queue: Queue): Long
    
    @Update
    suspend fun updateQueue(queue: Queue)
    
    @Delete
    suspend fun deleteQueue(queue: Queue)
    
    @Query("UPDATE queues SET isSynced = :isSynced, lastSyncedAt = :syncedAt WHERE id = :queueId")
    suspend fun updateSyncStatus(queueId: Long, isSynced: Boolean, syncedAt: Long?)
}