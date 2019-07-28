package com.kernel.colibri.core.strategy

import com.kernel.colibri.core.models.Condition

interface Strategy {
    var condition: Condition
    fun run()
}