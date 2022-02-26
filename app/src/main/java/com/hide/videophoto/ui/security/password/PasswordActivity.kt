package com.hide.videophoto.ui.security.password

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hide.videophoto.R
import com.hide.videophoto.common.Constants
import com.hide.videophoto.common.ext.*
import com.hide.videophoto.common.util.*
import com.hide.videophoto.data.mapper.mergeWith
import com.hide.videophoto.data.model.ActivityModel
import com.hide.videophoto.data.model.AuthModel
import com.hide.videophoto.ui.base.BaseActivity
import com.hide.videophoto.ui.main.MainActivity
import com.hide.videophoto.ui.security.email.EmailResetActivity
import com.hide.videophoto.ui.security.other.OtherActivity
import com.hide.videophoto.ui.security.question.QuestionActivity
import com.hide.videophoto.ui.security.question.QuestionResetActivity
import java.util.*

class PasswordActivity : BaseActivity<PasswordView, PasswordPresenterImp>(), PasswordView {

    private val btnZero by lazy { findViewById<TextView>(R.id.btn_zero) }
    private val btnOne by lazy { findViewById<TextView>(R.id.btn_one) }
    private val btnTwo by lazy { findViewById<TextView>(R.id.btn_two) }
    private val btnThree by lazy { findViewById<TextView>(R.id.btn_three) }
    private val btnFour by lazy { findViewById<TextView>(R.id.btn_four) }
    private val btnFive by lazy { findViewById<TextView>(R.id.btn_five) }
    private val btnSix by lazy { findViewById<TextView>(R.id.btn_six) }
    private val btnSeven by lazy { findViewById<TextView>(R.id.btn_seven) }
    private val btnEight by lazy { findViewById<TextView>(R.id.btn_eight) }
    private val btnNine by lazy { findViewById<TextView>(R.id.btn_nine) }
    private val btnEqual by lazy { findViewById<TextView>(R.id.btn_equal) }
    private val btnDecimal by lazy { findViewById<TextView>(R.id.btn_decimal) }
    private val btnDel by lazy { findViewById<ImageView>(R.id.btn_delete) }
    private val btnAdd by lazy { findViewById<TextView>(R.id.btn_add) }
    private val btnMinus by lazy { findViewById<TextView>(R.id.btn_minus) }
    private val btnMultiply by lazy { findViewById<TextView>(R.id.btn_multiply) }
    private val btnDivide by lazy { findViewById<TextView>(R.id.btn_divide) }
    private val lblExpression by lazy { findViewById<TextView>(R.id.lbl_expression) }
    private val lblResult by lazy { findViewById<TextView>(R.id.lbl_result) }
    private val lblMessage by lazy { findViewById<TextView>(R.id.lbl_message) }
    private val imgClearResult by lazy { findViewById<ImageView>(R.id.img_clear) }

    private val decimalSign by lazy { getString(R.string.decimal_symbol) }
    private val addSign by lazy { getString(R.string.add_sign) }
    private val minusSign by lazy { getString(R.string.minus_sign) }
    private val multiplySign by lazy { getString(R.string.multiply_sign) }
    private val divideSign by lazy { getString(R.string.divide_sign) }

    private val externalStoragePermissionUtil = ExternalStoragePermissionUtil(self)
    private var isOpenedByUser = false
    private var isFromBackground = false

