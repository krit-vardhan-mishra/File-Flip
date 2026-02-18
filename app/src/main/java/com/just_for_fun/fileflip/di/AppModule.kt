package com.just_for_fun.fileflip.di

import com.just_for_fun.fileflip.data.repository.MarkdownRepositoryImpl
import com.just_for_fun.fileflip.domain.repository.MarkdownRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMarkdownRepository(@dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context): MarkdownRepository {
        return MarkdownRepositoryImpl(context)
    }
}
