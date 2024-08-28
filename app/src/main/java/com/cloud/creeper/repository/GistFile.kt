package com.cloud.creeper.repository

import android.os.Parcel
import android.os.Parcelable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 *
 * Created by cloud on 2024/7/11.
 */
@Serializable
data class GistFile(
    @SerialName("filename") val fileName: String,
    val type: String,
    val language: String?,
    val size: Int,
    @SerialName("raw_url") val rawUrl: String
): Parcelable {
    @Transient
    var gistId: String? = null

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString(),
        parcel.readInt(),
        parcel.readString()!!
    ) {
        gistId = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(fileName)
        parcel.writeString(type)
        parcel.writeString(language)
        parcel.writeInt(size)
        parcel.writeString(rawUrl)
        parcel.writeString(gistId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GistFile> {
        override fun createFromParcel(parcel: Parcel): GistFile {
            return GistFile(parcel)
        }

        override fun newArray(size: Int): Array<GistFile?> {
            return arrayOfNulls(size)
        }
    }
}
