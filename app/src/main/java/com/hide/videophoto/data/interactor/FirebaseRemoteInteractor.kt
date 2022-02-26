package com.hide.videophoto.data.interactor

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.hide.videophoto.common.ext.adsConfigModel
import com.hide.videophoto.common.ext.ctx

private const val IS_ADS_ENABLED = "is_ads_enabled"
private const val INTERSTITIAL_AD = "interstitial_ad"
private const val NATIVE_AD_HOME = "native_ad_home"
private const val NATIVE_AD_ADDING_OPTIONS = "native_ad_adding_options"
private const val NATIVE_AD_EXIT_APP = "native_ad_exit_app"
private const val AD_CLICK_SESSION = "ad_click_session"
private const val MAX_AD_CLICK_NUMBER_IN_SESSION = "max_ad_click_number_in_session"

private const val STATUS_ON = "1"

class FirebaseRemoteInteractor(ctx: Context) : BaseInteractor(ctx) {

    private val remoteConfig by lazy {
        Firebase.remoteConfig.apply {
            remoteConfigSettings {
                minimumFetchIntervalInSeconds = 1 // 1s
            }.run {
                setConfigSettingsAsync(this)
            }
        }
    }

    fun fetchConfig(activity: AppCompatActivity, onComplete: (() -> Unit)? = null) {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    // Fill ads config into ads model
                    activity.ctx.adsConfigModel.apply {
                        with(remoteConfig) {
                            // Get ads status(On/Off)
                            isAdsEnabled = getBoolean(IS_ADS_ENABLED)

                            // Get interstitial ad info
                            val strInterstitial = getString(INTERSTITIAL_AD)
                            if (strInterstitial.contains(":")) {
                                val arr = strInterstitial.split(":")
                                if (arr[0] == STATUS_ON) {
                                    try {
                                        minTimeToShowNextInterstitialAd = arr[1].toInt()
                                    } catch (e: Exception) {
                                    }
                                    adIdInterstitial = arr[2]
                                }
                            }

                            // Get native ad home info
                            val strNativeAdHome = getString(NATIVE_AD_HOME)
                            if (strNativeAdHome.contains(":")) {
                                val arr = strNativeAdHome.split(":")
                                if (arr[0] == STATUS_ON) {
                                    adIdNativeHome = arr[1]
                                }
                            }

                            // Get native ad Adding options info
                            val strNativeAdAddingOptions = getString(NATIVE_AD_ADDING_OPTIONS)
                            if (strNativeAdAddingOptions.contains(":")) {
                                val arr = strNativeAdAddingOptions.split(":")
                                if (arr[0] == STATUS_ON) {
                                    adIdNativeAddingOptions = arr[1]
                                }
                            }

                            // Get native ad Exit app info
                            val strNativeAdExitApp = getString(NATIVE_AD_EXIT_APP)
                            if (strNativeAdExitApp.contains(":")) {
                                val arr = strNativeAdExitApp.split(":")
                                if (arr[0] == STATUS_ON) {
                                    adIdNativeExitApp = arr[1]
                                }
                            }

                            // Get ad click config
                            adClickSession = getLong(AD_CLICK_SESSION).toInt()
                            maxAdClickNumberInSession =
                                getLong(MAX_AD_CLICK_NUMBER_IN_SESSION).toInt()
                        }
                    }
                }
                onComplete?.invoke()
            }
            .addOnCanceledListener {
                onComplete?.invoke()
            }
            .addOnFailureListener {
                onComplete?.invoke()
                it.printStackTrace()
            }
    }
}