package com.cloud.creeper.server.controller

import com.yanzhenjie.andserver.annotation.Controller
import com.yanzhenjie.andserver.annotation.GetMapping

@Controller
class PageController {

    @GetMapping("/")
    fun index(): String {
        return "forward:/index.html"
    }
}