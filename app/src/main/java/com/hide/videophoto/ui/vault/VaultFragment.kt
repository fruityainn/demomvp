package com.hide.videophoto.ui.vault

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hide.videophoto.R
import com.hide.videophoto.common.Constants
import com.hide.videophoto.common.ext.*
import com.hide.videophoto.common.util.*
import com.hide.videophoto.data.mapper.*
import com.hide.videophoto.data.model.FileModel
import com.hide.videophoto.ui.base.BaseFragment
import com.hide.videophoto.ui.main.MainActivity
import com.hide.videophoto.ui.player.MediaPlayerActivity
import com.hide.videophoto.ui.slider.SliderActivity
import java.io.File

class VaultFragment : BaseFragment<VaultView, VaultPresenterImp>(), VaultView {

    private var menuItemLayout: MenuItem? = null
    private lateinit var llSelect: LinearLayout
    private lateinit var imgSelect: ImageView
    private lateinit var lblSelect: TextView
    private lateinit var btnSort: TextView
    private lateinit var imgSortDirection: ImageView
    private lateinit var rclVault: RecyclerView
    private lateinit var lblNoData: TextView
    private lateinit var vwBarAction: View
    private lateinit var btnHide: LinearLayout
    private lateinit var lblHide: TextView
    private lateinit var cstActionFile: ConstraintLayout
    private lateinit var btnDelete: LinearLayout
    private lateinit var btnUnhide: LinearLayout
    private lateinit var btnMove: LinearLayout

    private val externalStoragePermissionUtil = ExternalStoragePermissionUtil(self)
    private val arrAllFiles by lazy { arrayListOf<FileModel>() }
    private val arrFilteredFiles by lazy { arrayListOf<FileModel>() }
    private lateinit var vaultAdapter: VaultAdapter
    private var fileModel: FileModel? = null
    private var isShowingFileByFolder: Boolean = false
    private var isShowingFileByType: Boolean = false
    private var dataType: String? = null
    private var isChoosingFileToHide: Boolean = false
    private var modeListener: ModeListener? = null
    private val arrFolder by lazy { arrayListOf<FileModel>() }

