package com.kernel.colibri.core

import android.util.Log
import com.kernel.colibri.core.Config.LOG_FILE_NAME
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*

object FileLog {
    @Synchronized
    private fun write(type: Char, tag: String, msg: String) {
        val sdf = SimpleDateFormat("MM-dd HH:mm:ss")
        var writer: PrintWriter? = null
        try {
            writer = PrintWriter(BufferedWriter(FileWriter(LOG_FILE_NAME, true)))
            writer.println(String.format("%s %c/%s: %s", sdf.format(Date()), type, tag, msg))
        } catch (e: IOException) {
            // well now sad
        } finally {
            writer?.close()
        }
    }

    @JvmStatic
    fun v(tag: String, msg: String): Int {
        write('v', tag, msg)
        return Log.v(tag, msg)
    }

    @JvmStatic
    fun w(tag: String, msg: String): Int {
        write('w', tag, msg)
        return Log.w(tag, msg)
    }

    @JvmStatic
    fun e(tag: String, msg: String): Int {
        write('e', tag, msg)
        return Log.e(tag, msg)
    }

    @JvmStatic
    fun i(tag: String, msg: String): Int {
        write('i', tag, msg)
        return Log.i(tag, msg)
    }

    @JvmStatic
    fun d(tag: String, msg: String): Int {
        write('d', tag, msg)
        return Log.d(tag, msg)
    }
}
