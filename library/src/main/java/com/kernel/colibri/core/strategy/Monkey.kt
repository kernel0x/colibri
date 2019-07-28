package com.kernel.colibri.core.strategy

import com.kernel.colibri.core.models.Screen

class Monkey : DepthFirst() {
    override fun handleCurrentScreen(currentScreen: Screen) {
        super.handleCurrentScreen(currentScreen)
        currentScreen.elementsList.shuffle()
    }
}