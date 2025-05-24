package com.ebaytools.companion.data.dao

import androidx.room.*
import com.ebaytools.companion.data.models.ItemImage
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemImageDao {
    @Query("SELECT * FROM item_images WHERE itemId = :itemId ORDER BY orderIndex ASC")
    fun getImagesByItemId(itemId: Long): Flow<List<ItemImage>>
    
    @Insert
    suspend fun insertImage(image: ItemImage): Long
    
    @Insert
    suspend fun insertImages(images: List<ItemImage>)
    
    @Update
    suspend fun updateImage(image: ItemImage)
    
    @Delete
    suspend fun deleteImage(image: ItemImage)
    
    @Query("DELETE FROM item_images WHERE itemId = :itemId")
    suspend fun deleteAllImagesForItem(itemId: Long)
    
    @Query("SELECT COUNT(*) FROM item_images WHERE itemId = :itemId")
    suspend fun getImageCountForItem(itemId: Long): Int
}