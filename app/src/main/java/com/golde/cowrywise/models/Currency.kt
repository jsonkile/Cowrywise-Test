package com.golde.cowrywise.models

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currencies")
data class Currency(
    @NonNull
    @PrimaryKey(autoGenerate = false)
    var currency: String,
    @ColumnInfo(name = "nation")
    var nation: String? = ""
)