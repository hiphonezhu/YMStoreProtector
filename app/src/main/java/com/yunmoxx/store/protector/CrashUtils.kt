package com.yunmoxx.store.protector

import android.annotation.SuppressLint
import android.content.Context
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.sql.Date
import java.text.Format
import java.text.SimpleDateFormat
import java.util.concurrent.Executors

object CrashUtils : Thread.UncaughtExceptionHandler {

    @SuppressLint("SimpleDateFormat")
    private val FORMAT: Format = SimpleDateFormat("MM-dd HH:mm:ss")
    private val FILE_SEP = System.getProperty("file.separator")
    private const val SYMBOL = "********************"
    private const val NEWLINE = "\r\n"
    private lateinit var mContext: Context

    fun init(c: Context) {
        mContext = c
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread?, e: Throwable?) {
        e?.let {
            getTrace(e, AppDroid.getDiskCacheDir(mContext, "log").absolutePath + FILE_SEP.toString() + "crash.log")
            AppDroid.triggerRebirth(mContext, null);
        }
    }

    private fun getTrace(t: Throwable, filePath: String) {
        val info = mContext.packageManager.getPackageInfo(mContext.packageName, 0)
        val stringWriter = StringWriter()
        val time: String = FORMAT.format(Date(System.currentTimeMillis()))
        val writer = PrintWriter(stringWriter)
        stringWriter.append("$SYMBOL CRASH LOG $SYMBOL $NEWLINE" +
                "Time: $time$NEWLINE" +
                "Model: ${android.os.Build.BRAND} ${android.os.Build.MODEL}$NEWLINE" +
                "OSVersion: ${android.os.Build.VERSION.RELEASE}$NEWLINE" +
                "PackageName: ${info.packageName}$NEWLINE" +
                "VersionName: ${info.versionName}$NEWLINE" +
                "VersionCode: ${info.versionCode}$NEWLINE" +
                "${NEWLINE}Info: $NEWLINE")
        t.printStackTrace(writer)
        stringWriter.append("$SYMBOL CRASH LOG END $SYMBOL")
        stringWriter.append(NEWLINE)
        stringWriter.flush()
        val buffer = stringWriter.buffer
        Executors.newSingleThreadExecutor().submit<Boolean> {
            val bw = BufferedWriter(FileWriter(filePath, true))
            bw.write(buffer.toString())
            bw.close()
            return@submit true
        }
    }
}