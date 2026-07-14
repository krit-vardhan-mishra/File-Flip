package com.just_for_fun.fileflip.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.just_for_fun.fileflip.data.local.entity.ProviderConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProviderConfigDao {

    @Query("SELECT * FROM provider_configs")
    fun getAllConfigs(): Flow<List<ProviderConfigEntity>>

    @Query("SELECT * FROM provider_configs WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveConfig(): ProviderConfigEntity?

    @Query("SELECT * FROM provider_configs WHERE providerName = :providerName LIMIT 1")
    suspend fun getConfigForProvider(providerName: String): ProviderConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: ProviderConfigEntity)

    @Update
    suspend fun updateConfig(config: ProviderConfigEntity)

    @Delete
    suspend fun deleteConfig(config: ProviderConfigEntity)

    @Query("UPDATE provider_configs SET isActive = 0")
    suspend fun deactivateAllConfigs()

    @Query("UPDATE provider_configs SET isActive = 1 WHERE providerName = :providerName")
    suspend fun activateConfig(providerName: String)
}
