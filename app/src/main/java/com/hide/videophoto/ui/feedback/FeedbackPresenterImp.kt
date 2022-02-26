package com.hide.videophoto.ui.feedback

import android.content.Context
import android.os.Build
import com.google.firebase.firestore.FirebaseFirestore
import com.hide.videophoto.BuildConfig
import com.hide.videophoto.common.ext.currentTimeInSecond
import com.hide.videophoto.common.util.DateTimeUtil
import com.hide.videophoto.ui.base.BasePresenterImp
import java.util.*
import kotlin.collections.HashMap

class FeedbackPresenterImp(private val ctx: Context) : BasePresenterImp<FeedbackView>(ctx) {

    fun sendFeedback(content: String) {
        view?.also { v ->
            showProgressDialog()

            val appVersion = BuildConfig.VERSION_NAME
            val osVersion = Build.VERSION.RELEASE
            val time = DateTimeUtil.convertTimeStampToDate(
                ctx.currentTimeInSecond, DateTimeUtil.YYYYMMDD_HHMMSS
            )
            val device = "${Build.MANUFACTURER}-${Build.MODEL}"
            val language = "${Locale.getDefault().displayLanguage}-${Locale.getDefault().language}"
            val user = "$appVersion-$time-$device"
            val data = HashMap<String, String>().apply {
                put("feedback", content)
                put("app_version", appVersion)
                put("device", device)
                put("os_version", osVersion)
                put("language", language)
                put("time", time)
            }

            val fireStore = FirebaseFirestore.getInstance()
            fireStore.collection("Feedback").document(user)
                .set(data)
                .addOnSuccessListener {
                    dismissProgressDialog()
                    v.onFeedbackSentSuccess()
                }
                .addOnFailureListener {
                    dismissProgressDialog()
                    v.onFeedbackSentSuccess()
                    it.printStackTrace()
                }
        }
    }

    fun reportTranslation(content: String) {
        view?.also { v ->
            showProgressDialog()

            val appVersion = BuildConfig.VERSION_NAME
            val osVersion = Build.VERSION.RELEASE
            val time = DateTimeUtil.convertTimeStampToDate(
                ctx.currentTimeInSecond, DateTimeUtil.YYYYMMDD_HHMMSS
            )
            val device = "${Build.MANUFACTURER}-${Build.MODEL}"
            val language = "${Locale.getDefault().displayLanguage}-${Locale.getDefault().language}"
            val user = "$appVersion-$time-$device"
            val data = HashMap<String, String>().apply {
                put("translation", content)
                put("app_version", appVersion)
                put("device", device)
                put("os_version", osVersion)
                put("language", language)
                put("time", time)
            }

            val fireStore = FirebaseFirestore.getInstance()
            fireStore.collection("Translation").document(user)
                .set(data)
                .addOnSuccessListener {
                    dismissProgressDialog()
                    v.onFeedbackSentSuccess()
                }
                .addOnFailureListener {
                    dismissProgressDialog()
                    v.onFeedbackSentSuccess()
                    it.printStackTrace()
                }
        }
    }
}