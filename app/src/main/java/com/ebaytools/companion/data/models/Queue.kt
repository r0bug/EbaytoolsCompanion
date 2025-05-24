package com.ebaytools.companion.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "queues")
data class Queue(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val isSynced: Boolean = false,
    val lastSyncedAt: Date? = null
)