package com.yunmoxx.store.protector

import android.app.*
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
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

    private val channelId = "ProtectorService"

    private var iRemoteService: IStoreService? = null

    override fun onBind(intent: Intent?): IBinder? = binder

    override fun onCreate() {
        super.onCreate()
        startForeground()
    }

    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
        startForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (iRemoteService == null) {
            startAndBindAppService(mConnection)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }

    private val mConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            iRemoteService = IStoreService.Stub.asInterface(service)
            Log.d(channelId, "已连接主程序服务，正在重启主程序")
            iRemoteService?.startApp()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            iRemoteService = null
            Log.d(channelId, "与主程序服务断开，正在尝试重连")
            startAndBindAppService(this)
        }
    }

    private val binder = object : IStoreService.Stub() {
        override fun startApp() {
            Log.d(channelId, "正在重启守护进程")
            val intent = Intent(this@ProtectorService, EmptyActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            this@ProtectorService.startActivity(intent)
        }
    }

    private fun startAndBindAppService(connection: ServiceConnection) {
        val appServiceIntent = Intent()
        appServiceIntent.component =
            ComponentName("com.yunmoxx.store", "com.yunmoxx.store.system.AppService")
        startService(appServiceIntent)
        bindService(appServiceIntent, connection, Context.BIND_IMPORTANT)
    }

    private fun startForeground() {
        createChannelIfNeeded()

        val pendingIntent: PendingIntent =
            Intent(this, EmptyActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getText(R.string.app_name))
            .setContentText("云摩守护进程正在运行，请勿关闭！！！")
            .setSmallIcon(R.drawable.icon_app_logo)
            .setContentIntent(pendingIntent)
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