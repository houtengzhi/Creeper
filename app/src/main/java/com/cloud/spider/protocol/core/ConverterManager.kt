package com.cloud.spider.protocol.core

import com.charleskorn.kaml.Yaml
import com.cloud.spider.protocol.clash.ClashConfig

/**
 *
 * Created by cloud on 2024/2/19.
 */
class ConverterManager {

    fun readClashSubscription(url: String): ApiResponse<ClashConfig> {
        return HttpHelper.getInstance().fetchSubscription(url)
            .mapSuccess {
                Yaml.default.decodeFromString(ClashConfig.serializer(), this)
            }
    }
}