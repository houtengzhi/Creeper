package com.cloud.spider.repository.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 *
 * Created by cloud on 2024/2/27.
 */
data class ConverterWithSources(@Embedded val converter: Converter,
                                @Relation(parentColumn = "converter_id", entityColumn = "source_id", associateBy = Junction(ConverterSubscriptionSourceCrossRef::class))
                                val subscriptionSourceList: List<SubscriptionSource>
)
