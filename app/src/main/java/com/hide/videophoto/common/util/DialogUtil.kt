package com.hide.videophoto.common.util


import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hide.videophoto.BuildConfig
import com.hide.videophoto.R
import com.hide.videophoto.common.Constants
import com.hide.videophoto.common.ext.*
import com.hide.videophoto.data.mapper.getThumbnail
import com.hide.videophoto.data.model.FileModel
import com.hide.videophoto.data.model.LanguageModel
import com.hide.videophoto.ui.main.ChoosingFolderAdapter
import com.hide.videophoto.ui.settings.ChangingLangueAdapter

object DialogUtil {

    fun showConfirmationDialog(
        ctx: Context?,
        textTitle: Any? = null,
        textMessage: Any,
        textOk: Any = ctx?.getString(R.string.ok) ?: "",
        textCancel: Any? = null,
        okListener: (() -> Unit)? = null,
        cancelListener: (() -> Unit)? = null,
        cancelable: Boolean = true
    ) {
        ctx?.run {
            createDialog(ctx, R.layout.dialog_confirmation, cancelable).run {
                val lblTitle = findViewById<TextView>(R.id.lbl_title)
                val lblMessage = findViewById<TextView>(R.id.lbl_message)
                val btnOk = findViewById<TextView>(R.id.btn_ok)
                val btnCancel = findViewById<TextView>(R.id.btn_cancel)

                textTitle?.let {
                    lblTitle.visible()
                    lblTitle.text = when (it) {
                        is String -> it
                        is CharSequence -> it
                        is Int -> ctx.getString(it)
                        else -> ""
                    }
                }

                lblMessage.text = when (textMessage) {
                    is String -> textMessage
                    is CharSequence -> textMessage
                    is Int -> ctx.getString(textMessage)
                    else -> ""
                }

                btnOk.text = when (textOk) {
                    is String -> textOk
                    is CharSequence -> textOk
                    is Int -> ctx.getString(textOk)
                    else -> ""
                }
                btnOk.setOnSafeClickListener {
                    if (isShowing) {
                        dismiss()
                        okListener?.invoke()
                    }
                }

                val strCancel = when (textCancel) {
                    is String -> textCancel
                    is CharSequence -> textCancel
                    is Int -> ctx.getString(textCancel)
                    else -> ""
                }
                if (strCancel.isEmpty() || strCancel.isBlank()) {
                    btnCancel.visibility = View.GONE
                } else {
                    btnCancel.text = strCancel
                    btnCancel.setOnSafeClickListener {
                        if (isShowing) {
                            dismiss()
                            cancelListener?.invoke()
                        }
                    }
                }

                if (!isShowing) {
                    show()
                }
            }
        }
    }

    fun showNewFolderDialog(ctx: Context?, okListener: (Dialog, String) -> Unit) {
        ctx?.run {
            createDialog(ctx, R.layout.dialog_new_folder, true).run {
                val txtName = findViewById<AppCompatEditText>(R.id.txt_folder_name)
                val btnOk = findViewById<TextView>(R.id.btn_ok)
                val btnCancel = findViewById<TextView>(R.id.btn_cancel)

                txtName.requestFocus()
                CommonUtil.showKeyboard(ctx)

                btnOk.setOnSafeClickListener {
                    if (isShowing) {
                        val name = txtName.text.toString().trim()
                        if (name.isNotBlank()) {
                            // Close keyboard
                            val inputMethodManager =
                                ctx.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                            inputMethodManager.hideSoftInputFromWindow(txtName.windowToken, 0)

                            // Do something
                            okListener(this, name)
                        } else {
                            ctx.toast(R.string.err_folder_name_empty)
                        }
                    }
                }

                btnCancel.setOnSafeClickListener {
                    if (isShowing) {
                        dismiss()
                    }
                }

                if (!isShowing) {
                    show()
                }
            }
        }
    }

