package com.hide.videophoto.common

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication
import com.hide.videophoto.common.ext.ctx
import com.hide.videophoto.common.ext.currentTimeInSecond
import com.hide.videophoto.common.ext.logE
import com.hide.videophoto.common.util.*
import com.hide.videophoto.data.model.ActivityModel
import com.hide.videophoto.data.model.AdsConfigModel
import com.hide.videophoto.data.model.AuthModel
import com.hide.videophoto.data.model.EventAppStateChangeModel

class MyApplication : MultiDexApplication(), Application.ActivityLifecycleCallbacks,
    DefaultLifecycleObserver {

    val sharedPref by lazy { SharedPreferencesUtil.customPrefs(ctx) }
    val appSettingsModel by lazy { CommonUtil.getAppSettingsModel(ctx) }
    val adsConfigModel by lazy { AdsConfigModel() }
    val authModel by lazy { AuthModel() }
    val activityModel by lazy { ActivityModel() }
    val eventTracking by lazy { EventTrackingManager.getInstance(this) }
    val adsManager by lazy { AdsManager(ctx) }

    companion object {
        lateinit var instance: MyApplication
            private set
    }

    override fun onCreate() {
        super<MultiDexApplication>.onCreate()
        instance = this

        // Register activity lifecycle callback
        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        // Init mobile ads SDK
        adsManager.initMobileAdSdk()
    }

    override fun onActivityCreated(act: Activity, bundle: Bundle?) {
    }

    override fun onActivityStarted(act: Activity) {
    }

    override fun onActivityResumed(act: Activity) {
    }

    override fun onActivityPaused(act: Activity) {
    }

    override fun onActivityStopped(act: Activity) {
    }

    override fun onActivitySaveInstanceState(act: Activity, bundle: Bundle) {
    }

    override fun onActivityDestroyed(act: Activity) {
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        logE("DefaultLifecycleObserver.ON_START")
        if (authModel.hasLoggedInAlready() && (currentTimeInSecond - appSettingsModel.lastTimeInForeground > 3)) {
            RxBus.publishAppStateChanged(EventAppStateChangeModel())
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        logE("DefaultLifecycleObserver.ON_STOP")
        if (authModel.hasLoggedInAlready()) {
            // Keep last time user is in foreground
            appSettingsModel.lastTimeInForeground = currentTimeInSecond
        }
    }
}