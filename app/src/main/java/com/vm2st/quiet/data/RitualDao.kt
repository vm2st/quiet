package com.vm2st.quiet.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RitualDao {
    @Query("SELECT * FROM rituals ORDER BY createdAt DESC")
    fun getAllRituals(): Flow<List<Ritual>>

    @Insert
    suspend fun insert(ritual: Ritual): Long

    @Update
    suspend fun update(ritual: Ritual)

    @Delete
    suspend fun delete(ritual: Ritual)

    @Query("UPDATE rituals SET isConfirmedToday = 0")
    suspend fun resetAllConfirmations()

    @Query("SELECT * FROM rituals WHERE id = :id")
    suspend fun getRitualById(id: Int): Ritual?
}