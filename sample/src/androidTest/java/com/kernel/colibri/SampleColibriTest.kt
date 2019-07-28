package com.kernel.colibri

import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import com.kernel.colibri.core.models.Condition
import com.kernel.colibri.core.models.Duration
import com.kernel.colibri.core.strategy.Monkey
import com.kernel.colibri.core.strategy.Strategy
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@LargeTest
@RunWith(AndroidJUnit4::class)
class SampleColibriTest : ColibriTest() {
    override fun getCondition(): Condition {
        return Condition.Builder()
                .randomInputText(arrayOf("borscht", "vodka", "bear"))
                .pause(Duration(500, TimeUnit.MILLISECONDS))
                .build()
    }

    override fun getStrategy(): Strategy {
        return Monkey()
    }

    @Test
    fun colibriTest() {
        launch("com.google.android.youtube")
    }
}
