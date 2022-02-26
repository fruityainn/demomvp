package com.hide.videophoto.ui.security.email

import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import com.hide.videophoto.R
import com.hide.videophoto.common.ext.*
import com.hide.videophoto.common.util.EventTrackingManager
import com.hide.videophoto.ui.base.BaseActivity
import com.hide.videophoto.ui.security.password.PasswordActivity
import kotlin.random.Random

class EmailResetActivity : BaseActivity<EmailResetView, EmailResetPresenterImp>(), EmailResetView {

    private val txtEmail by lazy { findViewById<EditText>(R.id.txt_email) }
    private val lblAnswerError by lazy { findViewById<TextView>(R.id.lbl_wrong_answer) }
    private val btnConfirm by lazy { findViewById<TextView>(R.id.btn_confirm) }

    private val textWatcher by lazy {
        object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(text: Editable?) {
                updateUI()
            }
        }
    }

    companion object {
        fun start(ctx: Context) {
            Intent(ctx, EmailResetActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }.run {
                ctx.startActivity(this)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // Listen answer text changed
        txtEmail.addTextChangedListener(textWatcher)
    }

    override fun onStop() {
        super.onStop()

        // Remove answer text changed
        txtEmail.removeTextChangedListener(textWatcher)
    }

    override fun initView(): EmailResetView {
        return this
    }

    override fun initPresenter(): EmailResetPresenterImp {
        return EmailResetPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_reset_password_by_email
    }

    override fun initWidgets() {
        // Init toolbar
        enableHomeAsUp {
            finish()
        }

        updateUI()

        // Listener
        btnConfirm.setOnSafeClickListener {
            verifyEmail()
        }
    }

    private fun verifyEmail() {
        val email = txtEmail.text.toString().trim().lowercase()
        if (email == authModel.email) {
            // Event tracking
            eventTracking.logEvent(EventTrackingManager.RESET_PASSWORD_BY_EMAIL_SUCCESS)

            PasswordActivity.start(self, true)
        } else {
            lblAnswerError.visible()
            txtEmail.requestFocus()

            // Event tracking
            eventTracking.logEvent(EventTrackingManager.RESET_PASSWORD_BY_EMAIL_FAIL)
        }
    }

    private fun updateUI() {
        // Toggle confirm button UI
        if (txtEmail.text.toString().isEmpty()) {
            btnConfirm.setBackgroundResource(R.drawable.btn_grey_round)
        } else {
            btnConfirm.setBackgroundResource(R.drawable.btn_accent_round)
        }

        // Hide answer error label
        lblAnswerError.gone()
    }

    private fun getRandomPassword(): String {
        val strDigits = "0123456789"
        val password = (1..4).map {
            Random.nextInt(0, strDigits.length)
        }.joinToString("")

        return password
    }
}
