package com.kernel.colibri.core.performance

import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader

object CpuInfo {

    private var totalCpuTime1 = -1f
    private var processCpuTime1 = -1f

    private val totalCpuTime: Long
        get() {
            var cpuInfos: Array<String>? = null
            try {
                val reader = BufferedReader(InputStreamReader(FileInputStream("/proc/stat")), 1000)
                val load = reader.readLine()
                reader.close()
                cpuInfos = load.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }

            return java.lang.Long.parseLong(cpuInfos!![2]) + java.lang.Long.parseLong(cpuInfos[3]) + java.lang.Long.parseLong(cpuInfos[4]) + java.lang.Long.parseLong(cpuInfos[6]) + java.lang.Long.parseLong(cpuInfos[5]) + java.lang.Long.parseLong(cpuInfos[7]) + java.lang.Long.parseLong(cpuInfos[8])
        }

    fun getProcessCpuRate(pid: Int): Float {

        if (totalCpuTime1 == -1f) {
            totalCpuTime1 = totalCpuTime.toFloat()
            processCpuTime1 = getAppCpuTime(pid).toFloat()
            try {
                Thread.sleep(360)
            } catch (e: Exception) {
            }
        }

        val totalCpuTime2 = totalCpuTime.toFloat()
        val processCpuTime2 = getAppCpuTime(pid).toFloat()

        val cpuRate = 100 * (processCpuTime2 - processCpuTime1) / (totalCpuTime2 - totalCpuTime1)

        totalCpuTime1 = totalCpuTime2
        processCpuTime1 = processCpuTime2

        return cpuRate
    }

    fun getAppCpuTime(pid: Int): Long {
        var cpuInfos: Array<String>? = null
        try {
            val reader = BufferedReader(InputStreamReader(FileInputStream("/proc/$pid/stat")), 1000)
            val load = reader.readLine()
            reader.close()
            cpuInfos = load.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        return java.lang.Long.parseLong(cpuInfos!![13]) + java.lang.Long.parseLong(cpuInfos[14]) + java.lang.Long.parseLong(cpuInfos[15]) + java.lang.Long.parseLong(cpuInfos[16])
    }
}
