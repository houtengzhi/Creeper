package com.cloud.creeper.repository

/**
 *
 * Created by cloud on 2024/7/11.
 */
data class GistInput(val description: String?, val public: Boolean, val files: List<GistFileInput>)
