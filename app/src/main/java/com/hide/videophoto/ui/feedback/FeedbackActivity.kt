package com.hide.videophoto.ui.feedback

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hide.videophoto.R
import com.hide.videophoto.common.ext.ctx
import com.hide.videophoto.common.ext.networkIsConnected
import com.hide.videophoto.common.ext.setOnSafeClickListener
import com.hide.videophoto.common.ext.toast
import com.hide.videophoto.common.util.CommonUtil
import com.hide.videophoto.ui.base.BaseActivity

class FeedbackActivity : BaseActivity<FeedbackView, FeedbackPresenterImp>(), FeedbackView {

    private val txtFeedback by lazy { findViewById<EditText>(R.id.txt_feedback) }
    private val btnSubmit by lazy { findViewById<TextView>(R.id.btn_submit) }

    private var isReportingTranslation = false

    private val textWatcher by lazy {
        object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(text: Editable?) {
                toggleSubmitButton()
            }
        }
    }

    companion object {
        private const val PARAM_IS_REPORTING = "param_is_reporting"

        fun start(ctx: AppCompatActivity, isReportingTranslation: Boolean = false) {
            Intent(ctx, FeedbackActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(PARAM_IS_REPORTING, isReportingTranslation)
            }.run {
                ctx.startActivity(this)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // Listen email text changed
        txtFeedback.addTextChangedListener(textWatcher)
    }

    override fun onStop() {
        super.onStop()

        // Remove email text changed
        txtFeedback.removeTextChangedListener(textWatcher)
    }

    override fun initView(): FeedbackView {
        return this
    }

    override fun initPresenter(): FeedbackPresenterImp {
        return FeedbackPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_feedback
    }

    override fun initWidgets() {
        // Get extra values
        intent?.run {
            extras?.also { bundle ->
                if (bundle.containsKey(PARAM_IS_REPORTING)) {
                    isReportingTranslation = bundle.getBoolean(PARAM_IS_REPORTING)
                }
            }
        }

        // Init toolbar
        enableHomeAsUp {
            finish()
        }
        if (isReportingTranslation) {
            showTitle(R.string.report_incorrect_translation)
            txtFeedback.setHint(R.string.report_incorrect_translation_hint)
        } else {
            showTitle(R.string.feedback_and_suggestion)
        }

        // Show keyboard
        txtFeedback.requestFocus()
        CommonUtil.showKeyboard(ctx)

        // Listener
        btnSubmit.setOnSafeClickListener {
            if (isReportingTranslation) {
                reportTranslation()
            } else {
                sendFeedback()
            }
        }
    }

    override fun onFeedbackSentSuccess() {
        toast(R.string.feedback_thanks)

        finish()
    }

    private fun sendFeedback() {
        val content = txtFeedback.text.toString().trim()
        if (content.isNotBlank()) {
            if (networkIsConnected()) {
                presenter.sendFeedback(content)
            } else {
                toast(R.string.err_network_not_available)
            }
        } else {
            toast(R.string.feedback_desc)
        }
    }

    private fun reportTranslation() {
        val content = txtFeedback.text.toString().trim()
        if (content.isNotBlank()) {
            if (networkIsConnected()) {
                presenter.reportTranslation(content)
            } else {
                toast(R.string.err_network_not_available)
            }
        } else {
            toast(R.string.feedback_desc)
        }
    }

    private fun toggleSubmitButton() {
        // Toggle confirm button UI
        if (txtFeedback.text.toString().isEmpty()) {
            btnSubmit.setBackgroundResource(R.drawable.btn_grey_round)
        } else {
            btnSubmit.setBackgroundResource(R.drawable.btn_accent_round)
        }
    }
}
