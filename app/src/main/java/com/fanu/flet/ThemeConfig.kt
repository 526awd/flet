package com.fanu.flet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * 全局主题配置管理
 */
object ThemeConfig {
    // 默认跟随系统：null，手动开启：true，手动关闭：false
    var darkModeState: Boolean? by mutableStateOf(null)
}
