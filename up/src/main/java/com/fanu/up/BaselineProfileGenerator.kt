package com.fanu.up

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This test class generates a basic startup baseline profile for the target package.
 *
 * We recommend you start with this but add important user flows to the profile to improve their performance.
 * Refer to the [baseline profile documentation](https://d.android.com/topic/performance/baselineprofiles)
 * for more information.
 *
 * You can run the generator with the "Generate Baseline Profile" run configuration in Android Studio or
 * the equivalent `generateBaselineProfile` gradle task:
 * ```
 * ./gradlew :app:generateReleaseBaselineProfile
 * ```
 * The run configuration runs the Gradle task and applies filtering to run only the generators.
 *
 * Check [documentation](https://d.android.com/topic/performance/benchmarking/macrobenchmark-instrumentation-args)
 * for more information about available instrumentation arguments.
 *
 * After you run the generator, you can verify the improvements running the [StartupBenchmarks] benchmark.
 *
 * When using this class to generate a baseline profile, only API 33+ or rooted API 28+ are supported.
 *
 * The minimum required version of androidx.benchmark to generate a baseline profile is 1.2.0.
 **/
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        // The application id for the running build variant is read from the instrumentation arguments.
        rule.collect(
            packageName = InstrumentationRegistry.getArguments().getString("targetAppId")
                ?: throw Exception("targetAppId not passed as instrumentation runner arg"),

            // See: https://d.android.com/topic/performance/baselineprofiles/dex-layout-optimizations
            includeInStartupProfile = true
        ) {
            // This block defines the app's critical user journey. Here we are interested in
            // optimizing for app startup. But you can also navigate and scroll through your most important UI.

            // Start default activity for your app
            pressHome()
            startActivityAndWait()

            // 1. 等待主页加载并尝试滚动列表
            // 使用 wait(Until.findObject(...)) 比 findObject 更稳健
            val scrollableList = device.wait(Until.findObject(By.scrollable(true)), 5000)
            if (scrollableList != null) {
                scrollableList.setGestureMargin(device.displayWidth / 5)
                scrollableList.fling(Direction.DOWN)
            }
            for (i in 1..6) {// 2. 模拟点击“添加”按钮进入编辑页（优化跳转性能）
                // 在 MainActivity.kt 中，你的添加按钮 contentDescription 是 "添加"
                val addButton = device.wait(Until.findObject(By.desc("添加")), 5000)
                addButton?.click()

                // 3. 等待编辑界面出来并输入一些文字
                // 这里的 By.desc("test_auto_title_exp") 对应 TextField 的 contentDescription
                val inputField = device.wait(Until.findObject(By.desc("test_auto_title_exp")), 5000)
                inputField?.text = "自动生成的笔记"

                // 4. 保存并返回
                // 在 NoteEditActivity.kt 中，保存按钮 contentDescription 是 "保存"
                val saveButton = device.wait(Until.findObject(By.desc("保存")), 5000)
                saveButton?.click()

                device.pressBack()
            }

            // TODO Write more interactions to optimize advanced journeys of your app.
            // For example:
            // 1. Wait until the content is asynchronously loaded
            // 2. Scroll the feed content
            // 3. Navigate to detail screen

            // Check UiAutomator documentation for more information how to interact with the app.
            // https://d.android.com/training/testing/other-components/ui-automator
        }
    }
}