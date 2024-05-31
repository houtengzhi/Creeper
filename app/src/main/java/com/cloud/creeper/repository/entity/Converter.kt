package com.cloud.creeper.repository.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cloud.creeper.R
import com.cloud.creeper.protocol.ClientType
import com.cloud.creeper.server.ServerManage
import com.cloud.creeper.util.NetUtil

/**
 *
 * Created by cloud on 2024/2/27.
 */
@Entity(tableName = "converter", indices = [Index(value = ["name"], unique = true)])
data class Converter(@PrimaryKey @ColumnInfo(name = "converter_id") val id: String, val name: String) {

    @ColumnInfo(name = "description")
    var description: String? = null

    @ColumnInfo(name = "created_time")
    var createdTime: Long = 0

    @ColumnInfo(name = "updated_time")
    var updatedTime: Long = 0

    @ColumnInfo(name = "output_type")
    var outputType: ClientType = ClientType.Clash

    @ColumnInfo(name = "output_file_name")
    var outputFileName: String? = null

    fun getClientIconResId():Int {
        return when (outputType) {
            ClientType.Clash -> {
                R.drawable.ic_clashr
            }
            ClientType.V2Ray -> {
                R.drawable.ic_v2ray
            }
            else -> {
                R.drawable.ic_clashr
            }
        }
    }

    override fun toString(): String {
        return "Converter(id='$id', name='$name', outputType='$outputType'), localAddr='${getLocalAddress()}'"
    }

    fun getUrlSegments() ="spider/converter/${id}/${outputFileName}"

    fun getLocalAddress() = "http://${NetUtil.getLocalIPAddress()?.hostAddress}:${ServerManage.DEFAULT_PORT}/${getUrlSegments()}"
}
