package com.ebaytools.companion.data.dao

import androidx.room.*
import com.ebaytools.companion.data.models.Item
import com.ebaytools.companion.data.models.ItemWithImages
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE queueId = :queueId ORDER BY createdAt DESC")
    fun getItemsByQueueId(queueId: Long): Flow<List<Item>>
    
    @Transaction
    @Query("SELECT * FROM items WHERE queueId = :queueId ORDER BY createdAt DESC")
    fun getItemsWithImagesByQueueId(queueId: Long): Flow<List<ItemWithImages>>
    
    @Query("SELECT * FROM items WHERE id = :itemId")
    suspend fun getItemById(itemId: Long): Item?
    
    @Insert
    suspend fun insertItem(item: Item): Long
    
    @Update
    suspend fun updateItem(item: Item)
    
    @Delete
    suspend fun deleteItem(item: Item)
    
    @Query("SELECT COUNT(*) FROM items WHERE queueId = :queueId")
    suspend fun getItemCountForQueue(queueId: Long): Int
}