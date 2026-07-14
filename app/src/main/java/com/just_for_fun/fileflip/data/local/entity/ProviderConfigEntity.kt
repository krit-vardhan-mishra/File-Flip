package com.just_for_fun.fileflip.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "provider_configs")
data class ProviderConfigEntity(
    @PrimaryKey val id: String,
    val providerName: String,
    val apiKey: String, // Encrypted
    val baseUrl: String?,
    val modelName: String?,
    val isActive: Boolean
)
