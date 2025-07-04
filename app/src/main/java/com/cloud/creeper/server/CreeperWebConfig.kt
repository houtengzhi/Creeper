package com.cloud.creeper.server

import android.content.Context
import com.cloud.creeper.util.FileUtil
import com.yanzhenjie.andserver.annotation.Config
import com.yanzhenjie.andserver.framework.config.Multipart
import com.yanzhenjie.andserver.framework.config.WebConfig
import com.yanzhenjie.andserver.framework.website.AssetsWebsite

@Config
class CreeperWebConfig: WebConfig {
    override fun onConfig(
        context: Context,
        delegate: WebConfig.Delegate
    ) {
        delegate.addWebsite(AssetsWebsite(context, "/web/"))

        val uploadDir = FileUtil.getFile("sdcard/")

        delegate.setMultipart(Multipart.newBuilder()
            .allFileMaxSize(1024 * 1024 * 20) // 单个请求所有文件总大小
            .fileMaxSize(1024 * 1024 * 5) // 单个请求每个文件大小
            .maxInMemorySize(1024 * 20) // 内存缓存大小
            .uploadTempDir(uploadDir) // 上传文件保存目录
            .build())
    }
}