package com.yunmoxx.store.protector

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * @author hyvenzhu
 * @version 2021-03-10
 */
class EmptyActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, ProtectorService::class.java))
        finish()
    }
}