package com.hide.videophoto.common.util

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.hide.videophoto.BuildConfig
import com.hide.videophoto.R
import com.hide.videophoto.common.ext.*
import com.hide.videophoto.data.interactor.DBInteractor
import com.hide.videophoto.data.model.ActivityModel
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class AdsManager(private val ctx: Context) {

    companion object {
        private const val VALID_TIME_TO_SHOW_NATIVE_AD = 59 * 60 // 1 hour

        private const val NATIVE_AD_UNIT_TEST = "ca-app-pub-3940256099942544/2247696110"
        private const val INTERSTITIAL_AD_UNIT_TEST = "ca-app-pub-3940256099942544/1033173712"
        private const val BANNER_AD_UNIT_TEST = "ca-app-pub-3940256099942544/6300978111"
    }

    private val adRequest by lazy { AdRequest.Builder().build() }
    private var interstitialAd: InterstitialAd? = null
    private var nativeAdHome: NativeAd? = null
    private var nativeAdHomeLoadedAt: Int = 0
    private var didShowNativeAdHome = false
    private var nativeAdAddingOptions: NativeAd? = null
    private var nativeAdAddingOptionsLoadedAt: Int = 0
    private var didShowNativeAdAddingOptions = false
    private var nativeAdExitApp: NativeAd? = null
    private var nativeAdExitAppLoadedAt: Int = 0
    private var didShowNativeAdExitApp = false
    private var disposableAdClick: Disposable? = null
    private val dbInteractor by lazy { DBInteractor(ctx) }

    fun initMobileAdSdk() {
        MobileAds.initialize(ctx) {}
    }

    fun loadNativeAdHome(containerLayout: ViewGroup? = null) {
        if (isLoadedNativeAdForLongTime(nativeAdHomeLoadedAt)) {
            loadNativeAd(ctx.adsConfigModel.adIdNativeHome) { nativeAd ->
                // You must call destroy on old ads when you are done with them,
                // otherwise you will have a memory leak.
                destroyNativeAdHome()
                nativeAdHome = nativeAd

                // Show after loading success
                containerLayout?.also {
                    populateUnifiedNativeAdView(nativeAdHome, containerLayout)
                }

                nativeAdHomeLoadedAt = ctx.currentTimeInSecond
                ctx.logE("Native ad Home loaded: $nativeAdHomeLoadedAt")
            }
        }
    }

    fun loadNativeAdAddingOptions() {
        if (isLoadedNativeAdForLongTime(nativeAdAddingOptionsLoadedAt)) {
            loadNativeAd(ctx.adsConfigModel.adIdNativeAddingOptions) { nativeAd ->
                // You must call destroy on old ads when you are done with them,
                // otherwise you will have a memory leak.
                destroyNativeAdAddingOptions()
                nativeAdAddingOptions = nativeAd

                nativeAdAddingOptionsLoadedAt = ctx.currentTimeInSecond
                ctx.logE("Native ad Adding Options loaded: $nativeAdAddingOptionsLoadedAt")
            }
        }
    }

    fun loadNativeAdExitApp() {
        if (isLoadedNativeAdForLongTime(nativeAdExitAppLoadedAt)) {
            loadNativeAd(ctx.adsConfigModel.adIdNativeExitApp) { nativeAd ->
                // You must call destroy on old ads when you are done with them,
                // otherwise you will have a memory leak.
                destroyNativeAdExitApp()
                nativeAdExitApp = nativeAd

                nativeAdExitAppLoadedAt = ctx.currentTimeInSecond
                ctx.logE("Native ad Exit loaded: $nativeAdExitAppLoadedAt")
            }
        }
    }

    fun showNativeAdHome(containerLayout: ViewGroup) {
        if (ctx.shouldShowAds()) {
            // Show native ad
            if (isLoadedNativeAdForLongTime(nativeAdHomeLoadedAt) && !didShowNativeAdHome) {
                loadNativeAdHome(containerLayout)
            } else {
                nativeAdHome?.run {
                    populateUnifiedNativeAdView(nativeAdHome, containerLayout)

                    didShowNativeAdHome = true
                } ?: run {
                    loadNativeAdHome(containerLayout)
                }
            }
        } else {
            containerLayout.gone()
        }
    }

    fun showNativeAdAddingOptions(containerLayout: ViewGroup) {
        if (ctx.shouldShowAds()) {
            // Show native ad
            if (isLoadedNativeAdForLongTime(nativeAdAddingOptionsLoadedAt) && !didShowNativeAdAddingOptions) {
                loadNativeAdAddingOptions()
            } else {
                nativeAdAddingOptions?.run {
                    populateUnifiedNativeAdView(nativeAdAddingOptions, containerLayout)

                    didShowNativeAdAddingOptions = true
                } ?: run {
                    loadNativeAdAddingOptions()
                }
            }
        } else {
            containerLayout.gone()
        }
    }

    fun showNativeAdExitApp(containerLayout: ViewGroup) {
        if (ctx.shouldShowAds()) {
            // Show native ad
            if (isLoadedNativeAdForLongTime(nativeAdExitAppLoadedAt) && !didShowNativeAdExitApp) {
                containerLayout.gone()
            } else {
                nativeAdExitApp?.run {
                    populateUnifiedNativeAdView(nativeAdExitApp, containerLayout)

                    didShowNativeAdExitApp = true
                }
            }
        } else {
            containerLayout.gone()
        }
    }

    fun destroyNativeAdHome() {
        destroyNativeAd(nativeAdHome)
    }

    fun destroyNativeAdAddingOptions() {
        destroyNativeAd(nativeAdAddingOptions)
    }

    fun destroyNativeAdExitApp() {
        destroyNativeAd(nativeAdExitApp)
    }

    fun showBannerAd(
        container: ViewGroup,
        adId: String?,
        adSize: AdSize = AdSize.MEDIUM_RECTANGLE,
        onAdLoaded: (() -> Unit)? = null
    ) {
        if (ctx.shouldShowAds() && adId?.isNotBlank() == true) {
            // Assign ad id
            val bannerAdId = if (BuildConfig.DEBUG) {
                BANNER_AD_UNIT_TEST
            } else {
                adId
            }

            // Load and show banner ad
            AdView(ctx).apply {
                setAdSize(adSize)
                adUnitId = bannerAdId
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        container.addView(this@apply)
                        onAdLoaded?.invoke()
                    }

                    override fun onAdFailedToLoad(p0: LoadAdError?) {
                        super.onAdFailedToLoad(p0)
                        onAdLoaded?.invoke()
                    }
                }
                loadAd(adRequest)
            }
        }
    }

    fun loadInterstitialAd() {
        if (interstitialAd == null) {
            loadInterstitialAd(ctx.adsConfigModel.adIdInterstitial,
                onAdLoaded = {
                    interstitialAd = it
                    ctx.logE("Loaded interstitial ad")
                },
                onAdFailedToLoad = {
                    interstitialAd = null
                }
            )
        }
    }

    fun showInterstitialAd(activity: AppCompatActivity?, onAdDismissed: () -> Unit) {
        showInterstitialAd(activity, interstitialAd,
            onAdDismissed = {
                interstitialAd = null
                onAdDismissed()
                loadInterstitialAd()

                activity?.adsConfigModel?.lastTimeInterstitialAdShown = ctx.currentTimeInSecond
            },
            onAdShowed = {
                interstitialAd = null
            },
            onAdFailedToShow = {
                interstitialAd = null
                onAdDismissed()
                loadInterstitialAd()
            },
            notEnoughTimeToShow = {
                onAdDismissed()
            },
            adNotLoaded = {
                loadInterstitialAd()
                onAdDismissed()
            },
            adNotActive = {
                onAdDismissed()
            }
        )
    }

    fun release() {
        interstitialAd = null
        nativeAdHome = null
        nativeAdAddingOptions = null
        nativeAdExitApp = null

        nativeAdHomeLoadedAt = 0
        didShowNativeAdHome = false
        nativeAdAddingOptionsLoadedAt = 0
        didShowNativeAdAddingOptions = false
        nativeAdExitAppLoadedAt = 0
        didShowNativeAdExitApp = false

        ctx.adsConfigModel.apply {
            isAdsEnabled = false
            adIdInterstitial = null
            adIdNativeHome = null
            adIdNativeAddingOptions = null
            adIdNativeExitApp = null
//            lastTimeInterstitialAdShown = 0
            minTimeToShowNextInterstitialAd = 0
        }

        disposeAdClick()

        ctx.logE("Release ads")
    }

    private fun loadInterstitialAd(
        adId: String?, onAdLoaded: ((InterstitialAd) -> Unit), onAdFailedToLoad: () -> Unit
    ) {
        if (ctx.shouldShowAds() && adId?.isNotBlank() == true) {
            // Assign ad id
            val interstitialAdId = if (BuildConfig.DEBUG) {
                INTERSTITIAL_AD_UNIT_TEST
            } else {
                adId
            }

            // Load ad
            InterstitialAd.load(ctx, interstitialAdId, adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        onAdFailedToLoad()
                        ctx.logE(adError.message)
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        onAdLoaded(interstitialAd)
                    }
                })
        }
    }

    private fun showInterstitialAd(
        activity: AppCompatActivity?,
        interstitialAd: InterstitialAd?,
        onAdDismissed: () -> Unit,
        onAdShowed: () -> Unit,
        onAdFailedToShow: () -> Unit,
        notEnoughTimeToShow: () -> Unit,
        adNotLoaded: () -> Unit,
        adNotActive: () -> Unit
    ) {
        if (activity?.shouldShowAds() == true) {
            interstitialAd?.run {
                if (isEnoughTimeToShowInterstitialAd()) {
                    fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            ctx.logE("Ad was dismissed.")
                            onAdDismissed()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                            ctx.logE("Ad failed to show.")
                            onAdFailedToShow()
                        }

                        override fun onAdShowedFullScreenContent() {
                            ctx.logE("Ad showed fullscreen content.")
                            onAdShowed()
                        }

                        override fun onAdClicked() {
                            super.onAdClicked()
                            countAdClicked()
                        }
                    }

                    show(activity)
                } else {
                    notEnoughTimeToShow()
                }
            } ?: run {
                adNotLoaded()
            }
        } else {
            adNotActive()
        }
    }

    private fun loadNativeAd(adId: String?, onAdLoaded: (NativeAd) -> Unit) {
        if (ctx.shouldShowAds() && adId?.isNotBlank() == true) {
            // Assign ad id
            val nativeAdId = if (BuildConfig.DEBUG) {
                NATIVE_AD_UNIT_TEST
            } else {
                adId
            }

            // Load native ad
            val adLoader = AdLoader.Builder(ctx, nativeAdId)
                .forNativeAd { nativeAd ->
                    onAdLoaded(nativeAd)
                }
                .withAdListener(object : AdListener() {
                    override fun onAdClicked() {
                        super.onAdClicked()
                        countAdClicked()
                    }
                })
                .build()
            adLoader.loadAd(adRequest)
        }
    }

    /**
     * Populates a [NativeAdView] object with data from a given
     * [NativeAd].
     *
     * @param nativeAd the object containing the ad's assets
     * @param adView the view to be populated
     */
    private fun populateUnifiedNativeAdView(nativeAd: NativeAd?, containerLayout: ViewGroup?) {
        nativeAd?.also {
            containerLayout?.also {
                val adView = LayoutInflater.from(ctx)
                    .inflate(R.layout.layout_native_ad, null, false) as NativeAdView

                adView.visible()

                // Set the media view.
                adView.mediaView = adView.findViewById(R.id.ad_media)

                // Set other ad assets.
                adView.headlineView = adView.findViewById(R.id.ad_headline)
                adView.bodyView = adView.findViewById(R.id.ad_body)
                adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
                adView.iconView = adView.findViewById(R.id.ad_app_icon)
                /*adView.priceView = adView.findViewById(R.id.ad_price)
                adView.starRatingView = adView.findViewById(R.id.ad_stars)
                adView.storeView = adView.findViewById(R.id.ad_store)
                adView.advertiserView = adView.findViewById(R.id.ad_advertiser)*/

                val langCode = Locale.getDefault().language
                when {
                    langCode.equals("ar") || langCode.equals("fa") || langCode.equals("ur") -> {
                        val lblAd = adView.findViewById<TextView>(R.id.lbl_ad)
                        lblAd.setBackgroundResource(R.drawable.bg_ad_text_rtl)
                    }
                }

                // The headline and media content are guaranteed to be in every UnifiedNativeAd.
                (adView.headlineView as TextView).text = nativeAd.headline
                nativeAd.mediaContent?.run {
                    adView.mediaView?.setMediaContent(this)
                }

                // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
                // check before trying to display them.
                if (nativeAd.body == null) {
                    adView.bodyView?.invisible()
                } else {
                    adView.bodyView?.apply {
                        visible()
                        (adView.bodyView as TextView).text = nativeAd.body
                    }
                }

                if (nativeAd.callToAction == null) {
                    adView.callToActionView?.invisible()
                } else {
                    adView.callToActionView?.apply {
                        visible()
                        (adView.callToActionView as TextView).text = nativeAd.callToAction
                    }
                }

                if (nativeAd.icon == null) {
                    adView.iconView?.gone()
                } else {
                    adView.iconView?.apply {
                        visible()
                        (adView.iconView as ImageView).setImageDrawable(
                            nativeAd.icon?.drawable
                        )
                    }
                }

                if (nativeAd.price == null) {
                    adView.priceView?.gone()
                } else {
                    adView.priceView?.apply {
                        visible()
                        (adView.priceView as TextView).text = nativeAd.price
                    }
                }

                if (nativeAd.store == null) {
                    adView.storeView?.gone()
                } else {
                    adView.storeView?.apply {
                        visible()
                        (adView.storeView as TextView).text = nativeAd.store
                    }
                }

                if (nativeAd.starRating == null) {
                    adView.starRatingView?.gone()
                } else {
                    adView.starRatingView?.apply {
                        visible()
                        (adView.starRatingView as RatingBar).rating =
                            nativeAd.starRating!!.toFloat()
                    }
                }

                if (nativeAd.advertiser == null) {
                    adView.advertiserView?.gone()
                } else {
                    adView.advertiserView?.apply {
                        visible()
                        (adView.advertiserView as TextView).text = nativeAd.advertiser
                    }
                }

                // This method tells the Google Mobile Ads SDK that you have finished populating your
                // native ad view with this native ad.
                adView.setNativeAd(nativeAd)

                // Get the video controller for the ad. One will always be provided, even if the ad doesn't
                // have a video asset.
                val vc = nativeAd.mediaContent?.videoController

                // Updates the UI to say whether or not this ad has a video asset.
                if (vc?.hasVideoContent() == true) {
                    // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
                    // VideoController will call methods on this object when events occur in the video
                    // lifecycle.
                    vc.videoLifecycleCallbacks =
                        object : VideoController.VideoLifecycleCallbacks() {
                            override fun onVideoEnd() {
                                // Publishers should allow native ads to complete video playback before
                                // refreshing or replacing them with another ad in the same UI location.
                                super.onVideoEnd()
                            }
                        }
                }

                // Add ad view to container
                containerLayout.apply {
                    visible()
                    removeAllViews()
                    addView(adView)
                }
            }
        }
    }

    private fun destroyNativeAd(nativeAd: NativeAd?) {
        nativeAd?.destroy()
    }

    private fun isLoadedNativeAdForLongTime(loadedTime: Int): Boolean {
        return ctx.currentTimeInSecond - loadedTime >= VALID_TIME_TO_SHOW_NATIVE_AD
    }

    private fun isEnoughTimeToShowInterstitialAd(): Boolean {
        return ctx.run {
            currentTimeInSecond - adsConfigModel.lastTimeInterstitialAdShown >= adsConfigModel.minTimeToShowNextInterstitialAd
        }
    }

    private fun countAdClicked() {
        ctx.activityModel.run {
            // Reset click number if current click time is out of ad session
            if (ctx.currentTimeInSecond - lastTimeAdClicked >= ctx.adsConfigModel.adClickSession) {
                adClickedNumber = 0
            }

            // Count ad clicked
            adClickedNumber += 1

            // Keep the first clicked time
            if (adClickedNumber == 1) {
                lastTimeAdClicked = ctx.currentTimeInSecond
            }

            // Save to db
            updateUserActivities(this)

            // Reach max click in a session so that all ads will be disabled in a day
            if (ctx.activityModel.adClickedNumber == ctx.adsConfigModel.maxAdClickNumberInSession) {
                // Add 1 day to last time clicked var to disable ad in a day
                ctx.activityModel.run {
                    lastTimeAdClicked = ctx.currentTimeInSecond + 60 * 60 * 24
                    adClickedNumber = 0

                    // Save to db
                    updateUserActivities(this)
                }

                // Destroy ads because user has clicked so much
                destroyNativeAdHome()
                destroyNativeAdAddingOptions()
                destroyNativeAdExitApp()

                // Event tracking
                ctx.eventTracking.logEvent(EventTrackingManager.CLICK_AD_TOO_MUCH)
            }
        }
    }

    private fun updateUserActivities(model: ActivityModel) {
        disposeAdClick()
        disposableAdClick = dbInteractor.updateActivity(model)
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    private fun disposeAdClick() {
        if (disposableAdClick?.isDisposed == false) {
            disposableAdClick?.dispose()
        }
    }
}