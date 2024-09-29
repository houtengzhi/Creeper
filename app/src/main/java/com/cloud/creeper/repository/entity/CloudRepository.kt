package com.cloud.creeper.repository.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *
 * Created by cloud on 2024/7/12.
 */
@Entity(tableName = "cloud_repository")
data class CloudRepository(@PrimaryKey @ColumnInfo(name = "repos_id") val id: String, val type: String) {

    var url: String? = null

    @ColumnInfo(name = "service_id")
    var serviceId: String? = null

    @ColumnInfo(name = "access_token")
    var accessToken: String? = null

    @ColumnInfo(name = "gist_id")
    var gistId: String? = null

    @ColumnInfo(name = "gist_file_name")
    var gistFileName: String? = null

}
