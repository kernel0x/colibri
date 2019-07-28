package com.kernel.colibri.core.performance

import android.app.ActivityManager
import android.content.Context
import android.support.test.InstrumentationRegistry

object MemInfo {
    fun getProcessMemInfo(pid: Int): android.os.Debug.MemoryInfo {
        val context = InstrumentationRegistry.getContext()
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val pids = IntArray(1)
        pids[0] = pid
        val mems = am.getProcessMemoryInfo(pids)
        return mems[0]
    }
}
