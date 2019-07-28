package com.kernel.colibri.core.models

import android.support.test.InstrumentationRegistry
import android.support.test.uiautomator.*
import android.util.Log
import com.kernel.colibri.Colibri
import com.kernel.colibri.core.Config.TAG
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertThat
import java.util.*

/**
 * UI screen that may be interesting during testing
 */
class Screen {

    lateinit var device: UiDevice
    var parentScreen: Screen? = null
    var parentElement: Element? = null
    var packageName: String = ""
    private lateinit var childScreenList: List<Screen>
    private lateinit var rootObject: UiObject
    private lateinit var signature: String
    lateinit var elementsList: MutableList<Element>
    lateinit var name: String
    var depth = -1
    var id = -1
    var loop = 0

    private var finished = false
    var isFinished: Boolean
        get() {
            if (finished)
                return finished

            for (child in elementsList) {
                if (!child.finished)
                    return false
            }
            finished = true
            return finished
        }
        set(finished) {
            this.finished = finished

            if (this.finished) {
                for (child in elementsList) {
                    child.finished = this.finished
                }
            }
        }

    constructor(parent: Screen?, element: Element?) {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val root = device.findObject(UiSelector().index(0))
        init(parent, element, root)
    }

    constructor(parent: Screen?, element: Element?, root: UiObject) {
        init(parent, element, root)
    }

    fun init(parent: Screen?, element: Element?, root: UiObject) {
        assertThat(root, notNullValue())

        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        parentScreen = parent
        parentElement = element
        rootObject = root
        childScreenList = ArrayList()
        elementsList = ArrayList()
        packageName = root.packageName

        signature = ""
        name = device.currentActivityName
        depth = if (parentScreen == null) 0 else parentScreen!!.depth + 1
        id = -1
        finished = false

        initSignature(rootObject)

        if (!packageName.equals(Colibri.packageName, ignoreCase = true)) {
            return
        }

        var i = 0
        var clickable: UiObject?
        do {
            clickable = device.findObject(UiSelector().clickable(true).instance(i++))
            if (clickable != null && clickable.exists())
                elementsList.add(Element(clickable))
        } while (clickable != null && clickable.exists())

        if (elementsList.size == 0)
            finished = true
    }

    private fun initSignature(uiObject: UiObject?) {
        val conf = Configurator.getInstance()
        val waitForIdleTimeout = conf.waitForIdleTimeout
        val waitForSelectorTimeout = conf.waitForSelectorTimeout
        val actionAcknowledgmentTimeout = conf.actionAcknowledgmentTimeout
        val scrollAcknowledgmentTimeout = conf.scrollAcknowledgmentTimeout
        conf.waitForIdleTimeout = 0L
        conf.waitForSelectorTimeout = 0L
        conf.actionAcknowledgmentTimeout = 0L
        conf.scrollAcknowledgmentTimeout = 0L

        parseSignature(uiObject)

        conf.waitForIdleTimeout = waitForIdleTimeout
        conf.waitForSelectorTimeout = waitForSelectorTimeout
        conf.actionAcknowledgmentTimeout = actionAcknowledgmentTimeout
        conf.scrollAcknowledgmentTimeout = scrollAcknowledgmentTimeout
    }

    private fun parseSignature(uiObject: UiObject?): Boolean {
        if (uiObject == null || !uiObject.exists())
            return false

        if (signature.length > 160)
            return true

        try {
            var classname = ""
            for (tmp in uiObject.className.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                classname = tmp
            }
            signature = "$signature$classname;"

            for (i in 0 until uiObject.childCount) {
                parseSignature(uiObject.getChild(UiSelector().index(i)))
            }
        } catch (e: UiObjectNotFoundException) {
            Log.e(TAG, "UiObjectNotFoundException", e)
            return false
        }

        return true
    }

    override fun toString(): String {
        var str = "name:" + name +
                ", id:" + id +
                ", depth:" + depth +
                ", finished:" + finished +
                ", signature:" + signature +
                ", elements:" + elementsList.size
        for (i in elementsList.indices) {
            val element = elementsList[i]
            str += " " + i + ":" + element.finished
        }
        return str
    }

    override fun equals(o: Any?): Boolean {
        if (o === this) {
            return true
        }
        if (o !is Screen) {
            return false
        }
        val c = o as Screen?
        return this.signature == c!!.signature
    }

    override fun hashCode(): Int {
        var result = device.hashCode()
        result = 31 * result + (parentScreen?.hashCode() ?: 0)
        result = 31 * result + (parentElement?.hashCode() ?: 0)
        result = 31 * result + packageName.hashCode()
        result = 31 * result + childScreenList.hashCode()
        result = 31 * result + rootObject.hashCode()
        result = 31 * result + signature.hashCode()
        result = 31 * result + elementsList.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + depth
        result = 31 * result + id
        result = 31 * result + loop
        result = 31 * result + finished.hashCode()
        return result
    }
}
