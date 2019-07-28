package com.kernel.colibri.core.behaviors

class CustomBehavior(private val listener: () -> Unit) : BaseBehavior() {
    override fun run() {
        listener.invoke()
    }
}