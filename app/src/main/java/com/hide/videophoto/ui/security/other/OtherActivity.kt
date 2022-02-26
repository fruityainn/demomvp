package com.hide.videophoto.ui.security.other

import android.content.Context
import android.content.Intent
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.hide.videophoto.R
import com.hide.videophoto.common.Constants
import com.hide.videophoto.common.ext.*
import com.hide.videophoto.common.util.DateTimeUtil
import com.hide.videophoto.common.util.EventTrackingManager
import com.hide.videophoto.common.util.ExternalStoragePermissionUtil
import com.hide.videophoto.ui.base.BaseActivity
import com.hide.videophoto.ui.security.password.PasswordActivity
import java.util.*

class OtherActivity : BaseActivity<OtherView, OtherPresenterImp>(), OtherView {

    private val llQuestions by lazy { findViewById<LinearLayout>(R.id.ll_questions) }
    private val radGrInstallTime by lazy { findViewById<RadioGroup>(R.id.radgr_install_time) }
    private val radGrLoginTime by lazy { findViewById<RadioGroup>(R.id.radgr_login_time) }
    private val radGrLastContent by lazy { findViewById<RadioGroup>(R.id.radgr_last_content) }
    private val lblDesc by lazy { findViewById<TextView>(R.id.lbl_desc) }
    private val btnConfirm by lazy { findViewById<TextView>(R.id.btn_confirm) }

    private val externalStoragePermissionUtil = ExternalStoragePermissionUtil(self)
    private val arrInstallTime by lazy { arrayListOf<Long>() }
    private val arrLoginTime by lazy { arrayListOf<Long>() }
    private val arrLastContent by lazy { arrayListOf<String>() }
    private var installTimeAnswer: Int? = null
    private var loginTimeAnswer: Int? = null
    private var lastContentAnswer: String? = null

    companion object {
        fun start(ctx: Context) {
            Intent(ctx, OtherActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }.run {
                ctx.startActivity(this)
            }
        }
    }

    override fun initView(): OtherView {
        return this
    }

    override fun initPresenter(): OtherPresenterImp {
        return OtherPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_reset_password_by_activity
    }

    override fun initWidgets() {
        // Init toolbar
        enableHomeAsUp {
            finish()
        }

        updateDescription()

        // Init activity questions
        if (canVerify()) {
            initActivityQuestions()
        } else {
            hideActivityQuestions()
        }

        // Listener
        radGrInstallTime.setOnCheckedChangeListener { _, checkedId ->
            val rad = radGrInstallTime.findViewById<RadioButton>(checkedId)
            val index = radGrInstallTime.indexOfChild(rad)
            installTimeAnswer = arrInstallTime[index].toInt()
            updateConfirmButton()
        }

        radGrLoginTime.setOnCheckedChangeListener { _, checkedId ->
            val rad = radGrLoginTime.findViewById<RadioButton>(checkedId)
            val index = radGrLoginTime.indexOfChild(rad)
            loginTimeAnswer = arrLoginTime[index].toInt()
            updateConfirmButton()
        }

        radGrLastContent.setOnCheckedChangeListener { _, checkedId ->
            val rad = radGrLastContent.findViewById<RadioButton>(checkedId)
            val index = radGrLastContent.indexOfChild(rad)
            lastContentAnswer = when (index) {
                0 -> Constants.DataType.VIDEO
                1 -> Constants.DataType.IMAGE
                2 -> Constants.DataType.AUDIO
                else -> Constants.DataType.OTHER
            }
            updateConfirmButton()
        }

        btnConfirm.setOnSafeClickListener {
            verifyAnswer()
        }
    }

    override fun onUpdateActivitySuccess() {
        // Show failed message
        toast(
            String.format(
                getString(R.string.wrong_answer_activity),
                (Constants.MAX_ACTIVITY_VERIFICATION_TIMES - activityModel.failedResetTimes)
            ), true
        )

        // Update desc label
        updateDescription()

        // Hide questions if it's max times
        hideActivityQuestions()
    }

    override fun onQueryDbError() {
        toast(R.string.err_common)
    }

