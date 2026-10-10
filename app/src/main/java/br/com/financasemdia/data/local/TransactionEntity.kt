package br.com.financasemdia.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    @ColumnInfo(name = "amount_cents") val amountCents: Long,
    val type: String,
    val category: String,
    @ColumnInfo(name = "epoch_day") val epochDay: Long,
    val note: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)
