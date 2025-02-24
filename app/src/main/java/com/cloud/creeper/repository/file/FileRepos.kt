package com.cloud.creeper.repository.file

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    fun deleteSubscriptionSource(fileName: String): Boolean {
        val path = "${cacheDirectory}/subscription-source"
        return deleteFile(path, fileName)
    }

    fun readSubscriptionSourceFile(fileName: String): File {
        val path = "${cacheDirectory}/subscription-source"
        return File(path, fileName)
    }

    fun saveConverter(fileName: String, content: String): File {
        val path = "${fileDirectory}/converter"
        return saveAsFile(path, fileName, content)
    }

    fun readConverter(fileName: String): String {
        val path = "${fileDirectory}/converter"
        return readFile(path, fileName)
    }

    fun deleteConverter(fileName: String): Boolean {
        val path = "${fileDirectory}/converter"
        return deleteFile(path, fileName)
    }

    fun readConverterFile(fileName: String): File {
        val path = "${fileDirectory}/converter"
        return File(path, fileName)
    }

    suspend fun suspendSaveConverter(fileName: String, content: String): File {
        return withContext(Dispatchers.IO) {
            saveConverter(fileName, content)
        }
    }

    suspend fun suspendReadConverter(fileName: String): String {
        return withContext(Dispatchers.IO) {
            readConverter(fileName)
        }
    }

    suspend fun suspendDeleteConverter(fileName: String) {
        return withContext(Dispatchers.IO) {
            deleteConverter(fileName)
        }
    }

    private fun saveAsFile(parentPath: String, fileName: String, content: String): File {
        Log.d(TAG, "saveAsFile(), parentPath=${parentPath}, name=${fileName}")
        val parentDir = File(parentPath)
        if (!parentDir.exists()) {
            parentDir.mkdirs()
        }
        val file = File(parentDir, fileName)
        file.writeText(content)
        return file
    }

    private fun readFile(parentPath: String, fileName: String): String {
        Log.d(TAG, "readFile(), parentPath=${parentPath}, name=${fileName}")
        return File(parentPath, fileName).readText()
    }

    private fun deleteFile(parentPath: String, fileName: String): Boolean {
        Log.d(TAG, "deleteFile(), parentPath=${parentPath}, name=${fileName}")
        val file = File(parentPath, fileName)
        if (file.exists()) {
            return file.delete()
        }
        return true
    }
}