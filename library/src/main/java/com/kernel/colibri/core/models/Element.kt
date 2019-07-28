package com.kernel.colibri.core.models

import android.support.test.uiautomator.UiObject

/**
 * UI element that may be interesting during testing
 */
class Element(var uiObject: UiObject) : Cloneable {
    var finished = false

    @Throws(CloneNotSupportedException::class)
    override fun clone(): Any {
        return super.clone() as Element
    }
}
