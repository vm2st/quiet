package com.vm2st.quiet.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rituals")
data class Ritual(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isConfirmedToday: Boolean = false,
    val lastConfirmedTime: Long? = null   // время последнего подтверждения
)