package com.hide.videophoto.ui.note

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.core.view.isGone
import com.hide.videophoto.R
import com.hide.videophoto.common.Constants
import com.hide.videophoto.common.ext.*
import com.hide.videophoto.common.util.CommonUtil
import com.hide.videophoto.common.util.EventTrackingManager
import com.hide.videophoto.common.util.ExternalStoragePermissionUtil
import com.hide.videophoto.common.util.RxBus
import com.hide.videophoto.data.mapper.convertToFileModel
import com.hide.videophoto.data.model.EventNoteModel
import com.hide.videophoto.data.model.FileModel
import com.hide.videophoto.ui.base.BaseActivity

class AddingNoteActivity : BaseActivity<AddingNoteView, AddingNotePresenterImp>(), AddingNoteView {

    private var menuItemDone: MenuItem? = null
    private var menuItemEdit: MenuItem? = null
    private val vwBarTitle by lazy { findViewById<View>(R.id.vw_bar_title) }
    private val txtTitle by lazy { findViewById<EditText>(R.id.txt_title) }
    private val txtContent by lazy { findViewById<EditText>(R.id.txt_content) }

    private val externalStoragePermissionUtil = ExternalStoragePermissionUtil(self)
    private var noteModel: FileModel? = null
    private var isEditing = false

