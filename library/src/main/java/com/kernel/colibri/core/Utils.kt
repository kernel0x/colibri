package com.kernel.colibri.core

import android.app.Activity
import android.support.test.InstrumentationRegistry
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import android.support.test.runner.lifecycle.Stage
import android.view.View
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import java.io.File
import java.io.IOException
import java.util.*

object Utils {
    fun getRandomItem(randomText: Array<String>): String {
        val rand = Random()
        return randomText[rand.nextInt(randomText.size - 1)]
    }

    fun getCurrentActivity(): Activity? {
        var currentActivity: Activity? = null
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
            for (activity in resumedActivities) {
                currentActivity = activity
                break
            }
        }
        return currentActivity
    }

    fun toMatcher(v: View): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            override fun matchesSafely(item: View): Boolean {
                return item === v
            }

            override fun describeTo(description: Description) {
                description.appendText(v.toString())
            }
        }
    }

    fun removeAllDirRecursively(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory)
            for (child in fileOrDirectory.listFiles())
                removeAllDirRecursively(child)

        fileOrDirectory.delete()
    }

    fun saveLogcat() {
        val file = File("${Config.FILE_OUTPUT}/${Config.TAG}.Logcat.log")
        try {
            val newFile = file.createNewFile()
            if (newFile) {
                val cmd = "logcat -d -s -v time -f " + file.absolutePath + " " + Config.TAG
                Runtime.getRuntime().exec(cmd)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}