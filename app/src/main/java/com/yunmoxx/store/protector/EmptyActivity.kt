package com.yunmoxx.store.protector

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

/**
 * @author hyvenzhu
 * @version 2021-03-10
 */
class EmptyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("EmptyActivity", "服务监控：守护进程已启动")
        startService(Intent(this, ProtectorService::class.java))
        finish()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("EmptyActivity", "服务监控：守护进程已启动（本次忽略）")
    }
}