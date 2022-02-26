package com.hide.videophoto.data.interactor

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import com.hide.videophoto.common.Constants
import com.hide.videophoto.common.ext.appSettingsModel
import com.hide.videophoto.common.ext.currentTimeInSecond
import com.hide.videophoto.common.ext.logE
import com.hide.videophoto.common.util.CommonUtil
import com.hide.videophoto.common.util.RxBus
import com.hide.videophoto.data.model.EventAppSettingsModel

class IapInteractor(private val ctx: Context) : BaseInteractor(ctx) {

    companion object {
        //        private const val IAP_REMOVE_ADS = "remove_ads"
//        private const val IAP_REMOVE_ADS_SALE_OFF = "remove_ads_sale_off"
        private const val IAP_REMOVE_ADS = "android.test.purchased"
        private const val IAP_REMOVE_ADS_SALE_OFF = "android.test.purchased"
    }

    private val billingClient = BillingClient.newBuilder(ctx)
        .setListener { billingResult, purchases ->
            handlePurchase(billingResult, purchases)
        }
        .enablePendingPurchases()
        .build()

    fun checkVipStatus() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingSetUpResult: BillingResult) {
                if (billingSetUpResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    billingClient.queryPurchaseHistoryAsync(
                        BillingClient.SkuType.INAPP,
                        object : PurchaseHistoryResponseListener {
                            override fun onPurchaseHistoryResponse(
                                billingResult: BillingResult,
                                purchases: MutableList<PurchaseHistoryRecord>?
                            ) {
                                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                                    val purchaseHistoryRecord =
                                        purchases.find {
                                            return@find !it.skus.find { sku ->
                                                sku == IAP_REMOVE_ADS || sku == IAP_REMOVE_ADS_SALE_OFF
                                            }.isNullOrEmpty()
                                        }
                                    purchaseHistoryRecord?.let {
                                        ctx.appSettingsModel.apply {
                                            didRemoveAds = true
                                        }
                                    } ?: run {
                                        ctx.logE("--- SKU not payment: $IAP_REMOVE_ADS")
                                    }

                                    // Keep this to avoid checking many times
                                    ctx.appSettingsModel.apply {
                                        didCheckVipStatus = true
                                    }.run {
                                        CommonUtil.saveAppSettingsModel(ctx, this)
                                    }
                                } else {
                                    ctx.logE("--- SKU not payment: $IAP_REMOVE_ADS")
                                }
                            }

                        })
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }

    fun removeAds(activity: AppCompatActivity) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingSetUpResult: BillingResult) {
                if (billingSetUpResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    val skuList = ArrayList<String>()
                    val iapId =
                        if (activity.currentTimeInSecond > activity.appSettingsModel.saleOffTime) {
                            IAP_REMOVE_ADS
                        } else {
                            IAP_REMOVE_ADS_SALE_OFF
                        }
                    skuList.add(iapId)

                    val params = SkuDetailsParams.newBuilder()
                    params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)

                    billingClient.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
                        // Process the result.
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK
                            && skuDetailsList != null
                        ) {
                            for (skuDetails in skuDetailsList) {
                                if (skuDetails.sku == iapId) {
                                    // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
                                    val flowParams = BillingFlowParams.newBuilder()
                                        .setSkuDetails(skuDetails)
                                        .build()
                                    val responseCode = billingClient.launchBillingFlow(
                                        activity,
                                        flowParams
                                    ).responseCode

                                    if (responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                                        // Update app settings (mark user has removed ads)
                                        updateAppSettingsModel()
                                        break
                                    }
                                }
                            }
                        }
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }

    private fun handlePurchase(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        purchases?.run {
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    for (purchase in purchases) {
                        if (!purchase.isAcknowledged) {
                            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken)
                                .build()
                            billingClient.acknowledgePurchase(acknowledgePurchaseParams) {
                                if (it.responseCode == BillingClient.BillingResponseCode.OK) {
                                    updateAppSettingsModel()
                                }
                            }
                        }
                    }
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    // Handle an error caused by a user cancelling the purchase flow.
                }
                else -> {
                    // Handle any other error codes.
                }
            }
        }
    }

    private fun updateAppSettingsModel() {
        ctx.appSettingsModel.apply {
            didRemoveAds = true
        }.run {
            CommonUtil.saveAppSettingsModel(ctx, this)

            // Publish app settings changed event (ads removed)
            RxBus.publishAppSettingsChanged(EventAppSettingsModel().apply {
                event = Constants.Event.REMOVE_AD
                appSettingsModel = this@run
            })
        }
    }
}