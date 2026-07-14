package com.just_for_fun.fileflip.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.just_for_fun.fileflip.data.local.dao.*
import com.just_for_fun.fileflip.data.local.entity.*

@Database(
    entities = [
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        WorkspaceEntity::class,
        FileEntity::class,
        ChunkEntity::class,
        ProviderConfigEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FileFlipDatabase : RoomDatabase() {
    abstract fun chatSessionDao(): ChatSessionDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun workspaceDao(): WorkspaceDao
    abstract fun fileDao(): FileDao
    abstract fun chunkDao(): ChunkDao
    abstract fun providerConfigDao(): ProviderConfigDao
}