    fun showRenameDialog(ctx: Context?, model: FileModel, okListener: (Dialog, String) -> Unit) {
        ctx?.run {
            createDialog(ctx, R.layout.dialog_rename, true).run {
                val txtName = findViewById<AppCompatEditText>(R.id.txt_name)
                val lblExt = findViewById<TextView>(R.id.lbl_ext)
                val btnOk = findViewById<TextView>(R.id.btn_ok)
                val btnCancel = findViewById<TextView>(R.id.btn_cancel)

                with(model) {
                    txtName.setText(name)
                    if (isDirectory) {
                        txtName.hint = ctx.getString(R.string.folder_name)
                        txtName.setText(name)
                    } else {
                        txtName.hint = ctx.getString(R.string.file_name)
                        getExtension()?.run {
                            txtName.setText(name.substringBeforeLast("."))
                            lblExt.apply {
                                visible()
                                text = getExtension()
                            }
                        } ?: run {
                            txtName.setText(name)
                        }
                    }
                }
                txtName.requestFocus()
                CommonUtil.showKeyboard(ctx)

                btnOk.setOnSafeClickListener {
                    if (isShowing) {
                        val name = txtName.text.toString().trim()
                        if (name.isNotBlank()) {
                            okListener(this, name)
                        } else {
                            val msg = if (model.isDirectory) {
                                R.string.err_folder_name_empty
                            } else {
                                R.string.err_file_name_empty
                            }
                            ctx.toast(msg)
                        }
                    }
                }

                btnCancel.setOnSafeClickListener {
                    if (isShowing) {
                        dismiss()
                    }
                }

                if (!isShowing) {
                    show()
                }
            }
        }
    }

    fun showFileDetailDialog(ctx: Context?, file: FileModel, folder: FileModel) {
        ctx?.run {
            createDialog(ctx, R.layout.dialog_file_detail, true).run {
                val imgFile = findViewById<ImageView>(R.id.img_file)
                val lblName = findViewById<TextView>(R.id.lbl_name)
                val lblSize = findViewById<TextView>(R.id.lbl_size)
                val lblFolder = findViewById<TextView>(R.id.lbl_folder)
                val lblPath = findViewById<TextView>(R.id.lbl_path)
                val lblModified = findViewById<TextView>(R.id.lbl_modified)
                val btnOk = findViewById<TextView>(R.id.btn_ok)

                val imageCornerRadius4 by lazy { CommonUtil.convertDpToPixel(ctx, R.dimen.dimen_4) }
                imgFile.loadImage(file.getThumbnail(), imageCornerRadius4)
                lblName.text = file.name
                lblSize.text = NumberUtil.parseSize(ctx, file.size)
                lblFolder.text = folder.name
                lblPath.text = if (file.getOriginalPath().contains("/")) {
                    file.getOriginalPath().substringBeforeLast("/").replace(
                        Constants.STORAGE_ROOT_FOLDER,
                        getString(R.string.internal_storage)
                    )
                } else {
                    file.getOriginalPath()
                }
                lblModified.text =
                    DateTimeUtil.convertTimeStampToDate(file.modifiedDate, ctx.dateTimeFormat)

                btnOk.setOnSafeClickListener {
                    if (isShowing) {
                        dismiss()
                    }
                }

                if (!isShowing) {
                    show()
                }
            }
        }
    }

    fun showFolderDetailDialog(ctx: Context?, folder: FileModel, size: Long) {
        ctx?.run {
            createDialog(ctx, R.layout.dialog_folder_detail, true).run {
                val lblName = findViewById<TextView>(R.id.lbl_name)
                val lblSize = findViewById<TextView>(R.id.lbl_size)
                val lblQuantity = findViewById<TextView>(R.id.lbl_quantity)
                val lblModified = findViewById<TextView>(R.id.lbl_modified)
                val btnOk = findViewById<TextView>(R.id.btn_ok)

                lblName.text = folder.name
                lblSize.text = NumberUtil.parseSize(ctx, size)
                if (folder.itemQuantity > 0) {
                    lblQuantity.text = NumberUtil.formatNumber(folder.itemQuantity)
                } else {
                    lblQuantity.gone()
                }
                lblModified.text =
                    DateTimeUtil.convertTimeStampToDate(folder.modifiedDate, ctx.dateTimeFormat)

                btnOk.setOnSafeClickListener {
                    if (isShowing) {
                        dismiss()
                    }
                }

                if (!isShowing) {
                    show()
                }
            }
        }
    }

