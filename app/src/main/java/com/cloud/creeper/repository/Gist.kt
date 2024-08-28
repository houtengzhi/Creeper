package com.cloud.creeper.repository

import kotlinx.serialization.Serializable

/**
 *
 * Created by cloud on 2024/7/8.
 */
@Serializable
data class Gist(val id: String, val description: String, val url: String, val files: Map<String, GistFile>)