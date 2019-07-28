package com.kernel.colibri.core.helpers

import android.support.test.InstrumentationRegistry
import android.support.test.uiautomator.UiDevice
import com.kernel.colibri.core.Config
import com.kernel.colibri.core.Config.FILE_OUTPUT
import com.kernel.colibri.core.Config.TAG
import com.kernel.colibri.core.FileLog
import java.io.File

object ScreenshotHelper {
    var screenshotIndex = 0
    lateinit var lastFilename: String

    fun takeScreenshots(message: String) {
        var message = message
        if (message.length > 50) {
            message = message.substring(0, 49)
        }

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.waitForIdle(Config.TIME_PAUSE.valueAsMs)

        var activity = device.currentActivityName ?: "Unknown"
        if (activity.length > 30) {
            activity = activity.substring(0, 29)
        }

        lastFilename = if (message.length > 0) {
            String.format("(%d) %s %s.png",
                    screenshotIndex, toValidFileName(activity), toValidFileName(message))
        } else {
            String.format("(%d) %s.png",
                    screenshotIndex, toValidFileName(activity))
        }

        device.takeScreenshot(File("$FILE_OUTPUT/$lastFilename"))
        screenshotIndex++
        FileLog.i(TAG, "{Screenshot} $lastFilename")
    }

    fun toValidFileName(input: String?): String {
        return input?.replace("[:\\\\/*\"?|<>']".toRegex(), "_") ?: ""
    }
}