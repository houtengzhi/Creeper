package com.cloud.creeper.repository.entity

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 *
 * Created by cloud on 2024/2/27.
 */
data class ConverterWithSources(@Embedded val converter: Converter,
                                @Relation(parentColumn = "converter_id", entityColumn = "source_id", associateBy = Junction(ConverterSubscriptionSourceCrossRef::class))
                                val subscriptionSourceList: List<SubscriptionSource>,
                                @Relation(parentColumn = "converter_id", entityColumn = "repos_id", associateBy = Junction(ConverterCloudRepositoryCrossRef::class))
                                val cloudRepositoryList: List<CloudRepository>?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Converter::class.java.classLoader)!!,
        parcel.createTypedArrayList(SubscriptionSource)!!,
        parcel.createTypedArrayList(CloudRepository)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(converter, flags)
        parcel.writeTypedList(subscriptionSourceList)
        parcel.writeTypedList(cloudRepositoryList)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ConverterWithSources> {
        override fun createFromParcel(parcel: Parcel): ConverterWithSources {
            return ConverterWithSources(parcel)
        }

        override fun newArray(size: Int): Array<ConverterWithSources?> {
            return arrayOfNulls(size)
        }
    }

}
