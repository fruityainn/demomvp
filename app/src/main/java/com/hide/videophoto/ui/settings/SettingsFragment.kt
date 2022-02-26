package com.hide.videophoto.ui.settings

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.hide.videophoto.R
import com.hide.videophoto.common.Constants
import com.hide.videophoto.common.ext.*
import com.hide.videophoto.common.util.CommonUtil
import com.hide.videophoto.common.util.DialogUtil
import com.hide.videophoto.common.util.RxBus
import com.hide.videophoto.data.model.EventAppSettingsModel
import com.hide.videophoto.data.model.LanguageModel
import com.hide.videophoto.ui.base.BaseFragment
import com.hide.videophoto.ui.feedback.FeedbackActivity
import com.hide.videophoto.ui.security.email.EmailActivity
import com.hide.videophoto.ui.security.password.PasswordActivity
import com.hide.videophoto.ui.security.question.QuestionActivity

class SettingsFragment : BaseFragment<SettingsView, SettingsPresenterImp>(), SettingsView {

    private lateinit var lblTitle: TextView
    private lateinit var cstRemoveAds: ConstraintLayout
    private lateinit var lblRemoveAds: TextView
    private lateinit var lblSaleOff: TextView
    private lateinit var lblSaleOffTimeLeft: TextView
    private lateinit var lblGeneral: TextView
    private lateinit var btnChangeLanguage: TextView
    private lateinit var lblSecurity: TextView
    private lateinit var btnChangePassword: TextView
    private lateinit var btnSetSecurityQuestion: TextView
    private lateinit var btnSetRecoveryEmail: TextView
    private lateinit var lblOther: TextView
    private lateinit var btnRateApp: TextView
    private lateinit var btnShareApp: TextView
    private lateinit var btnFeedback: TextView
    private lateinit var btnReportTranslation: TextView
    private lateinit var btnFilesAntiLost: TextView
    private lateinit var btnTerms: TextView
    private lateinit var btnAbout: TextView

    private var newContext: Context? = null

    companion object {
        private const val TIME_TO_SALE_OFF = 8 * 60 * 60 // 8 hours
    }

    override fun onResume() {
        super.onResume()
        startSaleOff()
    }

    override fun onPause() {
        super.onPause()
        stopSaleOff()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposeCountdown()
    }

    override fun initView(): SettingsView {
        return this
    }

    override fun initPresenter(): SettingsPresenterImp {
        return SettingsPresenterImp(ctx!!)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_settings
    }

    override fun initWidgets(rootView: View) {
        // Find views
        rootView.run {
            lblTitle = findViewById(R.id.lbl_title)
            cstRemoveAds = findViewById(R.id.cst_remove_ads)
            lblRemoveAds = findViewById(R.id.lbl_remove_ads)
            lblSaleOff = findViewById(R.id.lbl_sale_off)
            lblSaleOffTimeLeft = findViewById(R.id.lbl_time_left)
            lblGeneral = findViewById(R.id.lbl_general)
            btnChangeLanguage = findViewById(R.id.lbl_change_language)
            lblSecurity = findViewById(R.id.lbl_security)
            btnChangePassword = findViewById(R.id.lbl_change_password)
            btnSetSecurityQuestion = findViewById(R.id.lbl_security_question)
            btnSetRecoveryEmail = findViewById(R.id.lbl_recovery_email)
            lblOther = findViewById(R.id.lbl_other)
            btnRateApp = findViewById(R.id.lbl_rate_app)
            btnShareApp = findViewById(R.id.lbl_share_app)
            btnFeedback = findViewById(R.id.lbl_feedback)
            btnReportTranslation = findViewById(R.id.lbl_report_translation)
            btnFilesAntiLost = findViewById(R.id.lbl_anti_lost_note)
            btnTerms = findViewById(R.id.lbl_terms)
            btnAbout = findViewById(R.id.lbl_about)
        }

        // Listeners
        if (ctx?.appSettingsModel?.didRemoveAds == true) {
            cstRemoveAds.gone()
        } else {
            cstRemoveAds.setOnSafeClickListener {
                presenter.removeAds(parentActivity)
            }
        }

        btnChangeLanguage.setOnSafeClickListener {
            val languages = arrayListOf<LanguageModel>()
            val arrLanguages = resources.getStringArray(R.array.arr_language)
            for (language in arrLanguages) {
                val langComponent = language.split(",")
                val languageModel = LanguageModel().apply {
                    code = langComponent[0]
                    name = langComponent[1]
                    isSelected = ctx?.appSettingsModel?.appLanguage == code
                }
                languages.add(languageModel)

                // Mark auto language is selected if user has not set
                if (ctx?.appSettingsModel?.appLanguage == null) {
                    languages[0].isSelected = true
                }
            }

            DialogUtil.showChangingLanguageDialog(ctx, languages, newContext?.resources) {
                if (it.code != ctx?.appSettingsModel?.appLanguage) {
                    newContext = ctx?.updateLocale(ctx!!, it.code)

                    // Refresh UI
                    refreshText(newContext?.resources)

                    // Show message
                    ctx?.toast(
                        newContext?.resources?.getString(R.string.msg_maybe_need_to_restart_app_to_app_changes),
                        true
                    )

                    // Save user's choice and notify to other
                    ctx?.appSettingsModel?.apply {
                        appLanguage = it.code
                    }?.run {
                        CommonUtil.saveAppSettingsModel(ctx, this)

                        // Publish app settings changed event (change language)
                        RxBus.publishAppSettingsChanged(EventAppSettingsModel().apply {
                            event = Constants.Event.CHANGE_LANGUAGE
                            appSettingsModel = this@run
                            resources = newContext?.resources
                        })
                    }
                }
            }
        }

        btnChangePassword.setOnSafeClickListener {
            ctx?.run {
                PasswordActivity.start(parentActivity, true)
            }
        }

        btnSetSecurityQuestion.setOnSafeClickListener {
            ctx?.run {
                QuestionActivity.start(ctx, true)
            }
        }

        btnSetRecoveryEmail.setOnSafeClickListener {
            ctx?.run {
                EmailActivity.start(ctx)
            }
        }

        btnRateApp.setOnSafeClickListener {
            DialogUtil.showRatingDialog(ctx)
        }

        btnShareApp.setOnSafeClickListener {
            CommonUtil.shareText(ctx, Constants.LINK_APP)
        }

        btnFeedback.setOnSafeClickListener {
            FeedbackActivity.start(parentActivity)
        }

        btnReportTranslation.setOnSafeClickListener {
            FeedbackActivity.start(parentActivity, true)
        }

        btnFilesAntiLost.setOnSafeClickListener {
            openActivity(TipsActivity::class.java)
        }

        btnTerms.setOnSafeClickListener {
            CommonUtil.openBrowser(ctx, Constants.LINK_POLICY)
        }

        btnAbout.setOnSafeClickListener {
            DialogUtil.showAboutDialog(ctx)
        }

        presenter.listenAppSettingsChanged()
    }

