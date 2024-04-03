package com.cloud.spider

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cloud.spider.base.BaseActivity
import com.cloud.spider.ui.AppMain
import dagger.hilt.android.AndroidEntryPoint

/**
 *
 * Created by cloud on 2024/1/26.
 */
@AndroidEntryPoint
class MainActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            AppMain()
        }
    }
}