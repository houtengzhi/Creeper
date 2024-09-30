package com.cloud.creeper.repository.entity

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *
 * Created by cloud on 2024/7/12.
 */
@Entity(tableName = "cloud_repository")
data class CloudRepository(@PrimaryKey @ColumnInfo(name = "repos_id") val id: String, val type: String): Parcelable {

    var url: String? = null

    @ColumnInfo(name = "service_id")
    var serviceId: String? = null

    @ColumnInfo(name = "access_token")
    var accessToken: String? = null

    @ColumnInfo(name = "gist_id")
    var gistId: String? = null

    @ColumnInfo(name = "gist_file_name")
    var gistFileName: String? = null

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!
    ) {
        url = parcel.readString()
        serviceId = parcel.readString()
        accessToken = parcel.readString()
        gistId = parcel.readString()
        gistFileName = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(type)
        parcel.writeString(url)
        serviceId?.let {
            parcel.writeString(it)
        }
        accessToken?.let {
            parcel.writeString(it)
        }
       gistId?.let {
           parcel.writeString(it)
       }
        gistFileName?.let {
            parcel.writeString(it)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CloudRepository> {
        override fun createFromParcel(parcel: Parcel): CloudRepository {
            return CloudRepository(parcel)
        }

        override fun newArray(size: Int): Array<CloudRepository?> {
            return arrayOfNulls(size)
        }
    }

}
