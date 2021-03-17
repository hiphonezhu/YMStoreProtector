package com.yunmoxx.store.protector

import android.content.Context
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure

/**
 * @author hyvenzhu
 * @version 2021-03-15
 */
object UMApp {

    /**
     * 友盟初始化
     *
     * @param context
     * @param logEnabled 是否输出日志
     * @param umAppKey
     */
    fun init(
        context: Context?,
        logEnabled: Boolean,
        umAppKey: String?,
    ) {
        UMConfigure.init(context, umAppKey, "default", UMConfigure.DEVICE_TYPE_PHONE, null)
        MobclickAgent.setScenarioType(context, MobclickAgent.EScenarioType.E_UM_NORMAL)
        // 选用AUTO页面采集模式
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO)
        UMConfigure.setLogEnabled(logEnabled)
        MobclickAgent.setCatchUncaughtExceptions(true)
    }
}