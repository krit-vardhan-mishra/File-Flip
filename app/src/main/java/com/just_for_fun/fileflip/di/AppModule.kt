package com.just_for_fun.fileflip.di

import android.content.Context
import androidx.room.Room
import com.just_for_fun.fileflip.data.ai.GeminiProvider
import com.just_for_fun.fileflip.data.ai.GroqProvider
import com.just_for_fun.fileflip.data.ai.OpenRouterProvider
import com.just_for_fun.fileflip.data.ai.ProviderFactory
import com.just_for_fun.fileflip.data.local.FileFlipDatabase
import com.just_for_fun.fileflip.data.local.dao.*
import com.just_for_fun.fileflip.data.repository.ChatRepositoryImpl
import com.just_for_fun.fileflip.data.repository.MarkdownRepositoryImpl
import com.just_for_fun.fileflip.domain.repository.ChatRepository
import com.just_for_fun.fileflip.domain.repository.MarkdownRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMarkdownRepository(@ApplicationContext context: Context): MarkdownRepository {
        return MarkdownRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FileFlipDatabase {
        return Room.databaseBuilder(
            context,
            FileFlipDatabase::class.java,
            "fileflip_db"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideChatSessionDao(database: FileFlipDatabase): ChatSessionDao {
        return database.chatSessionDao()
    }

    @Provides
    @Singleton
    fun provideChatMessageDao(database: FileFlipDatabase): ChatMessageDao {
        return database.chatMessageDao()
    }

    @Provides
    @Singleton
    fun provideWorkspaceDao(database: FileFlipDatabase): WorkspaceDao {
        return database.workspaceDao()
    }

    @Provides
    @Singleton
    fun provideFileDao(database: FileFlipDatabase): FileDao {
        return database.fileDao()
    }

    @Provides
    @Singleton
    fun provideChunkDao(database: FileFlipDatabase): ChunkDao {
        return database.chunkDao()
    }

    @Provides
    @Singleton
    fun provideProviderConfigDao(database: FileFlipDatabase): ProviderConfigDao {
        return database.providerConfigDao()
    }

    @Provides
    @Singleton
    fun provideChatRepository(impl: ChatRepositoryImpl): ChatRepository {
        return impl
    }

    @Provides
    @Singleton
    fun provideProviderFactory(
        geminiProvider: GeminiProvider,
        groqProvider: GroqProvider,
        openRouterProvider: OpenRouterProvider
    ): ProviderFactory {
        return ProviderFactory(geminiProvider, groqProvider, openRouterProvider)
    }
}

