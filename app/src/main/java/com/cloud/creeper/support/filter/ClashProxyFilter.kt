package com.cloud.creeper.support.filter


import com.cloud.creeper.protocol.clash.ClashProxyNode

class ClashProxyFilter(private val excludedValues: List<String> = emptyList(), private val excludedRegex : Regex? = null) {

    companion object {
        val DEFAULT_UNSUPPORTED_CIPHER = listOf("2022-blake3-aes-128-gcm")
    }

    private val allExcludedValues = excludedValues + DEFAULT_UNSUPPORTED_CIPHER

    fun isExcluded(node: ClashProxyNode): Boolean {
        val text = node.toString()

        if (allExcludedValues.any { text.contains(it, ignoreCase = true) }) {
            return true
        }

        return excludedRegex?.containsMatchIn(text) == true
    }
}