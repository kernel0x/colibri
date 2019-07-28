package com.kernel.colibri.core.helpers

import android.support.test.InstrumentationRegistry
import android.support.test.uiautomator.*
import android.util.Log
import com.kernel.colibri.core.Config.TAG
import java.util.*

class UiWatcherHelper {

    private val errors = ArrayList<String>()

    fun registerAnrAndCrashWatchers() {
        device.registerWatcher("ANR") { handleAnr() }
        device.registerWatcher("ANR2") { handleAnr2() }
        device.registerWatcher("CRASH") { handleCrash() }
        device.registerWatcher("CRASH2") { handleCrash2() }
    }

    fun handleAnr(): Boolean {
        val window = device.findObject(UiSelector()
                .className("com.android.server.am.AppNotRespondingDialog"))
        var errorText: String? = null
        if (window.exists()) {
            try {
                errorText = window.text
            } catch (e: UiObjectNotFoundException) {
                // don't care, ignore
            }

            onAnrDetected(errorText)
            postHandler()
            return true
        }
        return false
    }

    fun handleAnr2(): Boolean {
        val window = device.findObject(UiSelector().packageName("android")
                .textContains("isn't responding."))
        if (window.exists()) {
            var errorText: String? = null
            try {
                errorText = window.text
            } catch (e: UiObjectNotFoundException) {
                // don't care, ignore
            }

            onAnrDetected(errorText)
            postHandler()
            return true
        }
        return false
    }

    fun handleCrash(): Boolean {
        val window = device.findObject(UiSelector()
                .className("com.android.server.am.AppErrorDialog"))
        if (window.exists()) {
            var errorText: String? = null
            try {
                errorText = window.text
            } catch (e: UiObjectNotFoundException) {
                // don't care, ignore
            }

            onCrashDetected(errorText)
            postHandler()
            return true
        }
        return false
    }

    fun handleCrash2(): Boolean {
        val window = device.findObject(UiSelector().packageName("android")
                .textContains("has stopped"))
        if (window.exists()) {
            var errorText: String? = null
            try {
                errorText = window.text
            } catch (e: UiObjectNotFoundException) {
                // don't care, ignore
            }

            onCrashDetected(errorText)
            postHandler()
            return true
        }
        return false
    }

    fun onAnrDetected(errorText: String?) {
        errorText?.let { errors.add(it) }
    }

    fun onCrashDetected(errorText: String?) {
        errorText?.let { errors.add(it) }
    }

    fun reset() {
        errors.clear()
    }

    fun getErrors(): List<String> {
        return Collections.unmodifiableList(errors)
    }

    fun postHandler() {
        val formatedOutput = String.format("UI Exception Message: %-20s\n", device.currentPackageName)
        Log.e(TAG, formatedOutput)

        val buttonOK = device.findObject(UiSelector().text("OK").enabled(true))

        try {
            buttonOK.waitForExists(5000)
            buttonOK.click()
        } catch (e: UiObjectNotFoundException) {
            // don't care, ignore
        }
    }

    companion object {
        private var device: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }
}