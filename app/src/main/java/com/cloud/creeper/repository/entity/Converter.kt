package com.cloud.creeper.repository.entity

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cloud.creeper.R
import com.cloud.creeper.protocol.ClientType
import com.cloud.creeper.server.ServerManage
import com.cloud.creeper.util.NetUtil
import com.cloud.creeper.util.SystemUtil
import java.io.File

/**
 *
 * Created by cloud on 2024/2/27.
 */
@Entity(tableName = "converter", indices = [Index(value = ["name"], unique = true)])
data class Converter(@PrimaryKey @ColumnInfo(name = "converter_id") val id: String, val name: String): Parcelable {

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

    @Ignore
    var outputFile: File? = null

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!
    ) {
        description = parcel.readString()
        createdTime = parcel.readLong()
        updatedTime = parcel.readLong()
        outputType = ClientType.valueOf(parcel.readString()!!)
        outputFileName = parcel.readString()
    }


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

    fun toSimpleString(): String {
        return "Converter(id='$id', name='$name', outputType='$outputType')"
    }

    private fun getUrlSegments() ="creeper/converter/${id}/${outputFileName}"

    fun getLocalAddress() = "http://${NetUtil.getLocalIPAddress()?.hostAddress}:${ServerManage.DEFAULT_PORT}/${getUrlSegments()}"

    fun getUpdatedTimeText(context: Context): String {
        return SystemUtil.getPulledTimeText(context, updatedTime)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeLong(createdTime)
        parcel.writeLong(updatedTime)
        parcel.writeString(outputType.name)
        parcel.writeString(outputFileName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Converter> {
        override fun createFromParcel(parcel: Parcel): Converter {
            return Converter(parcel)
        }

        override fun newArray(size: Int): Array<Converter?> {
            return arrayOfNulls(size)
        }
    }
}