    private fun verifyAnswer() {
        if (installTimeAnswer != null && loginTimeAnswer != null && lastContentAnswer != null) {
            externalStoragePermissionUtil.requestPermission(object :
                ExternalStoragePermissionUtil.Listener {
                override fun onGranted() {
                    activityModel.run {
                        if (installedTime == installTimeAnswer && lastLogin == loginTimeAnswer && lastAddedContent == lastContentAnswer) {
                            // Event tracking
                            eventTracking.logEvent(EventTrackingManager.RESET_PASSWORD_BY_ACTIVITY_SUCCESS)

                            PasswordActivity.start(self, true)
                        } else {
                            // Keep failed times
                            presenter.countFailTimes()

                            // Event tracking
                            eventTracking.logEvent(EventTrackingManager.RESET_PASSWORD_BY_ACTIVITY_FAIL)
                        }
                    }
                }

                override fun onDenied() {
                    toast(R.string.msg_permission_storage)
                }
            })
        } else {
            toast(R.string.err_answer_empty)
        }
    }

    private fun initActivityQuestions() {
        val dateFormat = dateFormat

        // Init install time options
        val installTime = activityModel.installedTime * 1000L
        val calInstall = Calendar.getInstance()
        calInstall.timeInMillis = installTime
        arrInstallTime.add(calInstall.timeInMillis / 1000)
        calInstall.add(Calendar.DATE, -30)
        arrInstallTime.add(calInstall.timeInMillis / 1000)
        calInstall.add(Calendar.DATE, -15)
        arrInstallTime.add(calInstall.timeInMillis / 1000)
        calInstall.add(Calendar.DATE, -18)
        arrInstallTime.add(calInstall.timeInMillis / 1000)
        for (time in arrInstallTime) {
            val rad = layoutInflater.inflate(R.layout.item_activity_question, null) as RadioButton?
            rad?.apply {
                text = DateTimeUtil.convertTimeStampToDate(time, dateFormat)
                radGrInstallTime.addView(rad)
            }
        }

        // Init last login time options
        val loginTime = activityModel.lastLogin * 1000L
        val calLogin = Calendar.getInstance()
        calLogin.timeInMillis = loginTime
        arrLoginTime.add(calLogin.timeInMillis / 1000)
        calLogin.add(Calendar.DATE, -15)
        arrLoginTime.add(calLogin.timeInMillis / 1000)
        calLogin.add(Calendar.DATE, -10)
        arrLoginTime.add(calLogin.timeInMillis / 1000)
        calLogin.add(Calendar.DATE, -15)
        arrLoginTime.add(calLogin.timeInMillis / 1000)
        for (time in arrLoginTime) {
            val rad = layoutInflater.inflate(R.layout.item_activity_question, null) as RadioButton?
            rad?.apply {
                text = DateTimeUtil.convertTimeStampToDate(time, dateFormat)
                radGrLoginTime.addView(rad)
            }
        }

        // Init last added content
        arrLastContent.add(getString(R.string.video))
        arrLastContent.add(getString(R.string.photo))
        arrLastContent.add(getString(R.string.audio))
        arrLastContent.add(getString(R.string.other))
        for (content in arrLastContent) {
            val rad = layoutInflater.inflate(R.layout.item_activity_question, null) as RadioButton?
            rad?.apply {
                text = content
                radGrLastContent.addView(rad)
            }
        }
    }

    private fun updateConfirmButton() {
        if (installTimeAnswer != null && loginTimeAnswer != null && lastContentAnswer != null) {
            btnConfirm.setBackgroundResource(R.drawable.btn_accent_round)
        }
    }

    private fun canVerify(): Boolean {
        return activityModel.failedResetTimes < Constants.MAX_ACTIVITY_VERIFICATION_TIMES
    }

    private fun hideActivityQuestions() {
        if (!canVerify()) {
            llQuestions.gone()
            btnConfirm.gone()
        }
    }

    private fun updateDescription() {
        lblDesc.text = String.format(
            getString(R.string.reset_activities_desc),
            (Constants.MAX_ACTIVITY_VERIFICATION_TIMES - activityModel.failedResetTimes)
        )
    }
}
