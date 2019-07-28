package com.kernel.colibri

import android.support.test.filters.LargeTest
import android.support.test.rule.GrantPermissionRule
import android.support.test.runner.AndroidJUnit4
import com.kernel.colibri.core.models.Condition
import com.kernel.colibri.core.models.Duration
import com.kernel.colibri.core.strategy.Monkey
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@LargeTest
@RunWith(AndroidJUnit4::class)
class Sample2ColibriTest {

    @get:Rule
    var permissionRule = GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)

    @Test
    fun colibriTest() {
        Colibri.condition(Condition.Builder()
                .randomInputText(arrayOf("borscht", "vodka", "bear"))
                .pause(Duration(500, TimeUnit.MILLISECONDS))
                .build())
                .strategy(Monkey())
                .packageName("com.google.android.youtube")
                .launch()
    }
}
