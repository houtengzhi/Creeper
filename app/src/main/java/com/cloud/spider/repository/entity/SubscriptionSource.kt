package com.cloud.spider.repository.entity

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 *
 * Created by cloud on 2024/2/27.
 */
@Entity(tableName = "subscription_source")
data class SubscriptionSource(@PrimaryKey @ColumnInfo(name = "source_id") val id: String,
                              @ColumnInfo(name = "source_name") val name: String,
                              @ColumnInfo(name = "source_url") val sourceUrl: String,
                              @ColumnInfo(name = "source_type") val type: String): Parcelable {
    @ColumnInfo(name = "created_time")
    var createdTime: Long = 0

    @ColumnInfo(name = "updated_time")
    var updatedTime: Long = 0

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    ) {
        createdTime = parcel.readLong()
        updatedTime = parcel.readLong()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(sourceUrl)
        parcel.writeString(type)
        parcel.writeLong(createdTime)
        parcel.writeLong(updatedTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SubscriptionSource> {
        override fun createFromParcel(parcel: Parcel): SubscriptionSource {
            return SubscriptionSource(parcel)
        }

        override fun newArray(size: Int): Array<SubscriptionSource?> {
            return arrayOfNulls(size)
        }
    }
}
