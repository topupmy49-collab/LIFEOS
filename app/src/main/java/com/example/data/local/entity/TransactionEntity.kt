package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "INCOME", "EXPENSE"
    val amountCents: Long, // Integer minor units (e.g. 500 BDT = 50000 cents)
    val currencyCode: String = "BDT",
    val category: String,
    val note: String = "",
    val transactionDateEpoch: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