    private val textWatcher by lazy {
        object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(text: Editable?) {
                updateMenuDoneUI()
            }
        }
    }

    companion object {
        private const val PARAM_FILE_MODEL = "param_file_model"
        private const val PARAM_IS_EDITING = "param_is_editing"

        fun start(ctx: Context, fileModel: FileModel? = null, isEditing: Boolean = false) {
            Intent(ctx, AddingNoteActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                fileModel?.also { model ->
                    putExtra(PARAM_FILE_MODEL, model.toJson())
                    putExtra(PARAM_IS_EDITING, isEditing)
                }
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
                if (bundle.containsKey(PARAM_FILE_MODEL)) {
                    noteModel = bundle.getString(PARAM_FILE_MODEL)?.convertToFileModel()
                }
                if (bundle.containsKey(PARAM_IS_EDITING)) {
                    isEditing = bundle.getBoolean(PARAM_IS_EDITING)
                }
            }
        }

        // Init UI
        noteModel?.run {
            txtTitle.setText(title)
            txtContent.setText(content)

            if (isEditing) {
                if (content?.isNotBlank() == true) {
                    txtContent.apply {
                        requestFocus()
                        try {
                            setSelection(length())
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    txtTitle.apply {
                        requestFocus()
                        try {
                            setSelection(length())
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                CommonUtil.showKeyboard(ctx)
            } else {
                vwBarTitle.gone()

                if (title?.isNotBlank() != true) {
                    txtTitle.gone()
                }
                if (content?.isNotBlank() != true) {
                    txtContent.hint = ""
                }

                txtTitle.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        menuItemDone?.isVisible = true
                        menuItemEdit?.isVisible = false
                        vwBarTitle.visible()

                        txtContent.hint = getString(R.string.enter_note_content)
                    }
                }

                txtContent.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        menuItemDone?.isVisible = true
                        menuItemEdit?.isVisible = false
                        vwBarTitle.visible()

                        if (txtTitle.isGone) {
                            txtTitle.visible()
                        }
                        txtContent.hint = getString(R.string.enter_note_content)
                    }
                }
            }
        } ?: run {
            showTitle(R.string.add_note)

            txtContent.requestFocus()
            CommonUtil.showKeyboard(ctx)
        }
    }

    override fun onStart() {
        super.onStart()

        // Listen text changed
        txtTitle.addTextChangedListener(textWatcher)
        txtContent.addTextChangedListener(textWatcher)
    }

    override fun onStop() {
        super.onStop()

        // Remove text changed
        txtTitle.removeTextChangedListener(textWatcher)
        txtContent.removeTextChangedListener(textWatcher)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_note_adding, menu)

        menuItemDone = menu?.findItem(R.id.menu_done)
        menuItemEdit = menu?.findItem(R.id.menu_edit)

        noteModel?.run {
            if (!isEditing) {
                menuItemDone?.isVisible = false
                menuItemEdit?.isVisible = true
            }
        }

        updateMenuDoneUI()

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_done -> {
                noteModel?.run {
                    updateNote()
                } ?: run {
                    addNote()
                }
            }
            R.id.menu_edit -> {
                noteModel?.run {
                    if (txtTitle.isGone) {
                        txtTitle.visible()
                    }
                    if (content?.isNotBlank() == true) {
                        txtContent.apply {
                            requestFocus()
                            try {
                                setSelection(length())
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } else if (title?.isNotBlank() == true) {
                        txtTitle.apply {
                            requestFocus()
                            try {
                                setSelection(length())
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    // Show keyboard
                    CommonUtil.showKeyboard(ctx)
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun initView(): AddingNoteView {
        return this
    }

    override fun initPresenter(): AddingNotePresenterImp {
        return AddingNotePresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_adding_note
    }

    override fun initWidgets() {
        // Init toolbar
        enableHomeAsUp {
            finish()
        }
    }

    override fun onNoteAddedSuccess(model: FileModel) {
        // Event tracking
        eventTracking.logEvent(EventTrackingManager.NOTE_ADDED)

        // Publish added success event
        publishNoteEvent(Constants.Event.ADD, model)

        // Close activity
        showInterstitialAd {
            finish()
        }
    }

    override fun onNoteEditedSuccess(model: FileModel) {
        // Publish added success event
        publishNoteEvent(Constants.Event.EDIT, model)

        // Close activity
        showInterstitialAd {
            finish()
        }
    }

    override fun onQueryDbError() {
        toast(R.string.err_common)
    }

    private fun addNote() {
        val title = txtTitle.text.toString().trim()
        val content = txtContent.text.toString().trim()
        if (title.isNotBlank() || content.isNotBlank()) {
            externalStoragePermissionUtil.requestPermission(object :
                ExternalStoragePermissionUtil.Listener {
                override fun onGranted() {
                    val model = FileModel().apply {
                        this.title = title
                        this.content = content
                        this.isNote = true
                    }
                    presenter.addNote(model)
                }

                override fun onDenied() {
                    toast(R.string.msg_permission_storage)
                }
            })
        } else {
            txtContent.requestFocus()
            toast(R.string.err_note_empty)
        }
    }

    private fun updateNote() {
        val title = txtTitle.text.toString().trim()
        val content = txtContent.text.toString().trim()
        if (title.isNotBlank() || content.isNotBlank()) {
            externalStoragePermissionUtil.requestPermission(object :
                ExternalStoragePermissionUtil.Listener {
                override fun onGranted() {
                    noteModel?.apply {
                        this.title = title
                        this.content = content
                    }?.run {
                        presenter.updateNote(this)
                    }
                }

                override fun onDenied() {
                    toast(R.string.msg_permission_storage)
                }
            })
        } else {
            txtContent.requestFocus()
            toast(R.string.err_note_empty)
        }
    }

    private fun updateMenuDoneUI() {
        // Toggle confirm button UI
        if (txtTitle.text.toString().isEmpty() && txtContent.text.toString().isEmpty()) {
            menuItemDone?.setIcon(R.drawable.ic_menu_done_inactive)
        } else {
            menuItemDone?.setIcon(R.drawable.ic_menu_done_active)
        }
    }

    private fun publishNoteEvent(event: String, model: FileModel) {
        RxBus.publishNoteChanged(
            EventNoteModel().apply {
                this.event = event
                noteModel = model
            }
        )
    }

    private fun showInterstitialAd(onAdDismissed: () -> Unit) {
        adsManager.showInterstitialAd(self) {
            onAdDismissed()
        }
    }
}
