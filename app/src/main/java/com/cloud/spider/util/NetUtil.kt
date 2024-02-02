package com.cloud.spider.util

import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.Enumeration
import java.util.regex.Pattern


/**
 *
 * Created by cloud on 2024/1/30.
 */
object NetUtil {

    private val IPV4_PATTERN: Pattern = Pattern.compile(
        "^(" + "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}" +
                "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$"
    )

    fun isIPv4Address(input: CharSequence): Boolean {
        return IPV4_PATTERN.matcher(input).matches()
    }

    fun getLocalIPAddress(): InetAddress? {
        var enumeration: Enumeration<NetworkInterface>? = null
        try {
            enumeration = NetworkInterface.getNetworkInterfaces()
        } catch (e: SocketException) {
            e.printStackTrace()
        }
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                val nif = enumeration.nextElement()
                val inetAddresses = nif.inetAddresses
                if (inetAddresses != null) {
                    while (inetAddresses.hasMoreElements()) {
                        val inetAddress = inetAddresses.nextElement()
                        if (!inetAddress.isLoopbackAddress && isIPv4Address(inetAddress.hostAddress)) {
                            return inetAddress
                        }
                    }
                }
            }
        }
        return null
    }
}