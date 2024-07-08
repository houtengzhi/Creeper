package com.cloud.creeper.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 *
 * Created by cloud on 2024/7/8.
 */
class MyColors(
    colorPositive: Color
) {
    var colorPositive: Color by mutableStateOf(colorPositive)
        private set
}