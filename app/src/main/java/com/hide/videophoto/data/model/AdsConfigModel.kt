package com.hide.videophoto.data.model

class AdsConfigModel : BaseModel() {
    var isAdsEnabled = false
    var adIdInterstitial: String? = null
    var lastTimeInterstitialAdShown: Int = 0 // Second
    var minTimeToShowNextInterstitialAd: Int = 0 // Second
    var adIdNativeHome: String? = null
    var adIdNativeAddingOptions: String? = null
    var adIdNativeExitApp: String? = null
    var maxAdClickNumberInSession: Int = 0
    var adClickSession: Int = 0
}