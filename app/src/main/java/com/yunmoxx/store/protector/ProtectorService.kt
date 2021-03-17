package com.yunmoxx.store.protector

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.Service
import android.content.*
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.yunmoxx.store.system.IStoreService

/**
 * @author hyvenzhu
 * @version 2021-03-10
 */
class ProtectorService : Service() {

    companion object {
        const val appPackageName = "com.yunmoxx.store"
    }

    private val channelId = "ProtectorService"

    private var iStoreService: IStoreService? = null

    override fun onBind(intent: Intent?): IBinder? = binder

    override fun onCreate() {
        super.onCreate()
        Log.d(channelId, "服务监控：守护进程已启动")
        startForeground()
        registerPackageListener(false)
    }

    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
        startForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (iStoreService == null) {
            startAndBindAppService(mConnection)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        registerPackageListener(true)
    }

    private fun registerPackageListener(unregister: Boolean) {
        if (unregister) {
            unregisterReceiver(packageListener)
        } else {
            val intentFilter = IntentFilter()
            intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
            intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED)
            registerReceiver(packageListener, intentFilter)
        }
    }

    private val packageListener = PackageListener()

    private val mConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            iStoreService = IStoreService.Stub.asInterface(service)
            binder.linkToDeath(deathRecipient, 0)
            Log.d(channelId, "服务监控：已连接主程序服务，正在重启主程序")
            iStoreService?.startApp()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            iStoreService = null
            Log.d(channelId, "服务监控：与主程序服务断开，正在尝试重连")
            startAndBindAppService(this)
        }
    }

    private val binder = object : IStoreService.Stub() {
        override fun startApp() {
            if (iStoreService == null) {
                Log.d(channelId, "服务监控：主程序正在尝试重启守护进程")
            } else {
                Log.d(channelId, "服务监控：主程序正在尝试重启守护进程（守护进程已启动，本次忽略）")
            }
        }
    }

    private val deathRecipient: IBinder.DeathRecipient = object : IBinder.DeathRecipient {
        override fun binderDied() {
            iStoreService?.let {
                it.asBinder().unlinkToDeath(this, 0);
                iStoreService = null
                startAndBindAppService(mConnection)
            }
        }
    }

    private fun startAndBindAppService(connection: ServiceConnection) {
        Log.d(channelId, "服务监控：正在连接主程序服务")
        val appServiceIntent = Intent()
        appServiceIntent.component =
            ComponentName(appPackageName, "com.yunmoxx.store.system.AppService")
        startService(appServiceIntent)
        bindService(appServiceIntent, connection, Context.BIND_IMPORTANT)
    }

    private fun startForeground() {
        createChannelIfNeeded()

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getText(R.string.app_name))
            .setContentText("云摩守护进程正在运行，请勿关闭！！！")
            .setSmallIcon(R.drawable.icon_app_logo)
            .build()

        startForeground(2020, notification)
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "系统通知", IMPORTANCE_HIGH)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}