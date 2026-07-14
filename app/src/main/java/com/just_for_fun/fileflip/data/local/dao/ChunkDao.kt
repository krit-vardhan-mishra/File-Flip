package com.just_for_fun.fileflip.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.just_for_fun.fileflip.data.local.entity.ChunkEntity

@Dao
interface ChunkDao {

    @Query("SELECT * FROM chunks WHERE fileId = :fileId ORDER BY chunkIndex ASC")
    suspend fun getChunksForFile(fileId: String): List<ChunkEntity>

    @Query("SELECT * FROM chunks")
    suspend fun getAllChunks(): List<ChunkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChunk(chunk: ChunkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChunks(chunks: List<ChunkEntity>)

    @Update
    suspend fun updateChunk(chunk: ChunkEntity)

    @Delete
    suspend fun deleteChunk(chunk: ChunkEntity)

    @Query("DELETE FROM chunks WHERE fileId = :fileId")
    suspend fun deleteChunksForFile(fileId: String)
}
