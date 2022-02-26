package com.hide.videophoto.ui.note

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hide.videophoto.R
import com.hide.videophoto.common.Constants
import com.hide.videophoto.common.ext.*
import com.hide.videophoto.common.util.DialogUtil
import com.hide.videophoto.common.util.EventTrackingManager
import com.hide.videophoto.common.util.ExternalStoragePermissionUtil
import com.hide.videophoto.data.model.EventNoteModel
import com.hide.videophoto.data.model.FileModel
import com.hide.videophoto.ui.base.BaseActivity
import java.io.File

class NoteActivity : BaseActivity<NoteView, NotePresenterImp>(), NoteView {

    private var menuItemLayout: MenuItem? = null
    private val btnSelect by lazy { findViewById<LinearLayout>(R.id.ll_select) }
    private val imgSelect by lazy { findViewById<ImageView>(R.id.img_select) }
    private val lblSelect by lazy { findViewById<TextView>(R.id.lbl_select) }
    private val rclNote by lazy { findViewById<RecyclerView>(R.id.rcl_notes) }
    private val lblNoData by lazy { findViewById<TextView>(R.id.lbl_no_data) }
    private val cstActionNote by lazy { findViewById<ConstraintLayout>(R.id.cst_action_note) }
    private val btnDelete by lazy { findViewById<LinearLayout>(R.id.ll_delete) }
    private val btnExport by lazy { findViewById<LinearLayout>(R.id.ll_export) }

    private val externalStoragePermissionUtil = ExternalStoragePermissionUtil(self)
    private val arrNotes by lazy { arrayListOf<FileModel>() }
    private lateinit var noteAdapter: NoteAdapter

