package com.ebaytools.companion.data.models

import androidx.room.Embedded

data class QueueWithStats(
    @Embedded val queue: Queue,
    val itemCount: Int,
    val imageCount: Int
)