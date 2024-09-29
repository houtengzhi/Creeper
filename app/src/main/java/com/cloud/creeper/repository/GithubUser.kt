package com.cloud.creeper.repository

import kotlinx.serialization.Serializable

@Serializable
data class GithubUser(val name: String, val email: String)
