package com.cloud.creeper.base

object AppErrorCode {

    // Common
    const val UNKNOWN = -900
    const val CANCELED = -901
    const val UNSUPPORTED = -902
    const val INVALID_STATE = -903

    // Network
    const val NETWORK_UNAVAILABLE = -100
    const val NETWORK_TIMEOUT = -101
    const val DNS_ERROR = -102
    const val SSL_ERROR = -103
    const val HTTP_ERROR = -104
    const val NETWORK_PARSE_ERROR = -105
    const val TOKEN_EXPIRED_LOCAL = -110

    // Permission
    const val PERMISSION_DENIED = -200
    const val PERMISSION_ALWAYS_DENIED = -201
    const val PERMISSION_REQUIRED = -202

    // Storage / File
    const val FILE_NOT_FOUND = -300
    const val FILE_IO_ERROR = -301
    const val STORAGE_UNAVAILABLE = -302
    const val NO_SPACE = -303
    const val CREATE_DIR_FAILED = -304
    const val FILE_CORRUPTED = -305

    // Data
    const val DATA_PARSE_ERROR = -400
    const val DATA_SERIALIZE_ERROR = -401
    const val DATA_FORMAT_INVALID = -402
    const val PROTOCOL_INCOMPATIBLE = -403
    const val EMPTY_PROXY_LIST = -410

    // SDK
    const val SDK_ERROR = -500
    const val PAY_FAILED = -501
    const val LOGIN_FAILED = -502
    const val SYSTEM_API_UNAVAILABLE = -503

    // UI
    const val UI_DESTROYED = -600
    const val UI_INVISIBLE = -601
    const val FRAGMENT_INVALID = -602


}