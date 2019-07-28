package com.kernel.colibri

import android.support.test.rule.GrantPermissionRule
import com.kernel.colibri.core.models.Condition
import com.kernel.colibri.core.Utils.saveLogcat
import com.kernel.colibri.core.strategy.Strategy
import org.junit.After
import org.junit.Rule

abstract class ColibriTest {

    @get:Rule
    var permissionRule = GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)

    abstract fun getCondition(): Condition

    abstract fun getStrategy(): Strategy

    open fun launch() {
        Colibri.condition(getCondition())
                .strategy(getStrategy())
                .launch()
    }

    open fun launch(packageName: String) {
        Colibri.condition(getCondition())
                .strategy(getStrategy())
                .packageName(packageName)
                .launch()
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        saveLogcat()
    }

}
