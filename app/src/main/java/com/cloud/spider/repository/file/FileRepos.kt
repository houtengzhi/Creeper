package com.cloud.spider.repository.file

import java.io.File

/**
 *
 * Created by cloud on 2024/2/21.
 */
class FileRepos(private val cacheDirectory: String, private val fileDirectory: String) {

    fun saveAsFile(parentPath: String, fileName: String, content: String) {
        File(parentPath, fileName).writeText(content)
    }

    fun readFile(parentPath: String, fileName: String): String {
        return File(parentPath, fileName).readText()
    }
}