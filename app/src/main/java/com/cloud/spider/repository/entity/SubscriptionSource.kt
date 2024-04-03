package com.cloud.spider.repository.entity

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cloud.spider.util.SystemUtil

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

    @ColumnInfo(name = "pulled_time")
    var pulledTime: Long = 0

    @ColumnInfo(name = "pull_status")
    var pullStatus: SourceStatus = SourceStatus.IDLE

    @ColumnInfo(name = "cache_name")
    var cacheName: String? = null

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    ) {
        createdTime = parcel.readLong()
        updatedTime = parcel.readLong()
        pulledTime = parcel.readLong()
        pullStatus = SourceStatus.valueOf(parcel.readString()!!)
        cacheName = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(sourceUrl)
        parcel.writeString(type)
        parcel.writeLong(createdTime)
        parcel.writeLong(updatedTime)
        parcel.writeLong(pulledTime)
        parcel.writeString(pullStatus.name)
        cacheName?.let {
            parcel.writeString(it)
        }
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

    fun getPulledTimeText(context: Context): String {
        return when (pullStatus) {
            SourceStatus.IDLE, SourceStatus.PENDING -> {
                "Updating"
            }
            SourceStatus.UPDATED -> {
                SystemUtil.getPulledTimeText(context, pulledTime)
            }
            SourceStatus.FAILED -> {
                "Updated failed"
            }
        }
    }
}

enum class SourceStatus() {
    IDLE,
    PENDING,
    FAILED,
    UPDATED;
}
