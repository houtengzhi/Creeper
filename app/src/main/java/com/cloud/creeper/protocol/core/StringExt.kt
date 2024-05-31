package com.cloud.creeper.protocol.core

import java.net.URLDecoder
import java.net.URLEncoder
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 *
 * Created by cloud on 2024/4/15.
 */

@OptIn(ExperimentalEncodingApi::class)
fun String.b64Decode() = Base64.decode(this.filter { it.code < 128 }).decodeToString()

fun String.b64SafeDecode() =
    if (this.contains(":")) {
        this
    } else {
        runCatching {
            if (contains("=[^=]+.+".toRegex())) {
                split("=")
                    .filter { it.isNotEmpty() }
                    .joinToString(System.lineSeparator()) {
                        it.replace("_", "/").replace("-", "+").b64Decode()
                    }
            } else {
                trim().replace("_", "/").replace("-", "+").b64Decode()
            }
        }
            .getOrElse {
                println("failed: $this  ${it.message}")
                ""
            }
    }

@OptIn(ExperimentalEncodingApi::class)
fun String.b64Encode(): String = Base64.encode(toByteArray())

fun String.urlEncode() = URLEncoder.encode(this, "UTF-8").orEmpty()

fun String.urlDecode() = URLDecoder.decode(this, "UTF-8").orEmpty()