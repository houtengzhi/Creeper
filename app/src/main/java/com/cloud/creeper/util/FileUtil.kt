package com.cloud.creeper.util

import java.io.File

object FileUtil {

    fun getFile(path:String): File {
        val file = File(path)
        return if (file.exists()) {
            file
        } else {
            if (file.isFile) {
                file.createNewFile()
            } else {
                file.mkdirs()
            }
            file
        }
    }
}