package com.hide.videophoto.ui.security.question

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSpinner
import com.hide.videophoto.R
import com.hide.videophoto.common.Constants
import com.hide.videophoto.common.ext.ctx
import com.hide.videophoto.common.ext.eventTracking
import com.hide.videophoto.common.ext.setOnSafeClickListener
import com.hide.videophoto.common.ext.toast
import com.hide.videophoto.common.util.DialogUtil
import com.hide.videophoto.common.util.EventTrackingManager
import com.hide.videophoto.common.util.ExternalStoragePermissionUtil
import com.hide.videophoto.ui.base.BaseActivity
import com.hide.videophoto.ui.main.MainActivity

class QuestionActivity : BaseActivity<QuestionView, QuestionPresenterImp>(), QuestionView {

    private val spnQuestion by lazy { findViewById<AppCompatSpinner>(R.id.spn_question) }
    private val txtAnswer by lazy { findViewById<EditText>(R.id.txt_answer) }
    private val lblAnswerCharacters by lazy { findViewById<TextView>(R.id.lbl_characters) }
    private val btnConfirm by lazy { findViewById<TextView>(R.id.btn_confirm) }

    private val externalStoragePermissionUtil = ExternalStoragePermissionUtil(self)
    private var isOpenedByUser = false

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
        fun start(ctx: Context, isOpenedByUser: Boolean = false) {
            Intent(ctx, QuestionActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(Constants.Key.OPENED_BY_USER, isOpenedByUser)
            }.run {
                ctx.startActivity(this)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get extra values
        intent?.run {
            extras?.also { bundle ->
                if (bundle.containsKey(Constants.Key.OPENED_BY_USER)) {
                    isOpenedByUser = bundle.getBoolean(Constants.Key.OPENED_BY_USER)
                }
            }
        }

        // Show Back icon
        if (isOpenedByUser) {
            enableHomeAsUp {
                finish()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_skip, menu)
        if (!isOpenedByUser) {
            val skipItem = menu?.findItem(R.id.menu_skip)
            skipItem?.isVisible = true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_skip -> {
                // Event tracking
                eventTracking.logEvent(EventTrackingManager.SKIP_SECURITY_QUESTION)

                // Go to Setting recovery email page
                openHomePage()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun initView(): QuestionView {
        return this
    }

    override fun initPresenter(): QuestionPresenterImp {
        return QuestionPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_security_question
    }

    override fun initWidgets() {
        // Init questions
        ArrayAdapter.createFromResource(
            ctx, R.array.arr_security_question, R.layout.item_security_question
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(R.layout.item_security_question)
            // Apply the adapter to the spinner
            spnQuestion.adapter = adapter
        }

        setAnswerCharacterCounter()

        // Listener
        btnConfirm.setOnSafeClickListener {
            showQuestionConfirmDialog()
        }
    }

    override fun onSettingQuestionSuccess() {
        // Event tracking
        eventTracking.logEvent(EventTrackingManager.SET_SECURITY_QUESTION)

        toast(R.string.set_security_question_success)
        if (isOpenedByUser) {
            // Just finish activity
            finish()
        } else {
            // Go to Home page
            openHomePage()
        }
    }

    override fun onQueryDbError() {
        toast(R.string.err_common)
    }

    private fun showQuestionConfirmDialog() {
        val question = spnQuestion.selectedItem.toString()
        val answer = txtAnswer.text.toString().trim()

        if (answer.isNotEmpty()) {
            DialogUtil.showSecurityQuestionReviewDialog(ctx, question, answer) {
                externalStoragePermissionUtil.requestPermission(object :
                    ExternalStoragePermissionUtil.Listener {
                    override fun onGranted() {
                        presenter.setSecurityQuestion(question, answer)
                    }

                    override fun onDenied() {
                        toast(R.string.msg_permission_storage)
                    }
                })
            }
        } else {
            toast(R.string.err_answer_empty)
            txtAnswer.requestFocus()
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
    }

    private fun openHomePage() {
        MainActivity.start(ctx)

        finish()
    }
}
