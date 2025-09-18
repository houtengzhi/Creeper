package com.cloud.creeper.support.filter


import com.cloud.creeper.protocol.clash.ClashProxyNode

class ClashProxyFilter(private val excludedValues: List<String> = emptyList(), private val excludedRegex : Regex? = null) {

    companion object {
        val DEFAULT_UNSUPPORTED_CIPHER = listOf("2022-blake3-aes-128-gcm")
        val DEFAULT_UNSUPPORTED_TYPE = listOf("tuic", "hysteria")
    }

    fun isExcluded(node: ClashProxyNode): Boolean {

        if (DEFAULT_UNSUPPORTED_TYPE.any { node.type.contains(it, ignoreCase = true) }) {
            return true
        }
        if (DEFAULT_UNSUPPORTED_CIPHER.any { node.cipher.orEmpty().contains(it, ignoreCase = true) }) {
            return true
        }
        if (excludedValues.any { node.toString().contains(it, ignoreCase = true) }) {
            return true
        }

        return excludedRegex?.containsMatchIn( node.toString()) == true
    }
}