    fun showUnhideDialog(ctx: Context?, quantity: Int, unhideListener: (Boolean) -> Unit) {
        ctx?.run {
            createDialog(ctx, R.layout.dialog_unhide, true).run {
                val lblTitle = findViewById<TextView>(R.id.lbl_title)
                val radPath = findViewById<RadioButton>(R.id.rad_path)
                val radOriginalPath = findViewById<RadioButton>(R.id.rad_original_path)
                val btnOk = findViewById<TextView>(R.id.btn_ok)
                val btnCancel = findViewById<TextView>(R.id.btn_cancel)

                lblTitle.text = String.format(getString(R.string.unhide_title), quantity)
                radPath.text = Constants.UNHIDDEN_FOLDER.replace(
                    Constants.STORAGE_ROOT_FOLDER,
                    getString(R.string.internal_storage)
                )
                radOriginalPath.isChecked = ctx.appSettingsModel.shouldUnhideToOriginalPath

                btnOk.setOnSafeClickListener {
                    if (isShowing) {
                        unhideListener(radOriginalPath.isChecked)

                        dismiss()
                    }
                }

                btnCancel.setOnSafeClickListener {
                    if (isShowing) {
                        dismiss()
                    }
                }

                if (!isShowing) {
                    show()
                }
            }
        }
    }

