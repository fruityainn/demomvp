package com.hide.videophoto.ui.main

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hide.videophoto.R
import com.hide.videophoto.common.Constants
import com.hide.videophoto.common.ext.*
import com.hide.videophoto.common.util.*
import com.hide.videophoto.data.model.EventAppStateChangeModel
import com.hide.videophoto.data.model.FileModel
import com.hide.videophoto.ui.base.BaseActivity
import com.hide.videophoto.ui.note.AddingNoteActivity
import com.hide.videophoto.ui.security.password.PasswordActivity
import com.hide.videophoto.ui.vault.VaultActivity
import java.io.File
import java.io.IOException

class MainActivity : BaseActivity<MainView, MainPresenterImp>(), MainView {

    private val pagerMain by lazy { findViewById<ViewPager2>(R.id.pager_main) }
    private val bottomNavMain by lazy { findViewById<BottomNavigationView>(R.id.bottom_nav_main) }
    private val frlGuideAdding by lazy { findViewById<FrameLayout>(R.id.frl_guide) }
    private val vwCover by lazy { findViewById<View>(R.id.vw_cover) }
    private val imgAdd by lazy { findViewById<ImageView>(R.id.img_add) }

    private var animationGuideAdding: Animation? = null
    private var fileToHide: FileModel? = null
    private val arrFolder by lazy { arrayListOf<FileModel>() }
    private var capturedPhotoPath: String? = null
    private val filePicker = FilePicker(self)
    private val cameraPermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { maps ->
            val isGranted = maps.values.isNotEmpty() && maps.values.all { result ->
                result == true
            }
            if (isGranted) {
                takePhoto()
            }
        }
    private val cameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            with(it) {
                if (resultCode == Activity.RESULT_OK) {
                    capturedPhotoPath?.also { path ->
                        getUriFromString(path)?.also { uri ->
                            // Refresh MediaStore to avoid getting file detail error
                            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri)
                            sendBroadcast(mediaScanIntent)

                            // Wait a moment before getting file detail from MediaStore
                            presenter.delayBeforeDoing {
                                presenter.getImageFileDetail(uri)
                            }
                        }
                    }
                }
            }
        }

    private val dialogAddingOpt by lazy {
        DialogOptionAdding(ctx, R.style.BottomSheetDialogTheme,
            videoOpt = {
                pickMediaFile(Constants.DataType.VIDEO)
            },
            photoOpt = {
                pickMediaFile(Constants.DataType.IMAGE)
            },
            cameraOpt = {
                takePhoto()
            },
            audioOpt = {
                pickMediaFile(Constants.DataType.AUDIO)
            },
            noteOpt = {
                AddingNoteActivity.start(ctx)
            },
            otherOpt = {
                pickFile()
            }
        )
    }

    private val dialogExitApp by lazy {
        DialogExitApp(ctx, R.style.BottomSheetDialogTheme) {
            // Exit app
            finish()
        }
    }

    companion object {
        private const val PARAM_SHOW_ADDING_OPTION = "show_adding_option"

        private const val HOME_INDEX = 0
        private const val SETTINGS_INDEX = 1

        fun start(ctx: Context, showAddingOption: Boolean = false) {
            Intent(ctx, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(PARAM_SHOW_ADDING_OPTION, showAddingOption)
            }.run {
                ctx.startActivity(this)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Count app opened
        appSettingsModel.apply {
            countAppOpened += 1
        }.run {
            CommonUtil.saveAppSettingsModel(ctx, this)
        }

        // Show guide if it's the first time app opened
        if (appSettingsModel.countAppOpened == 1) {
            toggleGuideAdding(View.VISIBLE)
        }

        presenter.listenAppStateChanged()
    }

    override fun onStart() {
        super.onStart()

        // Load native ads
        adsManager.apply {
            loadInterstitialAd()
            loadNativeAdAddingOptions()
            loadNativeAdExitApp()
        }
    }

    override fun onResume() {
        super.onResume()

        // Checking if user has just hidden a file from Vault page
        if (appSettingsModel.shouldCheckShowingRateDialog) {
            if (shouldShowRatingDialog()) {
                DialogUtil.showRatingDialog(ctx, true)
            }

            appSettingsModel.shouldCheckShowingRateDialog = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Logout auth
        authModel.logOut()

        // Release ads config
        adsManager.release()
    }

    override fun onBackPressed() {
        dialogExitApp.showDialog()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent?.extras?.run {
            if (containsKey(PARAM_SHOW_ADDING_OPTION)) {
                val flag = getBoolean(PARAM_SHOW_ADDING_OPTION)
                if (flag) {
                    showAddingOptionsDialog()
                }
            }
        }
    }

    override fun initView(): MainView {
        return this
    }

    override fun initPresenter(): MainPresenterImp {
        return MainPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_main
    }

    override fun initWidgets() {
        // Init toolbar
        hideToolbarBase()

        // Init pager
        pagerMain.apply {
            adapter = MainPagerAdapter(self)
            isUserInputEnabled = false
        }

        // Init bottom nav
        bottomNavMain.itemIconTintList = null
        bottomNavMain.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    switchPage(HOME_INDEX)

                    return@setOnItemSelectedListener true
                }
                R.id.nav_add -> {
                    showAddingOptionsDialog()

                    return@setOnItemSelectedListener false
                }
                R.id.nav_settings -> {
                    switchPage(SETTINGS_INDEX)

                    return@setOnItemSelectedListener true
                }
            }
            false
        }
    }

    override fun onDestroyAds() {
        adsManager.apply {
            destroyNativeAdAddingOptions()
            destroyNativeAdExitApp()
        }
    }

    override fun onGettingFileDetailSuccess(model: FileModel) {
        fileToHide = model

        // Ask user to choose folder to save the file
        getFolders()
    }

    override fun onGettingFileDetailError() {
        capturedPhotoPath?.run {
            toast(R.string.err_common, true)

            // Delete temp file captured by camera
            val tempFile = File(this)
            if (tempFile.exists()) {
                tempFile.delete()
            }

            // Delete from MediaStore
            getUriFromString(capturedPhotoPath)?.run {
                val selection =
                    "${MediaStore.MediaColumns._ID} = ? OR ${MediaStore.MediaColumns.DATA} LIKE ?"
                val id = ctx.getIdFromUri(this)
                val selectionArgs: Array<String> = arrayOf(id, "%$id")
                val baseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                ctx.applicationContext.contentResolver.delete(baseUri, selection, selectionArgs)
            } ?: run {
                DialogUtil.showConfirmationDialog(
                    ctx, textMessage = R.string.err_have_no_access_to_file
                )
            }
        }

        // Event tracking
        eventTracking.logEvent(EventTrackingManager.ERROR_GET_FILE_DETAIL)
    }

    override fun onFileEncryptedSuccess(models: List<FileModel>) {
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

        // Show success message
        adsManager.showInterstitialAd(self) {
            toast(R.string.msg_hid_success)

            // Show rating dialog
            if (shouldShowRatingDialog()) {
                DialogUtil.showRatingDialog(ctx, true)
            }
        }

        // Clear encrypted files
        fileToHide = null

        // Publish folder changed event
        RxBus.publishFolderChanged()

        // Assign captured photo path to null after hiding successfully
        capturedPhotoPath = null

        // Clear screen on flag after done
        clearScreenOnFlag()
    }

    override fun onFoldersLoadedSuccess(folders: List<FileModel>) {
        arrFolder.clear()
        arrFolder.addAll(folders)

        // Show folders dialog to select
        showChoosingFolderDialog()
    }

    override fun onFolderAddedSuccess(folder: FileModel) {
        // Ask user to choose folder to save the file
        getFolders()

        // Publish folder changed event
        RxBus.publishFolderChanged()

        // Event tracking
        eventTracking.logEvent(EventTrackingManager.CREATE_NEW_FOLDER)
    }

    override fun onAppStateChanged(model: EventAppStateChangeModel) {
        PasswordActivity.start(self, isFromBackground = true)
    }

    private fun getFolders() {
        fileToHide?.run {
            presenter.getFolders()
        }
    }

    private fun switchPage(pageIndex: Int) {
        pagerMain.setCurrentItem(pageIndex, false)
    }

    private fun showAddingOptionsDialog() {
        dialogAddingOpt.showDialog()
    }

    private fun pickFile() {
        filePicker.pick(listener = object : FilePicker.Listener {
            override fun onFilePicked(uri: Uri) {
                val mimiType = getMimeTypeFromUri(uri)
                when {
                    mimiType?.startsWith(Constants.DataType.VIDEO) == true -> {
                        presenter.getVideoFileDetail(uri)
                    }
                    mimiType?.startsWith(Constants.DataType.IMAGE) == true -> {
                        presenter.getImageFileDetail(uri)
                    }
                    mimiType?.startsWith(Constants.DataType.AUDIO) == true -> {
                        presenter.getAudioFileDetail(uri)
                    }
                    else -> {
                        presenter.getOtherFileDetail(uri)
                    }
                }
            }
        })
    }

    private fun pickMediaFile(type: String) {
        VaultActivity.start(ctx, isChoosingFileToHide = true, dataType = type)
    }

    private fun takePhoto() {
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            if (PermissionUtil.isGranted(
                    self,
                    arrayOf(Manifest.permission.CAMERA),
                    cameraPermissionResult
                )
            ) {
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                    // Ensure that there's a camera activity to handle the intent
                    takePictureIntent.resolveActivity(packageManager)?.also {
                        // Create the File where the photo should go
                        val photoFile: File? = try {
                            createImageFile()
                        } catch (ex: IOException) {
                            // Error occurred while creating the File
                            null
                        }
                        // Continue only if the File was successfully created
                        photoFile?.also {
                            capturedPhotoPath = it.absolutePath
                            val capturedPhotoUri = FileProvider.getUriForFile(
                                this, "com.hide.videophoto.fileprovider", it
                            )
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedPhotoUri)
                            cameraResultLauncher.launch(takePictureIntent)
                        }
                    }
                }
            }
        } else {
            toast(R.string.err_no_camera_found)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        /*val storageDir: File? =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)*/
        val storageDir = File(Constants.CAPTURED_FOLDER)
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        return File(
            storageDir, "Captured_${
                DateTimeUtil.convertTimeStampToDate(
                    currentTimeInSecond, DateTimeUtil.YYYYMMDD_HHMMSS
                )
            }.jpg"
        )
    }

    private fun showChoosingFolderDialog() {
        DialogUtil.showChoosingFolderDialog(ctx, arrFolder,
            okListener = { folder ->
                fileToHide?.apply {
                    parentId = folder.rowId
                }?.run {
                    presenter.encryptFile(arrayListOf(this))

                    // Keep screen on while encrypting
                    addScreenOnFlag()
                }
            },
            newFolderListener = {
                showNewFolderDialog()
            }
        )
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
                toast(R.string.err_folder_exists_already)
            }
        }
    }

    private fun toggleGuideAdding(visibility: Int) {
        if (visibility == View.VISIBLE) {
            vwCover.visible()
            frlGuideAdding.visible()
            imgAdd.visible()

            vwCover.setOnSafeClickListener {
                toggleGuideAdding(View.GONE)
            }

            imgAdd.setOnSafeClickListener {
                toggleGuideAdding(View.GONE)

                showAddingOptionsDialog()
            }

            animationGuideAdding = AnimationUtils.loadAnimation(ctx, R.anim.anim_guide_adding)
            animationGuideAdding?.fillAfter = true
            frlGuideAdding.startAnimation(animationGuideAdding)
        } else {
            vwCover.gone()
            frlGuideAdding.gone()
            imgAdd.gone()

            animationGuideAdding?.run {
                frlGuideAdding.clearAnimation()

                cancel()
                reset()
            }
            animationGuideAdding = null
        }
    }
}