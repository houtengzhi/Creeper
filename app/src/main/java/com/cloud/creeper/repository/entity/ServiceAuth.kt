package com.cloud.creeper.repository.entity

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 *
 * Created by cloud on 2024/6/6.
 */
@Entity(tableName = "service_auth", indices = [Index(value = ["service_name"], unique = true)])
data class ServiceAuth(@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int = 0,
                       @ColumnInfo(name = "service_name")val serviceName: String,
                       @ColumnInfo(name = "service_uid") val serviceUid: String,
                       @ColumnInfo(name = "auth_type") val authType: String) : Parcelable {
    @ColumnInfo(name = "created_time")
    var createdTime: Long = 0

    @ColumnInfo(name = "updated_time")
    var updatedTime: Long = 0

    @ColumnInfo(name = "expired_time")
    var expiredTime: Long = 0

    @ColumnInfo(name = "user_name")
    var userName: String? = null

    var email: String? = null

    @ColumnInfo(name = "phone_number")
    var phoneNumber: String? = null

    @ColumnInfo(name = "access_token")
    var accessToken: String? = null

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    ) {
        createdTime = parcel.readLong()
        updatedTime = parcel.readLong()
        expiredTime = parcel.readLong()
        userName = parcel.readString()
        email = parcel.readString()
        phoneNumber = parcel.readString()
    }

    fun isExpired() = expiredTime > System.currentTimeMillis()
    override fun toString(): String {
        return "ServiceAuth(serviceName='$serviceName', accessToken='$accessToken', userName=$userName, email=$email, phoneNumber=$phoneNumber)"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(serviceName)
        parcel.writeString(accessToken)
        parcel.writeString(authType)
        parcel.writeLong(createdTime)
        parcel.writeLong(updatedTime)
        parcel.writeLong(expiredTime)
        parcel.writeString(userName)
        parcel.writeString(email)
        parcel.writeString(phoneNumber)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ServiceAuth> {
        override fun createFromParcel(parcel: Parcel): ServiceAuth {
            return ServiceAuth(parcel)
        }

        override fun newArray(size: Int): Array<ServiceAuth?> {
            return arrayOfNulls(size)
        }
    }


}