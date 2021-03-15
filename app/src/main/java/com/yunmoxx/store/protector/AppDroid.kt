package com.yunmoxx.store.protector

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.umeng.umcrash.UMCrash

/**
 * @author hyvenzhu
 * @version 2021-03-15
 */
class AppDroid : Application() {

    companion object {
        const val umAppKeyDebug = "604eeea56ee47d382b81ca3c"
        const val umAppKeyRelease = "604eed4bb8c8d45c139b8c26"
    }

    override fun onCreate() {
        super.onCreate()
        UMApp.init(
            this,
            BuildConfig.DEBUG,
            if (BuildConfig.DEBUG) umAppKeyDebug else umAppKeyRelease
        )
        loop()
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            UMCrash.generateCustomLog(e, "YMStoreProtector")
            triggerRebirth(this, null)
        }
    }

    /**
     * 重启应用
     *
     * @param context
     * @param intent
     */
    private fun triggerRebirth(context: Context, intent: Intent?) {
        val targetIntent =
            intent ?: context.packageManager.getLaunchIntentForPackage(context.packageName)
        targetIntent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(targetIntent)
        Runtime.getRuntime().exit(0)
    }

    /**
     * 接管 Looper 的 loop 过程
     */
    private fun loop() {
        Handler(Looper.getMainLooper()).post {
            while (true) {
                try {
                    Looper.loop()
                } catch (e: Exception) {
                    val stack = Log.getStackTraceString(e)
                    if (stack.contains("Toast")
                        || stack.contains("startForegroundService") && (Build.VERSION.SDK_INT == Build.VERSION_CODES.O || Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1)
                    ) {
                        // 屏蔽 7.0 toast bad token 异常
                        // 屏蔽 8.0 8.1 startForegroundService 异常
                    } else {
                        throw e
                    }
                }
            }
        }
    }
}