    companion object {
        private const val PARAM_IS_SHOWING_FILE_BY_FOLDER = "showing_file_by_folder"
        private const val PARAM_FILE_MODEL = "param_file_model"
        private const val PARAM_IS_SHOWING_FILE_BY_TYPE = "showing_file_by_type"
        private const val PARAM_DATA_TYPE = "param_data_type"
        private const val PARAM_IS_CHOOSING_FILE_TO_HIDE = "choosing_file_to_hide"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param folder Parent folder name for loading files.
         * @return A new instance of fragment VaultFragment.
         */
        fun newInstance(
            isShowingFileByFolder: Boolean? = null,
            fileModel: FileModel? = null,
            isShowingFileByType: Boolean? = null,
            dataType: String? = null,
            isChoosingFileToHide: Boolean? = null
        ): VaultFragment {
            return VaultFragment().apply {
                arguments = Bundle().apply {
                    isShowingFileByFolder?.also { flag ->
                        putBoolean(PARAM_IS_SHOWING_FILE_BY_FOLDER, flag)
                    }
                    fileModel?.also { model ->
                        putString(PARAM_FILE_MODEL, model.toJson())
                    }
                    isShowingFileByType?.also { flag ->
                        putBoolean(PARAM_IS_SHOWING_FILE_BY_TYPE, flag)
                    }
                    dataType?.also { type ->
                        putString(PARAM_DATA_TYPE, type)
                    }
                    isChoosingFileToHide?.also { flag ->
                        putBoolean(PARAM_IS_CHOOSING_FILE_TO_HIDE, flag)
                    }
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        modeListener = context as ModeListener?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { arg ->
            if (arg.containsKey(PARAM_IS_SHOWING_FILE_BY_FOLDER)) {
                isShowingFileByFolder = arg.getBoolean(PARAM_IS_SHOWING_FILE_BY_FOLDER)
            }
            if (arg.containsKey(PARAM_FILE_MODEL)) {
                fileModel = arg.getString(PARAM_FILE_MODEL)?.convertToFileModel()
            }
            if (arg.containsKey(PARAM_IS_SHOWING_FILE_BY_TYPE)) {
                isShowingFileByType = arg.getBoolean(PARAM_IS_SHOWING_FILE_BY_TYPE)
            }
            if (arg.containsKey(PARAM_DATA_TYPE)) {
                dataType = arg.getString(PARAM_DATA_TYPE)
            }
            if (arg.containsKey(PARAM_IS_CHOOSING_FILE_TO_HIDE)) {
                isChoosingFileToHide = arg.getBoolean(PARAM_IS_CHOOSING_FILE_TO_HIDE)
            }
        }

        // Show options menu
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isChoosingFileToHide) { // Switch to selectable mode
            vaultAdapter.run {
                setHiding(true)
                switchMode()
                if (null == ctx?.appSettingsModel?.layoutTypeFile) {
                    switchLayout()
                }
            }

            // Show/hide action layout
            updateUI()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_vault, menu)

        menuItemLayout = menu.findItem(R.id.menu_layout)
        updateMenuItemLayout()

        // Hide add option if it's hiding mode
        if (isChoosingFileToHide) {
            menu.findItem(R.id.menu_add).isVisible = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_layout -> {
                vaultAdapter.switchLayout()
                updateMenuItemLayout()
            }
            R.id.menu_add -> {
                ctx?.run {
                    MainActivity.start(ctx, true)
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun initView(): VaultView {
        return this
    }

    override fun initPresenter(): VaultPresenterImp {
        return VaultPresenterImp(ctx!!)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_vault
    }

    override fun initWidgets(rootView: View) {
        // Find views
        rootView.run {
            // Init recyclerView-
            rclVault = findViewById(R.id.rcl_vault)
            rclVault.apply {
                layoutManager = GridLayoutManager(
                    ctx, ctx.appSettingsModel.layoutTypeFile ?: Constants.Layout.LIST,
                    GridLayoutManager.VERTICAL, false
                )
                setHasFixedSize(true)
                vaultAdapter = VaultAdapter(
                    parentActivity, arrFilteredFiles,
                    itemClickListener = { model ->
                        if (vaultAdapter.isSelectableMode()) {
                            updateUI()
                        } else {
                            clickOnItem(model)
                        }
                    },
                    itemLongClickListener = { model ->
                        if (vaultAdapter.isSelectableMode()) {
                            updateUI()
                            notifyModeChangedToParentActivity()
                        }
                    },
                    movingListener = {
                        getFoldersToSave(listOf(it))
                    },
                    renameListener = {
                        showRenameDialog(it)
                    },
                    unhideListener = {
                        showUnhideDialog(listOf(it))
                    },
                    deletingListener = {
                        showDeleteFilesDialog(listOf(it))
                    },
                    detailListener = {
                        getFolderDetail(it)
                    }
                )
                adapter = vaultAdapter
            }

            llSelect = findViewById(R.id.ll_select)
            imgSelect = findViewById(R.id.img_select)
            lblSelect = findViewById(R.id.lbl_select)
            btnSort = findViewById(R.id.btn_sort)
            imgSortDirection = findViewById(R.id.img_sort_direction)
            lblNoData = findViewById(R.id.lbl_no_data)
            vwBarAction = findViewById(R.id.vw_bar_action)
            btnHide = findViewById(R.id.btn_hide)
            lblHide = findViewById(R.id.lbl_hide)
            cstActionFile = findViewById(R.id.cst_action_file)
            btnDelete = findViewById(R.id.ll_delete)
            btnUnhide = findViewById(R.id.ll_unhide)
            btnMove = findViewById(R.id.ll_move)
        }

        // Get hidden files/folders from DB or get media files from device for hiding
        getFiles()

        // Listener
        llSelect.setOnSafeClickListener {
            if (!vaultAdapter.isSelectableMode()) {
                vaultAdapter.switchMode()
                updateUI()

                notifyModeChangedToParentActivity()
            } else {
                if (arrFilteredFiles.any { !it.isSelected }) {
                    arrFilteredFiles.map { filteredModel ->
                        if (!filteredModel.isSelected) {
                            filteredModel.isSelected = true
                        }
                    }

                    presenter.doingInBackground({
                        arrAllFiles.filter {
                            it in arrFilteredFiles && !it.isSelected
                        }.map {
                            it.isSelected = true
                        }
                    })
                } else {
                    arrFilteredFiles.map { filteredModel ->
                        if (filteredModel.isSelected) {
                            filteredModel.isSelected = false
                        }
                    }

                    presenter.doingInBackground({
                        arrAllFiles.filter {
                            it in arrFilteredFiles && it.isSelected
                        }.map {
                            it.isSelected = false
                        }
                    })
                }
                updateUI()
                vaultAdapter.notifyDataSetChanged()
            }
        }

        btnSort.setOnSafeClickListener {
            btnSort.showPopupMenu(R.menu.menu_sort) {
                when (it.itemId) {
                    R.id.menu_date -> {
                        sortFiles(Constants.SortType.DATE)
                    }
                    R.id.menu_name -> {
                        sortFiles(Constants.SortType.NAME)
                    }
                    R.id.menu_size -> {
                        sortFiles(Constants.SortType.SIZE)
                    }
                }
            }
        }

        imgSortDirection.setOnSafeClickListener {
            if (Constants.SortType.DIRECTION_ASC == ctx?.appSettingsModel?.sortDirection) {
                sortFiles(direction = Constants.SortType.DIRECTION_DESC)
            } else {
                sortFiles(direction = Constants.SortType.DIRECTION_ASC)
            }
        }

        btnHide.setOnSafeClickListener {
            if (arrAllFiles.any { it.isSelected }) {
                val filesToHide = arrAllFiles.filter { it.isSelected }
                getFoldersToSave(filesToHide)
            } else {
                ctx?.toast(R.string.no_file_selected)
            }
        }

        btnUnhide.setOnSafeClickListener {
            if (arrFilteredFiles.any { it.isSelected }) {
                val filesToUnhide = arrFilteredFiles.filter { it.isSelected }
                showUnhideDialog(filesToUnhide)
            } else {
                ctx?.toast(R.string.no_file_selected)
            }
        }

        btnMove.setOnSafeClickListener {
            if (arrFilteredFiles.any { it.isSelected }) {
                val filesToMove = arrFilteredFiles.filter { it.isSelected }
                getFoldersToSave(filesToMove)
            } else {
                ctx?.toast(R.string.no_file_selected)
            }
        }

        btnDelete.setOnSafeClickListener {
            if (arrFilteredFiles.any { it.isSelected }) {
                val filesToDelete = arrFilteredFiles.filter { it.isSelected }
                showDeleteFilesDialog(filesToDelete)
            } else {
                ctx?.toast(R.string.no_file_selected)
            }
        }

        lblNoData.setOnSafeClickListener {
            ctx?.run {
                MainActivity.start(ctx, true)
            }
        }

        // Listen file changed event
//        presenter.listenFileChangedEvent()
    }

    override fun onFilesLoadedSuccess(files: List<FileModel>) {
        fillFiles(files)

        // Distinct folder
        val buckets = arrayListOf<FileModel>()
        if (isChoosingFileToHide) {
            files.distinctBy {
                it.bucketName
            }.map { bucket ->
                bucket.itemQuantity = files.count {
                    it.bucketName == bucket.bucketName
                }
                buckets.add(bucket)
            }
            modeListener?.onMediaFilesLoadedSuccess(buckets)
        }
    }

    override fun onFoldersLoadedSuccess(folders: List<FileModel>, selectedModels: List<FileModel>) {
        arrFolder.clear()
        arrFolder.addAll(folders)

        // Show folders dialog to select
        showChoosingFolderDialog(selectedModels)
    }

    override fun onFolderAddedSuccess(folder: FileModel) {
        val models = if (isChoosingFileToHide) {
            arrAllFiles.filter { it.isSelected }
        } else {
            arrFilteredFiles.filter { it.isSelected }
        }
        getFoldersToSave(models)

        // Publish folder changed event
        publishFolderChanged()

        // Event tracking
        ctx?.eventTracking?.logEvent(EventTrackingManager.CREATE_NEW_FOLDER)
    }

    override fun onFilesEncryptedSuccess(models: List<FileModel>) {
        // Update to activity entity
        if (models.isNotEmpty()) {
            val mimeType = models[0].mimeType
            val content = when {
                mimeType?.startsWith(Constants.DataType.VIDEO) == true -> {
                    Constants.DataType.VIDEO
                }
                mimeType?.startsWith(Constants.DataType.IMAGE) == true -> {
                    Constants.DataType.IMAGE
                }
                mimeType?.startsWith(Constants.DataType.AUDIO) == true -> {
                    Constants.DataType.AUDIO
                }
                else -> {
                    Constants.DataType.OTHER
                }
            }
            presenter.saveLastAddedContent(content)
        }

        // Publish folder changed event
        publishFolderChanged()

        // Clear screen on flag after done
        parentActivity.clearScreenOnFlag()

        // Show success message and finish parent activity
        showInterstitialAd {
            ctx?.toast(R.string.msg_hid_success)

            ctx?.appSettingsModel?.shouldCheckShowingRateDialog = true

            parentActivity.finish()
        }
    }

    override fun onFilesMovedSuccess(models: List<FileModel>) {
        // remove from recent list
        if (isShowingFileByFolder) {
            arrFilteredFiles.removeAll {
                it in models
            }
            if (arrFilteredFiles.isEmpty()) {
                rclVault.gone()
                lblNoData.visible()
            }

            arrAllFiles.removeAll {
                it in models
            }
        } else {
            arrFilteredFiles.filter {
                it in models && it.isSelected
            }.map {
                it.isSelected = false
            }

            arrAllFiles.filter {
                it in models && it.isSelected
            }.map {
                it.isSelected = false
            }
        }
        vaultAdapter.notifyDataSetChanged()
        updateUI()

        // Show success message
        showInterstitialAd {
            ctx?.toast(R.string.msg_moved_success)
        }

        // Publish folder changed event
        publishFolderChanged()
    }

    override fun onFileRenamedSuccess(model: FileModel) {
        vaultAdapter.notifyItemChanged(arrFilteredFiles.indexOf(model))
    }

    override fun onFilesDeletedSuccess(models: List<FileModel>) {
        // Update UI
        arrFilteredFiles.removeAll {
            it in models
        }
        if (arrFilteredFiles.isEmpty()) {
            rclVault.gone()
            lblNoData.visible()
        }
        vaultAdapter.notifyDataSetChanged()

        presenter.doingInBackground(
            {
                arrAllFiles.removeAll {
                    it in models
                }
            },
            {
                updateUI()
            }
        )

        // Show success message
        showInterstitialAd {
            ctx?.toast(R.string.msg_deleted_success)
        }

        // Publish folder changed event
        publishFolderChanged()
    }

    override fun onFilesUnhiddenSuccess(models: List<FileModel>) {
        // Update UI
        arrFilteredFiles.removeAll {
            it in models
        }
        if (arrFilteredFiles.isEmpty()) {
            rclVault.gone()
            lblNoData.visible()
        }
        vaultAdapter.notifyDataSetChanged()

        presenter.doingInBackground(
            {
                arrAllFiles.removeAll {
                    it in models
                }
            },
            {
                updateUI()
            }
        )

        // Show success message
        showInterstitialAd {
            ctx?.toast(R.string.msg_unhide_success)
        }

        // Publish folder changed event
        publishFolderChanged()

        // Clear screen on flag after done
        parentActivity.clearScreenOnFlag()
    }

    override fun onFolderDetailLoadedSuccess(file: FileModel, folder: FileModel) {
        DialogUtil.showFileDetailDialog(ctx, file, folder)
    }

    private fun getFiles() {
        externalStoragePermissionUtil.requestPermission(object :
            ExternalStoragePermissionUtil.Listener {
            override fun onGranted() {
                when {
                    isShowingFileByFolder -> {
                        fileModel?.also { model ->
                            presenter.getFilesByFolderFromDb(model.rowId)
                        }
                    }
                    isShowingFileByType -> {
                        dataType?.also { type ->
                            when (type) {
                                Constants.DataType.VIDEO, Constants.DataType.IMAGE -> {
                                    presenter.getFilesByTypeFromDb(type)
                                }
                                else -> {
                                    presenter.getOtherFilesFromDb()
                                }
                            }
                        }
                    }
                    isChoosingFileToHide -> {
                        when (dataType) {
                            Constants.DataType.VIDEO -> presenter.getVideosForHiding()
                            Constants.DataType.IMAGE -> presenter.getPhotosForHiding()
                            Constants.DataType.AUDIO -> presenter.getAudiosForHiding()
                        }
                    }
                }
            }

            override fun onDenied() {
                rclVault.gone()
                lblNoData.visible()

                ctx?.toast(R.string.msg_permission_storage)
            }
        })
    }

    private fun fillFiles(files: List<FileModel>) {
        // Add all files and sort by user's choice
        arrAllFiles.addAll(0, files)
        sortFiles(refreshAdapter = false)

        arrFilteredFiles.addAll(arrAllFiles)
        if (arrFilteredFiles.isNotEmpty()) {
            rclVault.visible()
            lblNoData.gone()

            // Notify data changed
            vaultAdapter.notifyItemRangeInserted(0, files.size)
            rclVault.smoothScrollToPosition(0)
        } else {
            rclVault.gone()
            lblNoData.visible()
        }
    }

    private fun clickOnItem(model: FileModel) {
        ctx?.run {
            when {
                model.isImage() -> { // Open image slider if it's image
                    val arrPhoto = arrayListOf<FileModel>()
                    arrAllFiles.filter {
                        it.isImage()
                    }.also { photos ->
                        arrPhoto.addAll(photos)
                    }
                    val position = arrPhoto.indexOfFirst {
                        it.getEncryptedPath() == model.getEncryptedPath()
                    }
                    SliderActivity.start(ctx, arrPhoto, position)
                }
                model.isVideo() || model.isAudio() -> { // Open media player if it's video or audio
                    if (model.isMediaSupported()) {
                        val arrSupportedVideo = arrayListOf<FileModel>()
                        arrAllFiles.filter {
                            (it.isVideo() || it.isAudio()) && it.isMediaSupported()
                        }.also { videos ->
                            arrSupportedVideo.addAll(videos)
                        }
                        val position = arrSupportedVideo.indexOfFirst {
                            it.getEncryptedPath() == model.getEncryptedPath()
                        }
                        MediaPlayerActivity.start(ctx, arrSupportedVideo, position)
                    } else {
                        ctx.createFileChooser(model)
                    }
                }
                else -> {
                    Intent(Intent.ACTION_SEND).apply {
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        type = model.mimeType
                        putExtra(
                            Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                                ctx, CommonUtil.getAppID() + ".fileprovider",
                                File(model.getEncryptedPath())
                            )
                        )
                    }.run {
                        startActivity(Intent.createChooser(this, ctx.getString(R.string.app_name)))
                    }
                }
            }
        }
    }

    private fun updateUI() {
        if (isChoosingFileToHide) { // Show Hide button if it's hiding mode
            vwBarAction.visible()
            btnHide.apply {
                if (isGone) {
                    visible()
                }

                if (arrAllFiles.any { it.isSelected }) {
                    setBackgroundResource(R.drawable.btn_accent_round)
                } else {
                    setBackgroundResource(R.drawable.btn_grey_round)
                }
            }
            lblHide.text =
                String.format(getString(R.string.hide_value), arrAllFiles.count { it.isSelected })
        } else { // Show/hide file actions if it's view mode
            if (vaultAdapter.isSelectableMode()) {
                if (vwBarAction.isGone) {
                    vwBarAction.visible()
                }
                if (cstActionFile.isGone) {
                    cstActionFile.visible()
                }

                if (arrAllFiles.any { it.isSelected }) {
                    btnDelete.setBackgroundResource(R.drawable.btn_accent_round_left)
                    btnUnhide.setBackgroundResource(R.drawable.btn_accent)
                    btnMove.setBackgroundResource(R.drawable.btn_accent_round_right)
                } else {
                    btnDelete.setBackgroundResource(R.drawable.btn_grey_round_left)
                    btnUnhide.setBackgroundResource(R.drawable.btn_grey)
                    btnMove.setBackgroundResource(R.drawable.btn_grey_round_right)
                }
            } else {
                if (vwBarAction.isVisible) {
                    vwBarAction.gone()
                }
                if (cstActionFile.isVisible) {
                    cstActionFile.gone()
                }
            }
        }

        // Update select UI
        if (vaultAdapter.isSelectableMode()) {
            if (arrFilteredFiles.isEmpty() || arrFilteredFiles.any { !it.isSelected }) {
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
        if (Constants.Layout.GRID == ctx?.appSettingsModel?.layoutTypeFile) {
            menuItemLayout?.setIcon(R.drawable.ic_menu_list)
        } else {
            menuItemLayout?.setIcon(R.drawable.ic_menu_grid)
        }
    }

    private fun notifyModeChangedToParentActivity() {
        modeListener?.onModeChanged(vaultAdapter.isSelectableMode())
    }

    private fun sortFiles(
        type: Int? = ctx?.appSettingsModel?.sortType,
        direction: Int? = ctx?.appSettingsModel?.sortDirection,
        refreshAdapter: Boolean = true
    ) {
        ctx?.appSettingsModel?.apply {
            sortType = type ?: Constants.SortType.DATE
            sortDirection = direction ?: Constants.SortType.DIRECTION_DESC
        }?.run {
            CommonUtil.saveAppSettingsModel(ctx, this)
        }
        when (type) {
            Constants.SortType.NAME -> {
                // Sort all files
                if (Constants.SortType.DIRECTION_ASC == direction) {
                    arrAllFiles.sortBy {
                        it.name
                    }
                } else {
                    arrAllFiles.sortByDescending {
                        it.name
                    }
                }

                // Sort filtered files
                if (Constants.SortType.DIRECTION_ASC == direction) {
                    arrFilteredFiles.sortBy {
                        it.name
                    }

                    // Update sort direction icon
                    imgSortDirection.rotate(180f)
                } else {
                    arrFilteredFiles.sortByDescending {
                        it.name
                    }

                    // Update sort direction icon
                    imgSortDirection.rotate(0f)
                }

                // Update label
                btnSort.text = getString(R.string.name)
            }
            Constants.SortType.SIZE -> {
                // Sort all files
                if (Constants.SortType.DIRECTION_ASC == direction) {
                    arrAllFiles.sortBy {
                        it.size
                    }
                } else {
                    arrAllFiles.sortByDescending {
                        it.size
                    }
                }

                // Sort filtered files
                if (Constants.SortType.DIRECTION_ASC == direction) {
                    arrFilteredFiles.sortBy {
                        it.size
                    }

                    // Update sort direction icon
                    imgSortDirection.rotate(180f)
                } else {
                    arrFilteredFiles.sortByDescending {
                        it.size
                    }

                    // Update sort direction icon
                    imgSortDirection.rotate(0f)
                }

                // Update label
                btnSort.text = getString(R.string.size)
            }
            else -> {
                // Sort all files
                if (Constants.SortType.DIRECTION_ASC == direction) {
                    arrAllFiles.sortBy {
                        it.modifiedDate
                    }
                } else {
                    arrAllFiles.sortByDescending {
                        it.modifiedDate
                    }
                }

                // Sort filtered files
                if (Constants.SortType.DIRECTION_ASC == direction) {
                    arrFilteredFiles.sortBy {
                        it.modifiedDate
                    }

                    // Update sort direction icon
                    imgSortDirection.rotate(180f)
                } else {
                    arrFilteredFiles.sortByDescending {
                        it.modifiedDate
                    }

                    // Update sort direction icon
                    imgSortDirection.rotate(0f)
                }

                // Update label
                btnSort.text = getString(R.string.date)
            }
        }
        if (refreshAdapter) {
            vaultAdapter.notifyDataSetChanged()
        }
    }

    private fun showChoosingFolderDialog(selectedModels: List<FileModel>) {
        val title = if (isChoosingFileToHide) {
            R.string.select_folder_to_save
        } else {
            R.string.select_folder_to_move
        }
        DialogUtil.showChoosingFolderDialog(ctx, arrFolder, title,
            okListener = { folder ->
                if (isChoosingFileToHide) { // Hiding file
                    for (model in selectedModels) {
                        model.parentId = folder.rowId
                    }
                    presenter.encryptFile(selectedModels)

                    // Keep screen on while encrypting
                    parentActivity.addScreenOnFlag()
                } else { // Moving file
                    if (selectedModels.all { it.parentId == folder.rowId }) {
                        ctx?.toast(R.string.msg_moved_success)
                    } else {
                        for (model in selectedModels) {
                            model.parentId = folder.rowId
                        }
                        moveFiles(selectedModels)
                    }
                }
            },
            newFolderListener = {
                showNewFolderDialog()
            }
        )
    }

    private fun showRenameDialog(model: FileModel) {
        externalStoragePermissionUtil.requestPermission(object :
            ExternalStoragePermissionUtil.Listener {
            override fun onGranted() {
                DialogUtil.showRenameDialog(ctx, model) { dialog, newName ->
                    val shouldUpdate: Boolean
                    val name = if (model.name.contains(".")) {
                        shouldUpdate = model.name.substringBeforeLast(".") != newName

                        "$newName.${model.name.substringAfterLast(".")}"
                    } else {
                        shouldUpdate = model.name != newName

                        newName
                    }
                    if (shouldUpdate) {
                        model.name = name
                        presenter.rename(model)
                    }

                    dialog.dismiss()
                }
            }

            override fun onDenied() {
                ctx?.toast(R.string.msg_permission_storage)
            }
        })
    }

    private fun showUnhideDialog(selectedModels: List<FileModel>) {
        externalStoragePermissionUtil.requestPermission(object :
            ExternalStoragePermissionUtil.Listener {
            override fun onGranted() {
                DialogUtil.showUnhideDialog(ctx, selectedModels.size) { isOriginalPath ->
                    presenter.unhideFiles(selectedModels, isOriginalPath)

                    // Save user's choice
                    ctx?.appSettingsModel?.apply {
                        shouldUnhideToOriginalPath = isOriginalPath
                    }?.run {
                        CommonUtil.saveAppSettingsModel(ctx, this)
                    }

                    // Keep screen on while encrypting
                    parentActivity.addScreenOnFlag()
                }
            }

            override fun onDenied() {
                ctx?.toast(R.string.msg_permission_storage)
            }
        })
    }

    private fun moveFiles(models: List<FileModel>) {
        externalStoragePermissionUtil.requestPermission(object :
            ExternalStoragePermissionUtil.Listener {
            override fun onGranted() {
                presenter.moveFiles(models)
            }

            override fun onDenied() {
                ctx?.toast(R.string.msg_permission_storage)
            }
        })
    }

    private fun showDeleteFilesDialog(models: List<FileModel>) {
        externalStoragePermissionUtil.requestPermission(object :
            ExternalStoragePermissionUtil.Listener {
            override fun onGranted() {
                val title = String.format(getString(R.string.delete_title), models.size)
                DialogUtil.showConfirmationDialog(
                    ctx, textTitle = title, textMessage = R.string.delete_message,
                    textCancel = R.string.cancel, textOk = R.string.delete, cancelable = true,
                    okListener = {
                        presenter.deleteFiles(models)
                    }
                )
            }

            override fun onDenied() {
                ctx?.toast(R.string.msg_permission_storage)
            }
        })
    }

    private fun getFoldersToSave(selectedModels: List<FileModel>) {
        externalStoragePermissionUtil.requestPermission(object :
            ExternalStoragePermissionUtil.Listener {
            override fun onGranted() {
                presenter.getFolders(selectedModels)
            }

            override fun onDenied() {
                ctx?.toast(R.string.msg_permission_storage)
            }
        })
    }

    private fun showNewFolderDialog() {
        DialogUtil.showNewFolderDialog(ctx) { dialog, folderName ->
            if (arrFolder.all { it.name.lowercase() != folderName.lowercase() }) {
                FileModel().apply {
                    name = folderName
                    isDirectory = true
                }.run {
                    presenter.addNewFolder(this)
                }

                dialog.dismiss()
            } else {
                ctx?.toast(R.string.err_folder_exists_already)
            }
        }
    }

    private fun getFolderDetail(model: FileModel) {
        externalStoragePermissionUtil.requestPermission(object :
            ExternalStoragePermissionUtil.Listener {
            override fun onGranted() {
                presenter.getFolderDetail(model)
            }

            override fun onDenied() {
            }
        })
    }

    fun resetMode() {
        vaultAdapter.switchMode()
        updateUI()
    }

    fun onBucketSelected(bucket: FileModel) {
        arrFilteredFiles.clear()
        if (null == bucket.mimeType) {
            arrFilteredFiles.addAll(arrAllFiles)
        } else {
            arrFilteredFiles.addAll(arrAllFiles.filter {
                it.bucketName == bucket.bucketName
            })
        }
        vaultAdapter.notifyDataSetChanged()
        updateUI()
    }

    private fun publishFolderChanged() {
        RxBus.publishFolderChanged()
    }

    private fun showInterstitialAd(onAdDismissed: () -> Unit) {
        ctx?.adsManager?.showInterstitialAd(parentActivity) {
            onAdDismissed()
        }
    }

    interface ModeListener {
        fun onModeChanged(selectable: Boolean)

        fun onMediaFilesLoadedSuccess(buckets: ArrayList<FileModel>)
    }
}