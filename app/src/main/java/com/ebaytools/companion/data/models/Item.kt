package com.ebaytools.companion.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(
            entity = Queue::class,
            parentColumns = ["id"],
            childColumns = ["queueId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val queueId: Long,
    val name: String,
    val description: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)