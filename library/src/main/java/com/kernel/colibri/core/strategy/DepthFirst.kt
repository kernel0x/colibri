package com.kernel.colibri.core.strategy

import android.support.test.InstrumentationRegistry
import android.support.test.uiautomator.UiDevice
import android.support.test.uiautomator.UiObjectNotFoundException
import android.util.Log
import com.kernel.colibri.Colibri
import com.kernel.colibri.core.Config
import com.kernel.colibri.core.FileLog
import com.kernel.colibri.core.helpers.ScreenshotHelper
import com.kernel.colibri.core.models.Element
import com.kernel.colibri.core.models.Screen
import com.kernel.colibri.core.performance.TechLog
import java.util.*

open class DepthFirst : BaseStrategy() {

    private lateinit var device: UiDevice
    private lateinit var startTime: Date
    private var depth = 0
    private var steps = 0
    private var depthPeak = 0
    private var loop = 0
    private var scannedScreenList = ArrayList<Screen>()
    private var rootScreen: Screen? = null
    private var lastScreen: Screen? = null
    private var lastInteractedElement: Element? = null
    private var lastInteractedLog = ""
    private var finished = false

    private val isCurrentPackage: Boolean
        get() {
            val currentScreen = Screen(null, null)
            return if (currentScreen.packageName.equals(Colibri.packageName, ignoreCase = true)) isNewScreen(currentScreen) else false
        }

    override fun run() {
        super.run()

        reset()

        if (!launchTargetApp())
            return

        while (!finished) {
            steps++

            if (condition.listCustomBehavior.size > 0) {
                condition.listCustomBehavior.forEach { it.run() }
            }

            var currentScreen = Screen(lastScreen, lastInteractedElement)
            currentScreen.id = scannedScreenList.size + 1

            handleCurrentScreen(currentScreen)

            if (!currentScreen.packageName.equals(Colibri.packageName, true)) {
                FileLog.i(Config.TAG, "{Inspect} screen, in other package: " + currentScreen.packageName)
                handleOtherPackage(currentScreen)
                continue
            }

            var newScreen = true
            for (screen in scannedScreenList) {
                if (screen == currentScreen) {
                    newScreen = false
                    currentScreen = screen
                    depth = currentScreen.depth
                    break
                }
            }

            if (depth == 0) {
                if (rootScreen != null) {
                    Log.i(Config.TAG, "Root screen changed, may be due to app's coachmarks")
                }
                rootScreen = currentScreen
            }

            if (newScreen) {
                handleNewScreen(currentScreen)
                loop = 0
            } else {
                handleOldScreen(currentScreen)
                if (++loop > condition.maxScreenLoop) {
                    Log.i(Config.TAG, "Reached max old screen loop, re-launch target app")
                    loop = 0
                    launchTargetApp()
                    continue
                }
            }

            if (!currentScreen.isFinished) {
                var screen: Screen? = currentScreen
                do {
                    if (screen!!.parentElement != null)
                        screen.parentElement!!.finished = false
                    if (screen.parentScreen != null)
                        screen.parentScreen!!.isFinished = false
                    screen = screen.parentScreen
                } while (screen != null)
            }

            setRandomInputTextToEditText()

            handleNextElement(currentScreen)

            if (currentScreen.isFinished) {
                Log.d(Config.TAG, "Screen[" + currentScreen.id + "] finished")

                var screen: Screen? = currentScreen
                do {
                    if (screen!!.parentElement != null)
                        screen.parentElement!!.finished = true
                    if (screen.parentScreen != null) {
                        if (!screen.parentScreen!!.isFinished)
                            break
                    }
                    screen = screen.parentScreen
                } while (screen != null)

                if (currentScreen === rootScreen) {
                    if (isCurrentPackage) {
                        lastInteractedElement?.let { it.finished = false }
                        rootScreen!!.isFinished = false
                    } else {
                        FileLog.i(Config.TAG, "{Stop} root screen finished, id:" + rootScreen!!.id)
                        finished = true
                    }
                } else {
                    if (isSameScreen(currentScreen)) {
                        FileLog.i(Config.TAG, "{Click} Back")
                        device.pressBack()
                    }
                }
            }

            if (Date().time - startTime.time > condition.maxRuntime.valueAsMs) {
                FileLog.i(Config.TAG, "{Stop} reached max run-time second: " + condition.maxRuntime.value)
                finished = true
            }

            if (ScreenshotHelper.screenshotIndex >= Config.MAX_SCREENSHOTS - 1) {
                FileLog.i(Config.TAG, "{Stop} reached max screenshot files.")
                finished = true
            }

            if (steps >= condition.maxSteps) {
                FileLog.i(Config.TAG, "{Stop} reached max screenshot files.")
                finished = true
            }

            if (++currentScreen.loop > condition.maxScreenLoop) {
                Log.i(Config.TAG, "Reached max screen loop, set screen finished")
                currentScreen.isFinished = true
            }
        }

        FileLog.i(Config.TAG, "Total executed steps:" + steps +
                ", peak depth:" + depthPeak +
                ", detected screens:" + scannedScreenList.size +
                ", screenshot:" + ScreenshotHelper.screenshotIndex)

        val log = String.format("CPU average:%.1f%%, CPU peak:%.1f%%, " + "Memory average (KB):%d, Memory peak (KB):%d",
                TechLog.averageCpu, TechLog.cpuPeak,
                TechLog.averageMemory, TechLog.memPeak)
        FileLog.i(Config.TAG, log)
    }

