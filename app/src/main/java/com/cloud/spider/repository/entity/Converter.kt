package com.cloud.spider.repository.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 *
 * Created by cloud on 2024/2/27.
 */
@Entity(tableName = "converter", indices = [Index(value = ["name"], unique = true)])
data class Converter(@PrimaryKey @ColumnInfo(name = "converter_id") val id: String, val name: String) {

}
