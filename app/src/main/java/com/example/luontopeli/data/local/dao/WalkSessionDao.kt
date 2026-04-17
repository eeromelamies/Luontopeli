package com.example.luontopeli.data.local.dao

import androidx.room.*
import com.example.luontopeli.data.local.entity.WalkSession
import kotlinx.coroutines.flow.Flow

@Dao
interface WalkSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: WalkSession)

    @Query("SELECT * FROM walk_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<WalkSession>>

    @Query("SELECT * FROM walk_sessions WHERE id = :id")
    suspend fun getSessionById(id: String): WalkSession?

    @Delete
    suspend fun delete(session: WalkSession)
}
