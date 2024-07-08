package com.cloud.creeper.util

import com.cloud.creeper.protocol.ClientType

/**
 *
 * Created by cloud on 2024/4/12.
 */
val SUPPORTED_SOURCE_TYPE_LIST = mutableListOf<ClientType>(ClientType.Clash, ClientType.V2Ray)
val SUPPORTED_OUTPUT_TYPE_LIST = mutableListOf<ClientType>(ClientType.Clash, ClientType.V2Ray)

const val SERVICE_GITHUB = "github"