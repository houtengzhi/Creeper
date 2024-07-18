package com.cloud.creeper.repository.entity

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
                       val accessToken: String,
                       @ColumnInfo(name = "auth_type") val authType: String) {
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

    fun isExpired() = expiredTime > System.currentTimeMillis()
    override fun toString(): String {
        return "ServiceAuth(serviceName='$serviceName', accessToken='$accessToken', userName=$userName, email=$email, phoneNumber=$phoneNumber)"
    }


}