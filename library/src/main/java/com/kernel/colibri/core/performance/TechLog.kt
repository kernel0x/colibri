package com.kernel.colibri.core.performance

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.support.test.InstrumentationRegistry
import android.util.Log

import com.kernel.colibri.core.FileLog

import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date

import android.provider.ContactsContract.Directory.PACKAGE_NAME
import com.kernel.colibri.core.Config.TECH_LOG_FILE_NAME
import com.kernel.colibri.core.Config.TAG

object TechLog {

    var memList: ArrayList<Int> = ArrayList()
    var cpuList: ArrayList<Float> = ArrayList()

    var cpuLast = 0f
    var cpuPeak = 0f
    var memLast = 0
    var memPeak = 0

    val averageCpu: Float
        get() {
            if (cpuList.size == 0)
                return 0f

            var total = 0f
            var average = 0f
            for (i in cpuList.indices) {
                total += cpuList[i]
            }
            average = total / cpuList.size
            return average
        }

    val averageMemory: Int
        get() {
            if (memList.size == 0)
                return 0

            var total = 0
            var average = 0
            for (i in memList.indices) {
                total += memList[i]
            }
            average = total / memList.size
            return average
        }

    fun reset() {
        memList.clear()
        cpuList.clear()
        memPeak = 0
        memLast = 0
        cpuPeak = 0f
        cpuLast = 0f
    }

    @Synchronized
    fun init() {
        reset()
        var writer: PrintWriter? = null
        try {
            writer = PrintWriter(OutputStreamWriter(FileOutputStream(TECH_LOG_FILE_NAME, true), "UTF-8"))
            writer.print("\uFEFF") // byte-order marker (BOM)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            writer?.close()
        }
        writeLog("Time,CPU%,Memory(KB),Screen")
    }

    @Synchronized
    fun writeLog(str: String) {
        var writer: PrintWriter? = null
        try {
            writer = PrintWriter(OutputStreamWriter(FileOutputStream(TECH_LOG_FILE_NAME, true), "UTF-8"))
            writer.print(str + "\r\n")
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            writer?.close()
        }
    }

    fun record(screenName: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val list = InstrumentationRegistry.getInstrumentation().uiAutomation.windows
            for (win in list) {
                Log.i(TAG, win.javaClass.name)
                Log.i(TAG, win.javaClass.simpleName)
                Log.i(TAG, win.javaClass.toString())
            }
        }

        val context = InstrumentationRegistry.getContext()
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        val appList = am.runningAppProcesses
        for (app in appList) {
            if (PACKAGE_NAME.equals(app.processName, ignoreCase = true)) {

                cpuLast = CpuInfo.getProcessCpuRate(app.pid)
                cpuList.add(cpuLast)
                if (cpuLast > cpuPeak) cpuPeak = cpuLast

                val mem = MemInfo.getProcessMemInfo(app.pid)
                memLast = mem.totalPss
                memList.add(memLast)
                if (mem.totalPss > memPeak) memPeak = mem.totalPss

                val log = String.format("{Tech} package:%s, cpu:%.1f%%" +
                        ", memory total pss (KB):%d, total private dirty (KB):%d, total shared (KB):%d" +
                        ", dalvik private:%d, dalvik shared:%d, dalvik pss:%d" +
                        ", native private:%d, native shared:%d, native pss:%d" +
                        ", others private:%d, others shared:%d, others pss:%d",
                        app.processName, cpuLast,
                        mem.totalPss, mem.totalPrivateDirty, mem.totalSharedDirty,
                        mem.dalvikPrivateDirty, mem.dalvikSharedDirty, mem.dalvikPss,
                        mem.nativePrivateDirty, mem.nativeSharedDirty, mem.nativePss,
                        mem.otherPrivateDirty, mem.otherSharedDirty, mem.otherPss)

                FileLog.i(TAG, log)

                val sdf = SimpleDateFormat("MM-dd HH:mm:ss")
                writeLog(String.format("%s,%.1f%%,%d,%s", sdf.format(Date()), cpuLast, memLast, screenName))

                break
            }
        }

        return true
    }

}
