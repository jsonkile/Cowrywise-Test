package com.golde.cowrywise.models

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rates")
data class Rate(
    @NonNull @PrimaryKey(autoGenerate = false)
    var to: String,
    @ColumnInfo(name = "rate")
    var rate: Float? = 0F
)