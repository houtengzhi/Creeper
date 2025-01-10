package com.cloud.creeper.server

import android.util.Log
import com.yanzhenjie.andserver.annotation.Converter
import com.yanzhenjie.andserver.framework.MessageConverter
import com.yanzhenjie.andserver.framework.body.JsonBody
import com.yanzhenjie.andserver.http.ResponseBody
import com.yanzhenjie.andserver.util.IOUtils
import com.yanzhenjie.andserver.util.MediaType
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.io.InputStream
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.jvmErasure

@OptIn(InternalSerializationApi::class)
@Converter
class ServerMessageConverter: MessageConverter {
    companion object {
        private const val TAG = "ServerMessageConverter"
    }

    override fun convert(output: Any?, mediaType: MediaType?): ResponseBody {
        Log.v(TAG, "serialize ${output}")
        return JsonBody(Json.Default.encodeToString(output))
    }

    override fun <T : Any?> convert(stream: InputStream, mediaType: MediaType?, type: Type): T {
        val charset = if (mediaType === null) null else mediaType.charset
        val str= if (charset == null) {
           IOUtils.toString(stream)
        } else {
           IOUtils.toString(stream, charset)
        }

        val kType = javaTypeToKType(type) ?: throw IllegalArgumentException("Cannot convert Java Type ${type} to KType")

        Log.v(TAG, "deserialize ${str} ${kType}")
        return Json.Default.decodeFromString(kType.jvmErasure.serializer(), str) as T
    }

    fun javaTypeToKType(type: Type): KType? {
        return when (type) {
            is Class<*> -> type.kotlin.createType()
            is ParameterizedType -> {
                val rawType = (type.rawType as? Class<*>)?.kotlin ?: return null
                val typeArguments = type.actualTypeArguments.map { javaTypeToKType(it) ?: return null }
                rawType.createType(typeArguments.map { kotlin.reflect.KTypeProjection.invariant(it) })
            }
            else -> null
        }
    }
}