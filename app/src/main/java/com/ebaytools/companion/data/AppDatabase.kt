package com.ebaytools.companion.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ebaytools.companion.data.converters.DateConverter
import com.ebaytools.companion.data.dao.ItemDao
import com.ebaytools.companion.data.dao.ItemImageDao
import com.ebaytools.companion.data.dao.QueueDao
import com.ebaytools.companion.data.models.Item
import com.ebaytools.companion.data.models.ItemImage
import com.ebaytools.companion.data.models.Queue

@Database(
    entities = [Queue::class, Item::class, ItemImage::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun queueDao(): QueueDao
    abstract fun itemDao(): ItemDao
    abstract fun itemImageDao(): ItemImageDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ebaytools_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}