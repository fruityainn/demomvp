package com.hide.videophoto.ui.security.question

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

class QuestionResetActivity : BaseActivity<QuestionResetView, QuestionResetPresenterImp>(),
    QuestionResetView {

    private val lblQuestion by lazy { findViewById<TextView>(R.id.lbl_question) }
    private val txtAnswer by lazy { findViewById<EditText>(R.id.txt_answer) }
    private val lblAnswerCharacters by lazy { findViewById<TextView>(R.id.lbl_characters) }
    private val lblAnswerError by lazy { findViewById<TextView>(R.id.lbl_wrong_answer) }
    private val btnConfirm by lazy { findViewById<TextView>(R.id.btn_confirm) }

    private val textWatcher by lazy {
        object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(text: Editable?) {
                setAnswerCharacterCounter()
            }
        }
    }

    companion object {
        fun start(ctx: Context) {
            Intent(ctx, QuestionResetActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }.run {
                ctx.startActivity(this)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // Listen answer text changed
        txtAnswer.addTextChangedListener(textWatcher)
    }

    override fun onStop() {
        super.onStop()

        // Remove answer text changed
        txtAnswer.removeTextChangedListener(textWatcher)
    }

    override fun initView(): QuestionResetView {
        return this
    }

    override fun initPresenter(): QuestionResetPresenterImp {
        return QuestionResetPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_reset_password_by_question
    }

    override fun initWidgets() {
        // Init toolbar
        enableHomeAsUp {
            finish()
        }

        // Init questions
        lblQuestion.text = authModel.securityQuestion ?: ""

        setAnswerCharacterCounter()

        // Listener
        btnConfirm.setOnSafeClickListener {
            checkAnswer()
        }
    }

    private fun checkAnswer() {
        val answer = txtAnswer.text.toString().trim().lowercase()

        if (answer == authModel.securityAnswer) {
            // Event tracking
            eventTracking.logEvent(EventTrackingManager.RESET_PASSWORD_BY_QUESTION_SUCCESS)

            PasswordActivity.start(self, true)
        } else {
            lblAnswerError.visible()
            txtAnswer.requestFocus()

            // Event tracking
            eventTracking.logEvent(EventTrackingManager.RESET_PASSWORD_BY_QUESTION_FAIL)
        }
    }

    private fun setAnswerCharacterCounter() {
        lblAnswerCharacters.text = String.format(
            getString(R.string.value_characters),
            txtAnswer.length(),
            resources.getInteger(R.integer.max_length_security_answer)
        )

        // Toggle confirm button UI
        if (txtAnswer.text.toString().isEmpty()) {
            btnConfirm.setBackgroundResource(R.drawable.btn_grey_round)
        } else {
            btnConfirm.setBackgroundResource(R.drawable.btn_accent_round)
        }

        // Hide answer error label
        lblAnswerError.gone()
    }
}
