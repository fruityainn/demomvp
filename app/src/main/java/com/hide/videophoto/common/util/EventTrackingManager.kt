package com.hide.videophoto.common.util

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.hide.videophoto.BuildConfig

class EventTrackingManager {

    companion object {
        const val SET_SECURITY_QUESTION = "set_security_question"
        const val SET_SECURITY_EMAIL = "set_security_email"
        const val SKIP_SECURITY_QUESTION = "skip_security_question"
        const val SKIP_ONBOARD = "skip_onboard"
        const val SHOW_RESET_PASSWORD_DIALOG = "show_reset_password_dialog"
        const val RESET_PASSWORD_BY_QUESTION_SUCCESS = "reset_password_by_question_success"
        const val RESET_PASSWORD_BY_QUESTION_FAIL = "reset_password_by_question_fail"
        const val RESET_PASSWORD_BY_EMAIL_SUCCESS = "reset_password_by_email_success"
        const val RESET_PASSWORD_BY_EMAIL_FAIL = "reset_password_by_email_fail"
        const val RESET_PASSWORD_BY_ACTIVITY_SUCCESS = "reset_password_by_activity_success"
        const val RESET_PASSWORD_BY_ACTIVITY_FAIL = "reset_password_by_activity_fail"
        const val HIDE_VIDEO_ = "hide_video_"
        const val HIDE_PHOTO_ = "hide_photo_"
        const val HIDE_AUDIO_ = "hide_audio_"
        const val HIDE_OTHER_ = "hide_other_"
        const val NOTE_ADDED = "note_added"
        const val NOTE_DELETED = "note_deleted"
        const val NOTE_EXPORTED = "note_exported"
        const val CLICK_HOME_ITEM_VIDEO = "click_home_item_video"
        const val CLICK_HOME_ITEM_PHOTO = "click_home_item_photo"
        const val CLICK_HOME_ITEM_NOTE = "click_home_item_note"
        const val CLICK_HOME_ITEM_OTHER = "click_home_item_other"
        const val CREATE_NEW_FOLDER = "create_new_folder"
        const val UNHIDDEN_TO_HIDE_S_FOLDER = "unhidden_to_hide_s_folder"
        const val UNHIDDEN_TO_ORIGINAL_FOLDER = "unhidden_to_original_folder"
        const val ERROR_GET_FILE_DETAIL = "error_get_file_detail"
        const val FILE_SIZE_10GB = "file_size_10gb"
        const val FILE_SIZE_5GB = "file_size_5gb"
        const val FILE_SIZE_2GB = "file_size_2gb"
        const val FILE_SIZE_1GB = "file_size_1gb"
        const val FILE_SIZE_500MB = "file_size_500mb"
        const val FILE_SIZE_200MB = "file_size_200mb"
        const val FILE_SIZE_100MB = "file_size_100mb"
        const val FILE_SIZE_50MB = "file_size_50mb"
        const val FILE_SIZE_20MB = "file_size_20mb"
        const val FILE_SIZE_10MB = "file_size_10mb"
        const val FILE_SIZE_5MB = "file_size_5mb"
        const val FILE_SIZE_2MB = "file_size_2mb"
        const val FILE_SIZE_1MB = "file_size_1mb"
        const val FILE_SIZE_SMALL = "file_size_small"
        const val CLICK_AD_TOO_MUCH = "click_ad_too_much"

        private var instance: EventTrackingManager? = null
        private var firebaseAnalytics: FirebaseAnalytics? = null

        fun getInstance(ctx: Context): EventTrackingManager {
            if (instance == null) {
                instance = EventTrackingManager()
            }

            if (!BuildConfig.DEBUG) {
                if (firebaseAnalytics == null) {
                    firebaseAnalytics = FirebaseAnalytics.getInstance(ctx)
                }
            }

            return instance ?: EventTrackingManager()
        }
    }

    fun logEvent(event: String) {
        if (!BuildConfig.DEBUG) {
            firebaseAnalytics?.logEvent(event, null)
        }
    }
}