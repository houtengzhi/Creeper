package com.cloud.creeper.repository

import kotlinx.serialization.Serializable

@Serializable
data class GithubUser(val login: String, val id: Int, val name: String?, val email: String)
