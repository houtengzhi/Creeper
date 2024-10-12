package com.cloud.creeper.repository.entity

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cloud.creeper.R
import com.cloud.creeper.protocol.ClientType
import com.cloud.creeper.util.SystemUtil

/**
 *
 * Created by cloud on 2024/2/27.
 */
@Entity(tableName = "subscription_source")
data class SubscriptionSource(@PrimaryKey @ColumnInfo(name = "source_id") val id: String,
                              @ColumnInfo(name = "source_name") val name: String,
                              @ColumnInfo(name = "source_url") val sourceUrl: String,
                              @ColumnInfo(name = "source_type") val type: ClientType): Parcelable {

    @ColumnInfo(name = "description")
    var description: String? = null

    @ColumnInfo(name = "created_time")
    var createdTime: Long = 0

    @ColumnInfo(name = "updated_time")
    var updatedTime: Long = 0

    @ColumnInfo(name = "pulled_time")
    var pulledTime: Long = 0

    @ColumnInfo(name = "pull_status")
    var pullStatus: SourceStatus = SourceStatus.IDLE

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        ClientType.valueOf(parcel.readString()!!)
    ) {
        description = parcel.readString()
        createdTime = parcel.readLong()
        updatedTime = parcel.readLong()
        pulledTime = parcel.readLong()
        pullStatus = SourceStatus.valueOf(parcel.readString()!!)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(sourceUrl)
        parcel.writeString(type.name)
        description?.let {
            parcel.writeString(it)
        }
        parcel.writeLong(createdTime)
        parcel.writeLong(updatedTime)
        parcel.writeLong(pulledTime)
        parcel.writeString(pullStatus.name)
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
                "Updating..."
            }
            SourceStatus.UPDATED -> {
                SystemUtil.getPulledTimeText(context, pulledTime)
            }
            SourceStatus.FAILED -> {
                "Updated failed"
            }
        }
    }

    fun getClientIconResId():Int {
        return when (type) {
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

    @Composable
    fun getClientIconColor():Color {
        return when (type) {
            ClientType.Clash -> {
                MaterialTheme.colorScheme.primary
            }
            ClientType.V2Ray -> {
                MaterialTheme.colorScheme.primary
            }
            else -> {
                MaterialTheme.colorScheme.primary
            }
        }
    }

    fun getCacheFileName(): String {
        return if (type == ClientType.Clash) {
            "${id}.yaml"
        } else {
            "${id}.txt"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SubscriptionSource

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}

enum class SourceStatus() {
    IDLE,
    PENDING,
    FAILED,
    UPDATED;
}
