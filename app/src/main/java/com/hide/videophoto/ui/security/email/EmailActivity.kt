package com.hide.videophoto.ui.security.email

import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import com.hide.videophoto.R
import com.hide.videophoto.common.ext.ctx
import com.hide.videophoto.common.ext.eventTracking
import com.hide.videophoto.common.ext.setOnSafeClickListener
import com.hide.videophoto.common.ext.toast
import com.hide.videophoto.common.util.DialogUtil
import com.hide.videophoto.common.util.EventTrackingManager
import com.hide.videophoto.common.util.ExternalStoragePermissionUtil
import com.hide.videophoto.ui.base.BaseActivity

class EmailActivity : BaseActivity<EmailView, EmailPresenterImp>(), EmailView {

    private val txtEmail by lazy { findViewById<EditText>(R.id.txt_email) }
    private val btnConfirm by lazy { findViewById<TextView>(R.id.btn_confirm) }

    private val externalStoragePermissionUtil = ExternalStoragePermissionUtil(self)

    private val textWatcher by lazy {
        object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(text: Editable?) {
                toggleConfirmButton()
            }
        }
    }

    companion object {
        fun start(ctx: Context) {
            Intent(ctx, EmailActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }.run {
                ctx.startActivity(this)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // Listen email text changed
        txtEmail.addTextChangedListener(textWatcher)
    }

    override fun onStop() {
        super.onStop()

        // Remove email text changed
        txtEmail.removeTextChangedListener(textWatcher)
    }

    override fun initView(): EmailView {
        return this
    }

    override fun initPresenter(): EmailPresenterImp {
        return EmailPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_security_email
    }

    override fun initWidgets() {
        // Init toolbar
        enableHomeAsUp {
            finish()
        }

        // Listener
        btnConfirm.setOnSafeClickListener {
            showEmailConfirmDialog()
        }
    }

    override fun onSettingEmailSuccess() {
        // Event tracking
        eventTracking.logEvent(EventTrackingManager.SET_SECURITY_EMAIL)

        toast(R.string.set_recovery_email_success)
        finish()
    }

    override fun onQueryDbError() {
        toast(R.string.err_common)
    }

    private fun showEmailConfirmDialog() {
        val email = txtEmail.text.toString().trim()

        if (email.isNotEmpty()) {
            DialogUtil.showRecoveryEmailReviewDialog(ctx, email) {
                externalStoragePermissionUtil.requestPermission(object :
                    ExternalStoragePermissionUtil.Listener {
                    override fun onGranted() {
                        presenter.setRecoveryEmail(email)
                    }

                    override fun onDenied() {
                        toast(R.string.msg_permission_storage)
                    }
                })
            }
        } else {
            toast(R.string.err_email_empty)
            txtEmail.requestFocus()
        }
    }

    private fun toggleConfirmButton() {
        // Toggle confirm button UI
        if (txtEmail.text.toString().isEmpty()) {
            btnConfirm.setBackgroundResource(R.drawable.btn_grey_round)
        } else {
            btnConfirm.setBackgroundResource(R.drawable.btn_accent_round)
        }
    }
}