    open fun handleCurrentScreen(currentScreen: Screen) {
        TechLog.record(currentScreen.name)
    }

    private fun handleNewScreen(currentScreen: Screen) {
        FileLog.i(Config.TAG, "{Inspect} NEW screen, $currentScreen")
        lastInteractedLog = ""
        lastInteractedElement = null
        ScreenshotHelper.takeScreenshots("")

        currentScreen.depth = ++depth
        if (depth > depthPeak)
            depthPeak = depth

        var stop = false
        if (isIgnoreScreen(currentScreen.name)) {
            FileLog.i(Config.TAG, "{Inspect} screen, in ignored list: " + currentScreen.name)
            stop = true
        }
        if (depth >= condition.maxDepth) {
            Log.i(Config.TAG, "Has reached the MaxDepth: " + condition.maxDepth)
            stop = true
        }

        if (stop) {
            currentScreen.elementsList.clear()
            currentScreen.isFinished = true
            scannedScreenList.add(currentScreen)
            FileLog.i(Config.TAG, "{Click} Back")
            device.pressBack()
            device.waitForIdle(condition.timePause.valueAsMs)
        } else {
            scannedScreenList.add(currentScreen)
        }
    }

    private fun handleOldScreen(currentScreen: Screen) {
        FileLog.i(Config.TAG, "{Inspect} OLD screen, $currentScreen")
        if (condition.captureSteps) {
            ScreenshotHelper.takeScreenshots(lastInteractedLog)
            lastInteractedLog = ""
            lastInteractedElement = null
        }
    }

    private fun handleNextElement(currentScreen: Screen) {
        val element = getNextElement(currentScreen) ?: return

        var classname = ""
        var text = ""
        var desc = ""

        try {
            classname = element.uiObject.className
            text = element.uiObject.text
            if (text.length > 25) {
                text = text.substring(0, 21) + "..."
            }
        } catch (e: UiObjectNotFoundException) {
            // don't care, ignore
        }

        try {
            desc = element.uiObject.contentDescription
        } catch (e: UiObjectNotFoundException) {
            // don't care, ignore
        }

        try {
            val bounds = element.uiObject.bounds
            var clazz = ""
            for (tmp in classname.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                clazz = tmp
            }
            lastInteractedLog = when {
                text.isNotEmpty() -> String.format("{Click} %s %s %s", text, clazz, bounds.toShortString())
                desc.isNotEmpty() -> String.format("{Click} %s %s %s", desc, clazz, bounds.toShortString())
                else -> String.format("{Click} %s %s", clazz, bounds.toShortString())
            }

            FileLog.i(Config.TAG, lastInteractedLog)
            lastScreen = currentScreen
            lastInteractedElement = element
            element.finished = true
            element.uiObject.click()
        } catch (e: UiObjectNotFoundException) {
            Log.e(Config.TAG, "UiObjectNotFoundException, failed to test a element")
        }

    }

    private fun handleOtherPackage(currentScreen: Screen) {
        if (isNewScreen(currentScreen)) {
            ScreenshotHelper.takeScreenshots("(" + currentScreen.packageName + ")")
            currentScreen.elementsList.clear()
            currentScreen.isFinished = true
            scannedScreenList.add(currentScreen)
        }

        lastInteractedLog = ""
        lastInteractedElement = null

        if (handleAndroidUi()) {
            FileLog.i(Config.TAG, "Handle Android UI succeeded")
        } else if (handleCommonDialog()) {
            FileLog.i(Config.TAG, "Handle Common UI succeeded")
        } else {
            // not handle, try back
            FileLog.i(Config.TAG, "{Click} Back")
            device.pressBack()
            if (!isInTargetApp) {
                launchTargetApp()
                depth = 0
            }
        }
    }

    private fun getNextElement(currentScreen: Screen): Element? {
        if (currentScreen.isFinished)
            return null
        for (element in currentScreen.elementsList) {
            if (!element.uiObject.exists()) {
                element.finished = true
                continue
            }
            if (!element.finished)
                return element
        }
        return null
    }

    private fun isNewScreen(currentScreen: Screen): Boolean {
        for (screen in scannedScreenList) {
            if (screen == currentScreen) {
                return false
            }
        }
        return true
    }

    private fun reset() {
        startTime = Date()
        depth = 0
        steps = 0
        loop = 0
        depthPeak = 0
        rootScreen = null
        lastScreen = null
        lastInteractedElement = null
        lastInteractedLog = ""
        finished = false
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        scannedScreenList = ArrayList()
    }

    private fun logAllScreenInfo() {
        for (i in scannedScreenList.indices) {
            val screen = scannedScreenList[i]
            Log.d(Config.TAG, "Screen[" + (i + 1) + "] " + screen.toString())
        }
        Log.d(Config.TAG, "Root Screen id: " + rootScreen!!.id)
    }
}

