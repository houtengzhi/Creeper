package com.cloud.spider.server.controller

import com.yanzhenjie.andserver.annotation.GetMapping
import com.yanzhenjie.andserver.annotation.RestController

/**
 *
 * Created by cloud on 2024/1/30.
 */
@RestController
class SubscriptionController {

    @GetMapping("/subscription")
    fun getSubscription(): String {
        return "test"
    }
}