    override fun onBackPressed() {
        if (noteAdapter.isSelectableMode()) {
            setToolbarHomeIcon(R.drawable.ic_back_white)
            resetMode()
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_note_list, menu)

        menuItemLayout = menu?.findItem(R.id.menu_layout)
        updateMenuItemLayout()

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_layout -> {
                noteAdapter.switchLayout()
                updateMenuItemLayout()
            }
            R.id.menu_add -> {
                AddingNoteActivity.start(ctx)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun initView(): NoteView {
        return this
    }

    override fun initPresenter(): NotePresenterImp {
        return NotePresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_note
    }

    override fun initWidgets() {
        // Init toolbar
        enableHomeAsUp {
            onBackPressed()
        }
        showTitle(R.string.note)

        // Init recyclerView
        rclNote.apply {
            layoutManager = StaggeredGridLayoutManager(
                appSettingsModel.layoutTypeNote ?: Constants.Layout.LIST,
                StaggeredGridLayoutManager.VERTICAL
            )
            setHasFixedSize(true)
            noteAdapter = NoteAdapter(
                self, arrNotes,
                itemClickListener = { model ->
                    if (noteAdapter.isSelectableMode()) {
                        updateUI()
                    } else {
                        AddingNoteActivity.start(ctx, model)
                    }
                },
                itemLongClickListener = { model ->
                    if (noteAdapter.isSelectableMode()) {
                        updateUI()
                        setToolbarHomeIcon(R.drawable.ic_menu_close)
                    }
                },
                copyListener = {
                    copy(it)
                },
                editListener = {
                    AddingNoteActivity.start(ctx, it, true)
                },
                deletingListener = {
                    showDeleteNotesDialog(listOf(it))
                },
                exportListener = {
                    showExportNotesDialog(listOf(it))
                }
            )
            adapter = noteAdapter
        }

        // Get notes
        getNotes()

        // Listener
        btnSelect.setOnSafeClickListener {
            if (!noteAdapter.isSelectableMode()) {
                noteAdapter.switchMode()
                updateUI()

                setToolbarHomeIcon(R.drawable.ic_menu_close)
            } else {
                if (arrNotes.any { !it.isSelected }) {
                    arrNotes.map { filteredModel ->
                        if (!filteredModel.isSelected) {
                            filteredModel.isSelected = true
                        }
                    }
                } else {
                    arrNotes.map { filteredModel ->
                        if (filteredModel.isSelected) {
                            filteredModel.isSelected = false
                        }
                    }
                }
                updateUI()
                noteAdapter.notifyDataSetChanged()
            }
        }

        btnDelete.setOnSafeClickListener {
            if (arrNotes.any { it.isSelected }) {
                val filesToDelete = arrNotes.filter { it.isSelected }
                showDeleteNotesDialog(filesToDelete)
            } else {
                toast(R.string.no_note_selected)
            }
        }

        btnExport.setOnSafeClickListener {
            if (arrNotes.any { it.isSelected }) {
                val filesToExport = arrNotes.filter { it.isSelected }
                showExportNotesDialog(filesToExport)
            } else {
                toast(R.string.no_note_selected)
            }
        }

        lblNoData.setOnSafeClickListener {
            AddingNoteActivity.start(ctx)
        }

        presenter.listenNoteChangedEvent()
    }

    override fun onNotesLoadedSuccess(notes: List<FileModel>) {
        arrNotes.clear()
        arrNotes.addAll(notes)

        if (arrNotes.isNotEmpty()) {
            rclNote.visible()
            lblNoData.gone()

            // Notify data changed
            noteAdapter.notifyItemRangeInserted(0, arrNotes.size)
        } else {
            rclNote.gone()
            lblNoData.visible()
        }
    }

    override fun onNotesDeletedSuccess(models: List<FileModel>) {
        // Update UI
        arrNotes.removeAll {
            it in models
        }
        if (arrNotes.isEmpty()) {
            rclNote.gone()
            lblNoData.visible()
        }
        noteAdapter.notifyDataSetChanged()
        updateUI()

        // Show success message
        showInterstitialAd {
            toast(R.string.msg_deleted_success)
        }

        // Event tracking
        eventTracking.logEvent(EventTrackingManager.NOTE_DELETED)
    }

    override fun onNotesExportedSuccess(models: List<FileModel>) {
        // Update UI
        if (noteAdapter.isSelectableMode()) {
            resetMode()
        }

        // Show success message
        showInterstitialAd {
            toast(
                String.format(
                    getString(R.string.exported_success),
                    Constants.NOTE_FOLDER.replace(
                        Constants.STORAGE_ROOT_FOLDER,
                        getString(R.string.internal_storage)
                    )
                ), true
            )
        }

        // Event tracking
        eventTracking.logEvent(EventTrackingManager.NOTE_EXPORTED)
    }

    override fun onNoteChangedEvent(eventNoteModel: EventNoteModel) {
        when (eventNoteModel.event) {
            Constants.Event.ADD -> {
                eventNoteModel.noteModel?.run {
                    rclNote.visible()
                    lblNoData.gone()

                    arrNotes.add(0, this)
                    noteAdapter.notifyItemInserted(0)
                    rclNote.smoothScrollToPosition(0)
                }
            }
            Constants.Event.EDIT -> {
                eventNoteModel.noteModel?.run {
                    val index = arrNotes.indexOfFirst {
                        it.rowId == rowId
                    }
                    if (index >= 0) {
                        arrNotes[index] = this
                        noteAdapter.notifyItemChanged(index)
                    }
                }
            }
        }
    }

    private fun getNotes() {
        externalStoragePermissionUtil.requestPermission(object :
            ExternalStoragePermissionUtil.Listener {
            override fun onGranted() {
                presenter.getNotesFromDb()
            }

            override fun onDenied() {
                rclNote.gone()
                lblNoData.visible()

                toast(R.string.msg_permission_storage)
            }
        })
    }

    private fun updateUI() {
        if (noteAdapter.isSelectableMode()) {
            if (cstActionNote.isGone) {
                cstActionNote.visible()
            }

            if (arrNotes.any { it.isSelected }) {
                btnDelete.setBackgroundResource(R.drawable.btn_accent_round_left)
                btnExport.setBackgroundResource(R.drawable.btn_accent_round_right)
            } else {
                btnDelete.setBackgroundResource(R.drawable.btn_grey_round_left)
                btnExport.setBackgroundResource(R.drawable.btn_grey_round_right)
            }
        } else {
            if (cstActionNote.isVisible) {
                cstActionNote.gone()
            }
        }

        // Update select UI
        if (noteAdapter.isSelectableMode()) {
            if (arrNotes.isEmpty() || arrNotes.any { !it.isSelected }) {
                lblSelect.text = getString(R.string.select_all)
                imgSelect.setImageResource(R.drawable.ic_unselected)
            } else {
                lblSelect.text = getString(R.string.deselect_all)
                imgSelect.setImageResource(R.drawable.ic_selected)
            }
        } else {
            lblSelect.text = getString(R.string.select)
            imgSelect.setImageResource(R.drawable.ic_select_white)
        }
    }

    private fun updateMenuItemLayout() {
        if (Constants.Layout.GRID_NOTE == appSettingsModel.layoutTypeNote) {
            menuItemLayout?.setIcon(R.drawable.ic_menu_list)
        } else {
            menuItemLayout?.setIcon(R.drawable.ic_menu_grid)
        }
    }

    private fun copy(model: FileModel) {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("note", "${model.title}\n${model.content}")
        clipboardManager.setPrimaryClip(clipData)

        toast(R.string.copied)
    }

    private fun showDeleteNotesDialog(models: List<FileModel>) {
        externalStoragePermissionUtil.requestPermission(object :
            ExternalStoragePermissionUtil.Listener {
            override fun onGranted() {
                val title = String.format(getString(R.string.delete_note_title), models.size)
                DialogUtil.showConfirmationDialog(
                    ctx, textTitle = title, textMessage = R.string.delete_note_message,
                    textCancel = R.string.cancel, textOk = R.string.delete, cancelable = true,
                    okListener = {
                        presenter.deleteNotes(models)
                    }
                )
            }

            override fun onDenied() {
                toast(R.string.msg_permission_storage)
            }
        })
    }

    private fun showExportNotesDialog(models: List<FileModel>) {
        externalStoragePermissionUtil.requestPermission(object :
            ExternalStoragePermissionUtil.Listener {
            override fun onGranted() {
                DialogUtil.showExportNoteDialog(ctx, models.size) { dialog, fileName ->
                    val outputFile = File("${Constants.NOTE_FOLDER}${File.separator}$fileName.txt")
                    if (!outputFile.exists()) {
                        presenter.exportNotes(models, outputFile)

                        dialog.dismiss()
                    } else {
                        toast(R.string.err_file_exists_already)
                    }
                }
            }

            override fun onDenied() {
                toast(R.string.msg_permission_storage)
            }
        })
    }

    private fun setToolbarHomeIcon(resIcon: Int) {
        getToolbar().setNavigationIcon(resIcon)
    }

    private fun resetMode() {
        noteAdapter.switchMode()
        updateUI()
    }

    private fun showInterstitialAd(onAdDismissed: () -> Unit) {
        adsManager.showInterstitialAd(self) {
            onAdDismissed()
        }
    }
}
