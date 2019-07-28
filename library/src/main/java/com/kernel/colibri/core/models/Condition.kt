package com.kernel.colibri.core.models

import com.kernel.colibri.core.Config
import com.kernel.colibri.core.behaviors.CustomBehavior
import java.util.*
import kotlin.collections.ArrayList

class Condition(
        val timePause: Duration,
        val randomInputText: Array<String>,
        val ignoredActivity: Array<String>,
        val maxSteps: Int,
        val maxDepth: Int,
        val maxRuntime: Duration,
        val maxScreenLoop: Int,
        val maxScreenshots: Int,
        val listCustomBehavior: ArrayList<CustomBehavior>,
        val captureSteps: Boolean,
        val commonButtons: Array<String>) {

    data class Builder(
            private var timePause: Duration = Config.TIME_PAUSE,
            private var randomInputText: Array<String> = Config.RANDOM_INPUT_TEXT,
            private var ignoredActivity: Array<String> = Config.IGNORED_ACTIVITY,
            private var maxSteps: Int = Config.MAX_STEPS,
            private var maxDepth: Int = Config.MAX_DEPTH,
            private var maxRuntime: Duration = Config.MAX_RUNTIME,
            private var maxScreenLoop: Int = Config.MAX_SCREEN_LOOP,
            private var maxScreenshots: Int = Config.MAX_SCREENSHOTS,
            private var listCustomBehavior: ArrayList<CustomBehavior> = ArrayList(),
            private var captureSteps: Boolean = Config.CAPTURE_STEPS,
            private var commonButtons: Array<String> = Config.COMMON_BUTTONS) {

        fun pause(timePause: Duration) = apply { this.timePause = timePause }
        fun randomInputText(randomInputText: Array<String>) = apply { this.randomInputText = randomInputText }
        fun ignoredActivity(ignoredActivity: Array<String>) = apply { this.ignoredActivity = ignoredActivity }
        fun maxSteps(maxSteps: Int) = apply { this.maxSteps = maxSteps }
        fun maxDepth(maxDepth: Int) = apply { this.maxDepth = maxDepth }
        fun maxRuntime(maxRuntime: Duration) = apply { this.maxRuntime = maxRuntime }
        fun maxScreenLoop(maxScreenLoop: Int) = apply { this.maxScreenLoop = maxScreenLoop }
        fun maxScreenshots(maxScreenshots: Int) = apply { this.maxScreenshots = maxScreenshots }
        fun addCustomBehavior(customBehavior: CustomBehavior) = apply { this.listCustomBehavior.add(customBehavior) }
        fun captureSteps(captureSteps: Boolean) = apply { this.captureSteps = captureSteps }
        fun commonButtons(commonButtons: Array<String>) = apply { this.commonButtons = commonButtons }
        fun build() = Condition(timePause, randomInputText, ignoredActivity, maxSteps, maxDepth, maxRuntime, maxScreenLoop, maxScreenshots, listCustomBehavior, captureSteps, commonButtons)
    }

    override fun toString(): String {
        return "Condition(timePause=$timePause, randomInputText=${Arrays.toString(randomInputText)}, ignoredActivity=${Arrays.toString(ignoredActivity)}, maxSteps=$maxSteps, maxDepth=$maxDepth, maxRuntime=$maxRuntime, maxScreenLoop=$maxScreenLoop, maxScreenshots=$maxScreenshots, listCustomBehavior=$listCustomBehavior, captureSteps=$captureSteps, commonButtons=${Arrays.toString(commonButtons)})"
    }
}