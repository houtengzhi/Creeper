package com.cloud.creeper.repository

/**
 *
 * Created by cloud on 2024/7/8.
 */
data class Gist(val id: String, val description: String, val url: String, val files: Map<String, GistFile>)