    fun showExportNoteDialog(ctx: Context?, notes: Int, okListener: (Dialog, String) -> Unit) {
        ctx?.run {
            createDialog(ctx, R.layout.dialog_export_note, true).run {
                val lblTitle = findViewById<TextView>(R.id.lbl_title)
                val txtName = findViewById<AppCompatEditText>(R.id.txt_file_name)
                val lblPath = findViewById<TextView>(R.id.lbl_export_path)
                val btnOk = findViewById<TextView>(R.id.btn_ok)
                val btnCancel = findViewById<TextView>(R.id.btn_cancel)

                lblTitle.text = String.format(getString(R.string.export_note_title, notes))
                lblPath.text = String.format(
                    getString(R.string.export_note_message),
                    Constants.NOTE_FOLDER.replace(
                        Constants.STORAGE_ROOT_FOLDER,
                        getString(R.string.internal_storage)
                    )
                )

                txtName.setText(
                    "Note_${
                        DateTimeUtil.convertTimeStampToDate(
                            currentTimeInSecond, DateTimeUtil.YYYYMMDD_HHMMSS
                        )
                    }"
                )
                txtName.requestFocus()
                CommonUtil.showKeyboard(ctx)

                btnOk.setOnSafeClickListener {
                    if (isShowing) {
                        val name = txtName.text.toString().trim()
                        if (name.isNotBlank()) {
                            okListener(this, name)
                        } else {
                            ctx.toast(R.string.err_file_name_empty)
                        }
                    }
                }

                btnCancel.setOnSafeClickListener {
                    if (isShowing) {
                        dismiss()
                    }
                }

                if (!isShowing) {
                    show()
                }
            }
        }
    }

    fun showSecurityQuestionReviewDialog(
        ctx: Context?, question: String, answer: String, okListener: () -> Unit
    ) {
        ctx?.run {
            createDialog(ctx, R.layout.dialog_review_security_question, false).run {
                val lblQuestion = findViewById<TextView>(R.id.lbl_question)
                val lblAnswer = findViewById<TextView>(R.id.lbl_answer)
                val btnOk = findViewById<TextView>(R.id.btn_ok)
                val btnCancel = findViewById<TextView>(R.id.btn_cancel)

                lblQuestion.text = question
                lblAnswer.text = answer

                btnOk.setOnSafeClickListener {
                    if (isShowing) {
                        dismiss()
                        okListener()
                    }
                }

                btnCancel.setOnSafeClickListener {
                    if (isShowing) {
                        dismiss()
                    }
                }

                if (!isShowing) {
                    show()
                }
            }
        }
    }

    fun showRecoveryEmailReviewDialog(ctx: Context?, email: String, okListener: () -> Unit) {
        ctx?.run {
            createDialog(ctx, R.layout.dialog_review_recovery_email, false).run {
                val lblEmail = findViewById<TextView>(R.id.lbl_email)
                val btnOk = findViewById<TextView>(R.id.btn_ok)
                val btnCancel = findViewById<TextView>(R.id.btn_cancel)

                lblEmail.text = email

                btnOk.setOnSafeClickListener {
                    if (isShowing) {
                        dismiss()
                        okListener()
                    }
                }

                btnCancel.setOnSafeClickListener {
                    if (isShowing) {
                        dismiss()
                    }
                }

                if (!isShowing) {
                    show()
                }
            }
        }
    }

    fun showResetPasswordOptionDialog(
        ctx: Context?, question: (Dialog) -> Unit, email: (Dialog) -> Unit, another: () -> Unit
    ) {
        ctx?.run {
            createDialog(ctx, R.layout.dialog_reset_password_option, false).run {
                val btnQuestion = findViewById<TextView>(R.id.btn_question)
                val btnEmail = findViewById<TextView>(R.id.btn_email)
                val btnCancel = findViewById<TextView>(R.id.btn_cancel)
                val btnAnother = findViewById<TextView>(R.id.btn_another)
                val vwDividerAnother = findViewById<View>(R.id.vw_another)

                btnQuestion.setOnSafeClickListener {
                    if (isShowing) {
                        question(this)
                    }
                }

                btnEmail.setOnSafeClickListener {
                    if (isShowing) {
                        email(this)
                    }
                }

                if (activityModel.failedResetTimes < Constants.MAX_ACTIVITY_VERIFICATION_TIMES) {
                    vwDividerAnother.visible()
                    btnAnother.visible()
                    btnAnother.setOnSafeClickListener {
                        if (isShowing) {
                            dismiss()
                            another()
                        }
                    }
                }

                btnCancel.setOnSafeClickListener {
                    if (isShowing) {
                        dismiss()
                    }
                }

                if (!isShowing) {
                    show()
                }
            }
        }
    }

    fun showChoosingFolderDialog(
        ctx: Context?,
        folders: List<FileModel>,
        titleRes: Int = R.string.select_folder_to_save,
        okListener: (FileModel) -> Unit,
        newFolderListener: () -> Unit
    ) {
        ctx?.run {
            createDialog(ctx, R.layout.dialog_choosing_folder, true).run {
                val rclFolder = findViewById<RecyclerView>(R.id.rcl_folders)
                rclFolder.apply {
                    layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, false)
                    setHasFixedSize(true)
                }
                val lblTitle = findViewById<TextView>(R.id.lbl_title)
                val imgNewFolder = findViewById<ImageView>(R.id.img_create_new_folder)
                val btnCancel = findViewById<TextView>(R.id.btn_cancel)
                val btnOk = findViewById<TextView>(R.id.btn_ok)

                lblTitle.text = ctx.getString(titleRes)

                var selectedFolder: FileModel? = null
                val adapter = ChoosingFolderAdapter(ctx, folders) {
                    selectedFolder = it
                    btnOk.textColor = R.color.colorAccent
                }
                rclFolder.adapter = adapter

                btnOk.setOnSafeClickListener {
                    if (isShowing) {
                        selectedFolder?.also { folder ->
                            okListener(folder)
                            dismiss()
                        } ?: run {
                            ctx.toast(R.string.err_no_folder_selected)
                        }
                    }
                }

                imgNewFolder.setOnSafeClickListener {
                    newFolderListener()

                    if (isShowing) {
                        dismiss()
                    }
                }

                btnCancel.setOnSafeClickListener {
                    if (isShowing) {
                        dismiss()
                    }
                }

                if (!isShowing) {
                    show()
                }
            }
        }
    }

    fun showChangingLanguageDialog(
        ctx: Context?,
        languages: List<LanguageModel>,
        resources: Resources? = null,
        okListener: (LanguageModel) -> Unit
    ) {
        ctx?.run {
            createDialog(ctx, R.layout.dialog_change_language, true).run {
                val rclFolder = findViewById<RecyclerView>(R.id.rcl_languages)
                rclFolder.apply {
                    layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, false)
                    setHasFixedSize(true)
                }
                val lblTitle = findViewById<TextView>(R.id.lbl_title)
                val btnCancel = findViewById<TextView>(R.id.btn_cancel)
                val btnOk = findViewById<TextView>(R.id.btn_ok)

                resources?.run {
                    lblTitle.text = getString(R.string.change_language)
                    btnCancel.text = getString(R.string.cancel)
                }

                val adapter = ChangingLangueAdapter(ctx, languages)
                rclFolder.adapter = adapter

                btnOk.setOnSafeClickListener {
                    if (isShowing) {
                        languages.find { it.isSelected }?.also { language ->
                            okListener(language)
                            dismiss()
                        }
                    }
                }

                btnCancel.setOnSafeClickListener {
                    if (isShowing) {
                        dismiss()
                    }
                }

                if (!isShowing) {
                    show()
                }
            }
        }
    }

    fun showRatingDialog(ctx: Context?, isShowDontAskAgain: Boolean = false) {
        ctx?.run {
            createDialog(ctx, R.layout.dialog_rating, true).run {
                val ckbDontAskAgain = findViewById<CheckBox>(R.id.ckb_dont_ask_again)
                val btnRate = findViewById<TextView>(R.id.btn_rate)
                val btnCancel = findViewById<TextView>(R.id.btn_cancel)

                if (isShowDontAskAgain) {
                    ckbDontAskAgain.visible()
                    ckbDontAskAgain.setOnCheckedChangeListener { _, isChecked ->
                        ctx.appSettingsModel.apply {
                            dontShowRateDialogAgain = isChecked
                        }.run {
                            CommonUtil.saveAppSettingsModel(ctx, this)
                        }
                    }
                }

                btnRate.setOnSafeClickListener {
                    CommonUtil.openAppInPlayStore(ctx)

                    if (isShowing) {
                        dismiss()
                    }
                }

                btnCancel.setOnSafeClickListener {
                    if (isShowing) {
                        dismiss()
                    }
                }

                if (!isShowing) {
                    show()
                }
            }
        }
    }

    fun showAboutDialog(ctx: Context?) {
        ctx?.run {
            createDialog(ctx, R.layout.dialog_about, true).run {
                val lblVersion = findViewById<TextView>(R.id.lbl_version)
                val lblEmail = findViewById<TextView>(R.id.lbl_email)
                val lblAddress = findViewById<TextView>(R.id.lbl_address)
                val btnOk = findViewById<TextView>(R.id.btn_ok)

                lblVersion.text =
                    String.format(ctx.getString(R.string.version_value), BuildConfig.VERSION_NAME)
                lblEmail.text = Constants.SUPPORT_EMAIL
                lblAddress.text = Constants.ADDRESS

                btnOk.setOnSafeClickListener {
                    if (isShowing) {
                        dismiss()
                    }
                }

                if (!isShowing) {
                    show()
                }
            }
        }
    }

    private fun createDialog(ctx: Context, resLayout: Int, cancelable: Boolean): Dialog {
        return Dialog(ctx).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawableResource(R.color.transparent)
            window?.attributes?.windowAnimations = R.style.CustomDialog
            setContentView(resLayout)
            window?.setLayout(
                CommonUtil.convertDpToPixel(ctx, R.dimen.dimen_340),
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            setCancelable(cancelable)
        }
    }
}