package com.kernel.colibri.core

import android.os.Environment
import com.kernel.colibri.Colibri
import com.kernel.colibri.core.models.Duration
import java.io.File
import java.util.concurrent.TimeUnit

object Config {
    const val TAG = "Colibri"
    val FILE_OUTPUT = File(String.format("%s/%s/%s", Environment.getExternalStorageDirectory(), TAG, Colibri.packageName))
    val LOG_FILE_NAME = "$FILE_OUTPUT/$TAG.log"
    val TECH_LOG_FILE_NAME = "$FILE_OUTPUT/Performance.csv"
    val COMMON_BUTTONS = arrayOf("OK", "Cancel", "Yes", "No")
    val IGNORED_ACTIVITY = arrayOf<String>()
    val RANDOM_INPUT_TEXT = arrayOf<String>()
    val MAX_STEPS = Int.MAX_VALUE
    val MAX_DEPTH = 50
    var MAX_RUNTIME = Duration(5, TimeUnit.HOURS)
    var MAX_SCREEN_LOOP = Int.MAX_VALUE
    val TIME_PAUSE = Duration.ONE_SECOND
    val TIME_LAUNCH = Duration.FIVE_SECONDS
    var CAPTURE_STEPS = false
    val MAX_SCREENSHOTS = 100
}
