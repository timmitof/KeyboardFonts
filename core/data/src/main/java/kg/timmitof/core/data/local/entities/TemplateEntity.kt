package kg.timmitof.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bitmapFilePath: String,
    val name: String
)
