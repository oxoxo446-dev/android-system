package com.example.adminapp.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa un registro genérico de administración.
 * La base de datos inicia completamente vacía — sin datos precargados.
 *
 * Los campos son intencionalmente genéricos para que AdminApp
 * sirva como plantilla CRUD real. Renómbralos según el dominio real.
 */
@Entity(tableName = "records")
data class Record(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
