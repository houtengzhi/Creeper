package com.cloud.spider.repository.file

import android.util.Log
import java.io.File

/**
 *
 * Created by cloud on 2024/2/21.
 */
class FileRepos(private val cacheDirectory: String, private val fileDirectory: String) {

    companion object {
        private const val TAG = "FileRepos"
    }

    fun saveSubscriptionSource(fileName: String, content: String) {
        val path = "${cacheDirectory}/subscription-source"
        saveAsFile(path, fileName, content)
    }

    fun readSubscriptionSource(fileName: String): String {
        val path = "${cacheDirectory}/subscription-source"
        return readFile(path, fileName)
    }

    private fun saveAsFile(parentPath: String, fileName: String, content: String) {
        Log.d(TAG, "saveAsFile(), parentPath=${parentPath}, name=${fileName}")
        val parentDir = File(parentPath)
        if (!parentDir.exists()) {
            parentDir.mkdirs()
        }
        val file = File(parentDir, fileName)
        file.writeText(content)
    }

    private fun readFile(parentPath: String, fileName: String): String {
        Log.d(TAG, "readFile(), parentPath=${parentPath}, name=${fileName}")
        return File(parentPath, fileName).readText()
    }
}