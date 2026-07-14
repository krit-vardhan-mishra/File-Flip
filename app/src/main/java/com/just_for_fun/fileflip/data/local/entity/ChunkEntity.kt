package com.just_for_fun.fileflip.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chunks",
    foreignKeys = [
        ForeignKey(
            entity = FileEntity::class,
            parentColumns = ["id"],
            childColumns = ["fileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("fileId")]
)
data class ChunkEntity(
    @PrimaryKey val id: String,
    val fileId: String,
    val chunkIndex: Int,
    val startByte: Int,
    val endByte: Int,
    val vectorEmbedding: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChunkEntity

        if (id != other.id) return false
        if (fileId != other.fileId) return false
        if (chunkIndex != other.chunkIndex) return false
        if (startByte != other.startByte) return false
        if (endByte != other.endByte) return false
        if (!vectorEmbedding.contentEquals(other.vectorEmbedding)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + fileId.hashCode()
        result = 31 * result + chunkIndex
        result = 31 * result + startByte
        result = 31 * result + endByte
        result = 31 * result + vectorEmbedding.contentHashCode()
        return result
    }
}