    override fun onAppSettingsChanged(model: EventAppSettingsModel) {
        when (model.event) {
            Constants.Event.REMOVE_AD -> {
                cstRemoveAds.gone()
            }
        }
    }

    private fun refreshText(resources: Resources?) {
        resources?.run {
            lblTitle.text = getString(R.string.settings)
            lblRemoveAds.text = getString(R.string.remove_ads)
            lblGeneral.text = getString(R.string.general)
            btnChangeLanguage.text = getString(R.string.change_language)
            lblSecurity.text = getString(R.string.security)
            btnChangePassword.text = getString(R.string.change_password)
            btnSetSecurityQuestion.text = getString(R.string.set_security_question)
            btnSetRecoveryEmail.text = getString(R.string.set_recovery_email)
            lblOther.text = getString(R.string.other)
            btnRateApp.text = getString(R.string.rate_app)
            btnShareApp.text = getString(R.string.share_app)
            btnFeedback.text = getString(R.string.feedback_and_suggestion)
            btnReportTranslation.text = getString(R.string.report_incorrect_translation)
            btnFilesAntiLost.text = getString(R.string.files_anti_lost)
            btnTerms.text = getString(R.string.terms_and_policy)
            btnAbout.text = getString(R.string.about_us)
        }
    }

    private fun startSaleOff() {
        ctx?.run {
            if (!appSettingsModel.didRemoveAds) {
                val shouldSaleOff = appSettingsModel.let {
                    it.countAppOpened == 1 || it.countAppOpened == 4 || it.countAppOpened == 8
                            || (it.countAppOpened > 10 && it.countAppOpened % 10 == 0)
                }

                if (shouldSaleOff && !isSaleOff()) {
                    appSettingsModel.apply {
                        saleOffTime = currentTimeInSecond + TIME_TO_SALE_OFF
                        countAppOpened += 1
                    }.run {
                        CommonUtil.saveAppSettingsModel(ctx, this)
                    }
                }

                if (isSaleOff()) {
                    lblSaleOff.visible()
                    lblSaleOffTimeLeft.visible()
                    lblRemoveAds.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_vip,
                        0,
                        0,
                        0
                    )

                    presenter.countdown(appSettingsModel.saleOffTime - currentTimeInSecond,
                        onNext = {
                            lblSaleOffTimeLeft.text = it
                        },
                        onComplete = {
                            lblSaleOff.gone()
                            lblSaleOffTimeLeft.gone()
                            lblRemoveAds.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                R.drawable.ic_vip, 0, R.drawable.ic_chevron_right, 0
                            )
                        }
                    )
                } else {
                    lblSaleOff.gone()
                    lblSaleOffTimeLeft.gone()
                    lblRemoveAds.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_vip, 0, R.drawable.ic_chevron_right, 0
                    )

                    disposeCountdown()
                }
            }
        }
    }

    private fun stopSaleOff() {
        if (ctx?.appSettingsModel?.didRemoveAds != true) {
            if (isSaleOff()) {
                disposeCountdown()
            } else {
                lblSaleOff.gone()
                lblSaleOffTimeLeft.gone()
                lblRemoveAds.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_vip, 0, R.drawable.ic_chevron_right, 0
                )
            }
        }
    }

    private fun isSaleOff(): Boolean {
        return ctx?.let {
            it.currentTimeInSecond < it.appSettingsModel.saleOffTime
        } ?: false
    }

    private fun disposeCountdown() {
        presenter.disposeCountdown()
    }
}