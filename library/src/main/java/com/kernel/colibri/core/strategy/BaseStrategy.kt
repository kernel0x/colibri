package com.kernel.colibri.core.strategy

import android.content.Intent
import android.os.Bundle
import android.support.test.InstrumentationRegistry
import android.support.test.uiautomator.*
import android.util.Log
import com.kernel.colibri.Colibri
import com.kernel.colibri.core.models.Condition
import com.kernel.colibri.core.Config
import com.kernel.colibri.core.Config.TAG
import com.kernel.colibri.core.Config.TIME_LAUNCH
import com.kernel.colibri.core.FileLog
import com.kernel.colibri.core.Utils.removeAllDirRecursively
import com.kernel.colibri.core.helpers.ScreenshotHelper
import com.kernel.colibri.core.helpers.UiWatcherHelper
import com.kernel.colibri.core.models.Screen
import com.kernel.colibri.core.performance.TechLog
import java.util.*

abstract class BaseStrategy : Strategy {

    override var condition: Condition = Condition.Builder().build()

    private var uiWatcherHelper = UiWatcherHelper()

    val isInTargetApp: Boolean
        get() {
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            val pkg = device.currentPackageName
            return pkg != null && pkg.equals(Colibri.packageName, ignoreCase = true)
        }

    init {
        removeAllDirRecursively(Config.FILE_OUTPUT)
        if (!Config.FILE_OUTPUT.exists()) {
            if (!Config.FILE_OUTPUT.mkdirs()) {
                Log.d(TAG, "Failed to create screenshot folder: " + Config.FILE_OUTPUT.path)
            }
        }

        TechLog.init()

        val conf = Configurator.getInstance()
        conf.actionAcknowledgmentTimeout = 200L
        conf.scrollAcknowledgmentTimeout = 100L
        conf.waitForIdleTimeout = 0L
        conf.waitForSelectorTimeout = 0L
        logConfiguration()

        uiWatcherHelper.registerAnrAndCrashWatchers()
    }

    override fun run() {
        launchHome()
        pause()
    }

    protected fun pause() {
        if (!condition.timePause.isZero) {
            Thread.sleep(condition.timePause.valueAsMs)
        }
    }

    fun launchTargetApp(): Boolean {
        return launchApp(Colibri.packageName)
    }

    private fun launchApp(targetPackage: String): Boolean {
        FileLog.i(TAG, "{Launch} $targetPackage")

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val launcherPackage = device.launcherPackageName
        if (launcherPackage.equals(targetPackage, ignoreCase = true)) {
            launchHome()
            return true
        }
        val context = InstrumentationRegistry.getContext()
        val intent = context.packageManager.getLaunchIntentForPackage(targetPackage)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
            device.wait(Until.hasObject(By.pkg(Colibri.packageName).depth(0)), TIME_LAUNCH.valueAsMs)
        } else {
            val err = String.format("(%s) No launchable Activity.\n", targetPackage)
            Log.e(TAG, err)
            val bundle = Bundle()
            bundle.putString("ERROR", err)
            InstrumentationRegistry.getInstrumentation().finish(1, bundle)
        }
        return true
    }

    fun launchHome() {
        FileLog.i(TAG, "{Press} Home")

        val uidevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        uidevice.pressHome()
        val launcherPackage = uidevice.launcherPackageName
        uidevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), TIME_LAUNCH.valueAsMs)
    }

    private fun logConfiguration() {
        val conf = Configurator.getInstance()

        val log = String.format("ActionAcknowledgmentTimeout:%d," +
                " KeyInjectionDelay:%d, " +
                "ScrollAcknowledgmentTimeout:%d," +
                " WaitForIdleTimeout:%d," +
                " WaitForSelectorTimeout:%d",
                conf.actionAcknowledgmentTimeout,
                conf.keyInjectionDelay,
                conf.scrollAcknowledgmentTimeout,
                conf.waitForIdleTimeout,
                conf.waitForSelectorTimeout)

        FileLog.i(TAG, log)

        FileLog.i(TAG, "TargetPackage: " + Colibri.packageName + ", " + condition.toString())
    }

    fun handleAndroidUi(): Boolean {
        when {
            uiWatcherHelper.handleAnr() -> {
                ScreenshotHelper.takeScreenshots("[ANR]")
                return true
            }
            uiWatcherHelper.handleAnr2() -> {
                ScreenshotHelper.takeScreenshots("[ANR]")
                return true
            }
            uiWatcherHelper.handleCrash() -> {
                ScreenshotHelper.takeScreenshots("[CRASH]")
                return true
            }
            uiWatcherHelper.handleCrash2() -> {
                ScreenshotHelper.takeScreenshots("[CRASH]")
                return true
            }
            else -> {
                // something unknown
            }
        }

        return false
    }

    fun handleCommonDialog(): Boolean {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        var button: UiObject? = null
        for (keyword in condition.commonButtons) {
            button = device.findObject(UiSelector().text(keyword).enabled(true))
            if (button != null && button.exists()) {
                break
            }
        }
        try {
            if (button != null && button.exists()) {
                button.waitForExists(5000)
                button.click()
                Log.i(TAG, "{Click} " + button.text + " Button succeeded")
                return true
            }
        } catch (e: UiObjectNotFoundException) {
            // don't care, ignore
        }

        return false
    }

    fun setRandomInputTextToEditText() {
        if(condition.randomInputText.isEmpty()) return
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        var edit: UiObject?
        var i = 0
        do {
            edit = device.findObject(UiSelector().className("android.widget.EditText").instance(i++))
            if (edit != null && edit.exists()) {
                try {
                    val rand = Random()
                    val text = condition.randomInputText[rand.nextInt(condition.randomInputText.size - 1)]
                    edit.text = text
                } catch (e: UiObjectNotFoundException) {
                    // it happens
                }

            }
        } while (edit != null && edit.exists())
    }

    fun isIgnoreScreen(screen: Screen): Boolean {
        return isIgnoreScreen(screen.name)
    }

    fun isIgnoreScreen(activityName: String): Boolean {
        condition.ignoredActivity.forEach {
            if (it.equals(activityName, true)) return true
        }
        return false
    }

    fun isSameScreen(target: Screen): Boolean {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val root = device.findObject(UiSelector().packageName(Colibri.packageName))
        if (root == null || !root.exists()) {
            Log.e(TAG, "Fail to get screen root object")
            return false
        }
        val current = Screen(null, null, root)
        return current == target
    }
}