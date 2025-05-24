package com.ebaytools.companion.data.models

import androidx.room.Embedded
import androidx.room.Relation

data class ItemWithImages(
    @Embedded val item: Item,
    @Relation(
        parentColumn = "id",
        entityColumn = "itemId"
    )
    val images: List<ItemImage>
)