    companion object {
        private const val KEYPAD_ZERO = "0"
        private const val KEYPAD_ONE = "1"
        private const val KEYPAD_TWO = "2"
        private const val KEYPAD_THREE = "3"
        private const val KEYPAD_FOUR = "4"
        private const val KEYPAD_FIVE = "5"
        private const val KEYPAD_SIX = "6"
        private const val KEYPAD_SEVEN = "7"
        private const val KEYPAD_EIGHT = "8"
        private const val KEYPAD_NINE = "9"
        private const val KEYPAD_EQUAL = "equal"
        private const val KEYPAD_DEL = "del"
        private const val MIN_PASSWORD_LENGTH = 4
        private const val PASSWORD_RESET_DIGITS = "123123"

        fun start(
            ctx: AppCompatActivity,
            isOpenedByUser: Boolean = false,
            isFromBackground: Boolean = false
        ) {
            Intent(ctx, PasswordActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(Constants.Key.OPENED_BY_USER, isOpenedByUser)
                putExtra(Constants.Key.IS_FROM_BACKGROUND, isFromBackground)

                if (isFromBackground) {
                    addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                }
            }.run {
                ctx.startActivity(this)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Fetch remote config
        presenter.fetchRemoteConfiguration(self) {
            // Prefetching ads
            adsManager.apply {
                loadNativeAdHome()
            }
        }

        // Check VIP status
        presenter.checkVipStatus()

        // Get extra values
        getExtraValues(intent)

        // Check if has user already
        if (appSettingsModel.hasUserAlready) {
            if (!authModel.hasLoggedInAlready()) {
                getBasicUserInfo()
                getUserActivity()
            }
        } else {
            countUser()
        }

        // Show toolbar if user is changing password
        if (isOpenedByUser) {
            // Show message
            showGuideMessage()

            // Show Back icon
            if (authModel.hasLoggedInAlready()) {
                showToolbarBase()
                enableHomeAsUp {
                    finish()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (isFromBackground) {
            moveTaskToBack(true)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        getExtraValues(intent)

        if (isOpenedByUser) {
            showGuideMessage()
        }
    }

    override fun initView(): PasswordView {
        return this
    }

    override fun initPresenter(): PasswordPresenterImp {
        return PasswordPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_password
    }

    override fun initWidgets() {
        // Init toolbar
        hideToolbarBase()

        // Listener
        btnZero.setOnClickListener {
            onKeypadClicked(KEYPAD_ZERO)
        }

        btnOne.setOnClickListener {
            onKeypadClicked(KEYPAD_ONE)
        }

        btnTwo.setOnClickListener {
            onKeypadClicked(KEYPAD_TWO)
        }

        btnThree.setOnClickListener {
            onKeypadClicked(KEYPAD_THREE)
        }

        btnFour.setOnClickListener {
            onKeypadClicked(KEYPAD_FOUR)
        }

        btnFive.setOnClickListener {
            onKeypadClicked(KEYPAD_FIVE)
        }

        btnSix.setOnClickListener {
            onKeypadClicked(KEYPAD_SIX)
        }

        btnSeven.setOnClickListener {
            onKeypadClicked(KEYPAD_SEVEN)
        }

        btnEight.setOnClickListener {
            onKeypadClicked(KEYPAD_EIGHT)
        }

        btnNine.setOnClickListener {
            onKeypadClicked(KEYPAD_NINE)
        }

        btnDecimal.setOnClickListener {
            onKeypadClicked(decimalSign)
        }

        btnEqual.setOnClickListener {
            onKeypadClicked(KEYPAD_EQUAL)
        }

        btnAdd.setOnClickListener {
            onKeypadClicked(addSign)
        }

        btnMinus.setOnClickListener {
            onKeypadClicked(minusSign)
        }

        btnMultiply.setOnClickListener {
            onKeypadClicked(multiplySign)
        }

        btnDivide.setOnClickListener {
            onKeypadClicked(divideSign)
        }

        btnDel.setOnClickListener {
            onKeypadClicked(KEYPAD_DEL)
        }

        btnDel.setOnLongClickListener {
            clearExpression()
            return@setOnLongClickListener true
        }

        imgClearResult.setOnSafeClickListener {
            showGuideMessage()
            lblResult.text = ""
            imgClearResult.gone()
        }
    }

    override fun onLoggedInSuccess(model: AuthModel) {
        // Keep auth info
        if (authModel.hasLoggedInAlready()) {
            authModel.mergeWith(model)
        } else {
            authModel.mergeWith(model)

            MainActivity.start(ctx)
        }

        // Log user's log in time
        presenter.saveLoginTime()

        // Finish this page
        finish()
    }

    override fun onLoggingInError(password: String) {
        // Going to reset password
        if (password == PASSWORD_RESET_DIGITS) {
            DialogUtil.showResetPasswordOptionDialog(
                ctx,
                question = { dialog ->
                    if (authModel.securityQuestion?.isNotEmpty() == true) {
                        QuestionResetActivity.start(ctx)

                        dialog.dismiss()
                    } else {
                        toast(R.string.err_question_not_set, true)
                    }
                },
                email = { dialog ->
                    if (authModel.email?.isNotEmpty() == true) {
                        EmailResetActivity.start(ctx)

                        dialog.dismiss()
                    } else {
                        toast(R.string.err_email_not_set, true)
                    }
                },
                another = {
                    OtherActivity.start(ctx)
                }
            )

            clearExpression()

            // Event tracking
            eventTracking.logEvent(EventTrackingManager.SHOW_RESET_PASSWORD_DIALOG)
        }
    }

    override fun onPasswordChangedSuccess(newPassword: String) {
        toast(R.string.msg_password_changed_success)
        if (authModel.hasLoggedInAlready()) {
            // Re-assign new password if it's changed but not reset
            authModel.password = newPassword

            finish()
        }
    }

    override fun onCreatedUserSuccess(rowId: Long) {
        // Save auth info
        authModel.rowId = rowId

        // Open Setting security question page
        QuestionActivity.start(ctx)

        // Create activity entity to keep user's activities
        presenter.createActivity()

        // Finish this page
        finish()
    }

    override fun onGettingBasicUserInfoSuccess(model: AuthModel) {
        authModel.mergeWith(model)
    }

    override fun onGettingActivitySuccess(model: ActivityModel) {
        activityModel.mergeWith(model)
    }

    override fun onCountingUserSuccess(count: Int) {
        appSettingsModel.apply {
            hasUserAlready = count > 0
        }.run {
            CommonUtil.saveAppSettingsModel(ctx, this)
        }

        // Show message
        if (appSettingsModel.hasUserAlready) {
            getBasicUserInfo()
            getUserActivity()
        } else {
            showGuideMessage()
        }
    }

    override fun passwordIsValid(): Boolean {
        return lblExpression.text.length >= MIN_PASSWORD_LENGTH
    }

    override fun onPasswordInvalidError() {
        toast(R.string.err_password_invalid, true)
    }

    override fun passwordsAreMatch(): Boolean {
        return lblExpression.text.toString() == lblResult.text.toString()
    }

    override fun onPasswordNotMatchError() {
        toast(R.string.err_password_not_match)
    }

    override fun onQueryDbError() {
        toast(R.string.err_common)
    }

    private fun getExtraValues(intent: Intent?) {
        intent?.run {
            extras?.also { bundle ->
                if (bundle.containsKey(Constants.Key.OPENED_BY_USER)) {
                    isOpenedByUser = bundle.getBoolean(Constants.Key.OPENED_BY_USER)
                }
                if (bundle.containsKey(Constants.Key.IS_FROM_BACKGROUND)) {
                    isFromBackground = bundle.getBoolean(Constants.Key.IS_FROM_BACKGROUND)
                }
            }
        }
    }

    private fun onKeypadClicked(pressedKey: String) {
        when (pressedKey) {
            KEYPAD_DEL -> {
                lblExpression.run {
                    text = if (text.isNotEmpty()) {
                        text.take(text.length - 1)
                    } else {
                        ""
                    }
                }
            }
            KEYPAD_EQUAL -> {
                processEqualAction()
            }
            else -> {
                if (appSettingsModel.hasUserAlready && !isOpenedByUser) {
                    appendExpression(pressedKey)
                } else {
                    if (pressedKey !in listOf(
                            decimalSign, addSign, minusSign, multiplySign, divideSign
                        )
                    ) {
                        appendExpression(pressedKey)
                    }
                }
            }
        }
    }

    private fun processEqualAction() {
        if (appSettingsModel.hasUserAlready && !isOpenedByUser) { // Had user already
            val strExpression = lblExpression.text.toString()
            if (strExpression.contains(decimalSign) || strExpression.contains(addSign)
                || strExpression.contains(minusSign) || strExpression.contains(multiplySign)
                || strExpression.contains(divideSign)
            ) { // Evaluate as a calculator if the expression contains any operator
                try {
                    evaluateExpression()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else { // Else log in here
                login(strExpression)
            }
        } else { // Has no user or changing password
            val password = lblExpression.text.toString()
            val result = lblResult.text.toString()
            if (result.isEmpty()) {
                if (passwordIsValid()) {
                    lblResult.text = password
                    clearExpression()
                    imgClearResult.visible()
                    showGuideMessage(getString(R.string.msg_confirm_password))
                } else {
                    onPasswordInvalidError()
                }
            } else {
                if (appSettingsModel.hasUserAlready) { // Change password
                    changePassword(password)
                } else { // Create new user
                    createUser(password)
                }
            }
        }
    }

    private fun evaluateExpression() {
        var strExpression = lblExpression.text.toString()
        if (strExpression.contains(addSign)) {
            strExpression = strExpression.replace(addSign, "+")
        }
        if (strExpression.contains(minusSign)) {
            strExpression = strExpression.replace(minusSign, "-")
        }
        if (strExpression.contains(multiplySign)) {
            strExpression = strExpression.replace(multiplySign, "*")
        }
        if (strExpression.contains(divideSign)) {
            strExpression = strExpression.replace(divideSign, "/")
        }
        val result = Eval.execute(strExpression)
        val decimal = if (result.toString().contains(".")) {
            result.toString().substring(result.toString().lastIndexOf(".") + 1).length
        } else {
            0
        }
        lblResult.text = NumberUtil.formatNumber(result, decimal)
        clearExpression()
    }

    private fun appendExpression(value: String) {
        val strExpression = lblExpression.text.toString()
        val shouldAppend =
            if (value == addSign || value == minusSign || value == multiplySign || value == divideSign) {
                strExpression.isNotEmpty()
                        && !strExpression.endsWith(addSign)
                        && !strExpression.endsWith(minusSign)
                        && !strExpression.endsWith(multiplySign)
                        && !strExpression.endsWith(divideSign)
                        && !strExpression.endsWith(decimalSign)
            } else if (value == decimalSign) {
                val flag = strExpression.isNotEmpty()
                        && !strExpression.endsWith(addSign)
                        && !strExpression.endsWith(minusSign)
                        && !strExpression.endsWith(multiplySign)
                        && !strExpression.endsWith(divideSign)
                        && !strExpression.endsWith(decimalSign)
                if (flag) {
                    val decimalIndex = if (strExpression.contains(decimalSign)) {
                        strExpression.lastIndexOf(decimalSign)
                    } else {
                        -1
                    }
                    val addIndex = if (strExpression.contains(addSign)) {
                        strExpression.lastIndexOf(addSign)
                    } else {
                        -1
                    }
                    val minusIndex = if (strExpression.contains(minusSign)) {
                        strExpression.lastIndexOf(minusSign)
                    } else {
                        -1
                    }
                    val multiplyIndex = if (strExpression.contains(multiplySign)) {
                        strExpression.lastIndexOf(multiplySign)
                    } else {
                        -1
                    }
                    val divideIndex = if (strExpression.contains(divideSign)) {
                        strExpression.lastIndexOf(divideSign)
                    } else {
                        -1
                    }
                    if (decimalIndex < 0) {
                        true
                    } else {
                        val largestOperatorIndex = listOf(
                            addIndex, minusIndex, multiplyIndex, divideIndex
                        ).maxOrNull() ?: 0
                        if (largestOperatorIndex > 0) {
                            decimalIndex < largestOperatorIndex
                        } else {
                            false
                        }
                    }
                } else {
                    false
                }
            } else {
                true
            }

        if (shouldAppend) {
            lblExpression.append(value)
        }
    }

    private fun login(password: String) {
        externalStoragePermissionUtil.requestPermission(object :
            ExternalStoragePermissionUtil.Listener {
            override fun onGranted() {
                presenter.login(password)
            }

            override fun onDenied() {
                toast(R.string.msg_permission_storage)
            }
        })
    }

    private fun changePassword(newPassword: String) {
        externalStoragePermissionUtil.requestPermission(object :
            ExternalStoragePermissionUtil.Listener {
            override fun onGranted() {
                presenter.changePassword(authModel.password, newPassword)
            }

            override fun onDenied() {
                toast(R.string.msg_permission_storage)
            }
        })
    }

    private fun createUser(password: String) {
        externalStoragePermissionUtil.requestPermission(object :
            ExternalStoragePermissionUtil.Listener {
            override fun onGranted() {
                presenter.createUser(password)
            }

            override fun onDenied() {
                toast(R.string.msg_permission_storage)
            }
        })
    }

    private fun countUser() {
        externalStoragePermissionUtil.requestPermission(object :
            ExternalStoragePermissionUtil.Listener {
            override fun onGranted() {
                presenter.countUser()
            }

            override fun onDenied() {
                toast(R.string.msg_permission_storage)
            }
        })
    }

    private fun getBasicUserInfo() {
        externalStoragePermissionUtil.requestPermission(object :
            ExternalStoragePermissionUtil.Listener {
            override fun onGranted() {
                presenter.getBasicUserInfo()
            }

            override fun onDenied() {
                toast(R.string.msg_permission_storage)
            }
        })
    }

    private fun getUserActivity() {
        externalStoragePermissionUtil.requestPermission(object :
            ExternalStoragePermissionUtil.Listener {
            override fun onGranted() {
                presenter.getActivity()
            }

            override fun onDenied() {
            }
        })
    }

    private fun showGuideMessage(msg: String = getString(R.string.err_password_invalid)) {
        lblMessage.apply {
            visible()
            text = msg
        }
    }

    private fun clearExpression() {
        lblExpression?.text = ""
    }
}
