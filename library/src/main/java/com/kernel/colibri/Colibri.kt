package com.kernel.colibri

import android.support.test.InstrumentationRegistry
import android.util.Log
import com.kernel.colibri.core.models.Condition
import com.kernel.colibri.core.Config.TAG
import com.kernel.colibri.core.strategy.Monkey
import com.kernel.colibri.core.strategy.Strategy

object Colibri {

    var packageName = InstrumentationRegistry.getContext().packageName.replace(".test", "")
    private var condition = Condition.Builder().build()
    private var strategy: Strategy = Monkey()

    fun condition(condition: Condition): Colibri {
        this.condition = condition
        this.strategy.condition = condition
        return this
    }

    fun strategy(strategy: Strategy): Colibri {
        this.strategy = strategy
        this.strategy.condition = condition
        return this
    }

    fun packageName(packageName: String): Colibri {
        this.packageName = packageName
        return this
    }

    fun launch() {
        try {
            strategy.run()
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Don't panic! Something went wrong:" + e.message)
        }
    }

}