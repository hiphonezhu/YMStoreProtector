package com.yunmoxx.store.protector

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import java.io.File

/**
 * @author hyvenzhu
 * @version 2021-03-15
 */
class AppDroid : Application() {

    companion object {
        const val umAppKeyDebug = "604eeea56ee47d382b81ca3c"
        const val umAppKeyRelease = "604eed4bb8c8d45c139b8c26"

        /**
         * 重启应用
         *
         * @param context
         * @param intent
         */
        fun triggerRebirth(context: Context, intent: Intent?) {
            val targetIntent =
                intent ?: context.packageManager.getLaunchIntentForPackage(context.packageName)
            targetIntent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(targetIntent)
            Runtime.getRuntime().exit(0)
        }

        /**
         * 获得磁盘缓存目录 [PS：应用卸载后会被自动删除]
         *
         * @param context
         * @param uniqueName
         * @return
         */
        fun getDiskCacheDir(context: Context, uniqueName: String): File {
            val cachePath: String
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable()) {
                var dir = context.applicationContext.externalCacheDir
                if (dir == null) {
                    dir = context.applicationContext.cacheDir
                }
                cachePath = dir!!.path
            } else {
                val dir = context.applicationContext.cacheDir
                cachePath = dir.path
            }
            val dir: File
            dir = if (TextUtils.isEmpty(uniqueName)) {
                File(cachePath)
            } else {
                File(cachePath + File.separator + uniqueName)
            }
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }
    }

    override fun onCreate() {
        super.onCreate()
        val releaseMode = "release" == BuildConfig.BUILD_TYPE
        UMApp.init(this, releaseMode, if (releaseMode) umAppKeyRelease else umAppKeyDebug)
        loop()
        CrashUtils.init(this)
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