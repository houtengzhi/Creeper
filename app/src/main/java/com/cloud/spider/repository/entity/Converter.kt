package com.cloud.spider.repository.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cloud.spider.R
import com.cloud.spider.protocol.ClientType

/**
 *
 * Created by cloud on 2024/2/27.
 */
@Entity(tableName = "converter", indices = [Index(value = ["name"], unique = true)])
data class Converter(@PrimaryKey @ColumnInfo(name = "converter_id") val id: String, val name: String) {

    @ColumnInfo(name = "created_time")
    var createdTime: Long = 0

    @ColumnInfo(name = "updated_time")
    var updatedTime: Long = 0

    @ColumnInfo(name = "output_type")
    var outputType: String = ClientType.Clash.text

    @ColumnInfo(name = "output_file_name")
    var outputFileName: String? = null

    fun getClientIconResId():Int {
        return when (outputType) {
            ClientType.Clash.text -> {
                R.drawable.ic_clashr
            }
            ClientType.V2Ray.text -> {
                R.drawable.ic_v2ray
            }
            else -> {
                R.drawable.ic_clashr
            }
        }
    }
}
