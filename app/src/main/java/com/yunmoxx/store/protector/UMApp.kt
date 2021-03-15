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
     * @param release 是否是线上版本：不可调式&正式环境
     * @param umAppKey
     */
    fun init(
        context: Context?,
        release: Boolean,
        umAppKey: String?,
    ) {
        UMConfigure.init(context, umAppKey, "default", UMConfigure.DEVICE_TYPE_PHONE, null)
        MobclickAgent.setScenarioType(context, MobclickAgent.EScenarioType.E_UM_NORMAL)
        // 选用AUTO页面采集模式
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO)
        UMConfigure.setLogEnabled(!release)
        // 开发阶段，关闭错误日志上报功能
        MobclickAgent.setCatchUncaughtExceptions(release)
    }
}