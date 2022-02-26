package com.hide.videophoto.ui.dashboard

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hide.videophoto.R
import com.hide.videophoto.common.ext.*
import com.hide.videophoto.common.util.CommonUtil
import com.hide.videophoto.common.util.DialogUtil
import com.hide.videophoto.common.util.EventTrackingManager
import com.hide.videophoto.common.util.ExternalStoragePermissionUtil
import com.hide.videophoto.data.model.FileModel
import com.hide.videophoto.ui.base.BaseFragment
import com.hide.videophoto.ui.vault.VaultActivity

class FolderFragment : BaseFragment<FolderView, FolderPresenterImp>(), FolderView {

    private lateinit var rclFolder: RecyclerView

    private val externalStoragePermissionUtil = ExternalStoragePermissionUtil(self)
    private val arrFolder by lazy { arrayListOf<FileModel>() }
    private lateinit var folderAdapter: FolderAdapter

    override fun initView(): FolderView {
        return this
    }

    override fun initPresenter(): FolderPresenterImp {
        return FolderPresenterImp(ctx!!)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_folder
    }

    override fun initWidgets(rootView: View) {
        // Find views
        rootView.run {
            // Init recyclerView
            rclFolder = findViewById(R.id.rcl_folder)
            rclFolder.apply {
                layoutManager = GridLayoutManager(
                    ctx, ctx.appSettingsModel.layoutTypeFolder, GridLayoutManager.VERTICAL, false
                )
                setHasFixedSize(true)
                folderAdapter = FolderAdapter(parentActivity, arrFolder,
                    itemClickListener = { model ->
                        VaultActivity.start(ctx, isShowingFileByFolder = true, fileModel = model)
                    },
                    renameListener = {
                        showRenameDialog(it)
                    },
                    unhideListener = {
                        showUnhideDialog(it)
                    },
                    deletingListener = {
                        showDeleteFilesDialog(it)
                    },
                    detailListener = {
                        getFolderSize(it)
                    }
                )
                adapter = folderAdapter
            }
        }

        // Get folders from DB
        getFolders()

        // Listen folder changed event
        presenter.listenFolderChangedEvent()
    }

    override fun onFoldersLoadedSuccess(folders: List<FileModel>) {
        fillFolders(folders)
    }

    override fun onFolderAddedSuccess(folder: FileModel) {
        fillFolders(listOf(folder))

        // Event tracking
        ctx?.eventTracking?.logEvent(EventTrackingManager.CREATE_NEW_FOLDER)
    }

    override fun onFolderRenamedSuccess(model: FileModel) {
        folderAdapter.notifyItemChanged(arrFolder.indexOf(model))
    }

    override fun onFilesDeletedSuccess(folder: FileModel) {
        if (folder.itemQuantity > 0) {
            showInterstitialAd {
                // Show success message
                ctx?.toast(R.string.msg_deleted_success)
            }
        } else {
            // Show success message
            ctx?.toast(R.string.msg_deleted_success)
        }

        // Update UI
        val isAlsoDeleteFolder = arrFolder.size > 1
        if (isAlsoDeleteFolder) {
            val deletedIndex = arrFolder.indexOf(folder)
            arrFolder.removeAt(deletedIndex)
            folderAdapter.notifyItemRemoved(deletedIndex)
        } else {
            folder.itemQuantity = 0
            folderAdapter.notifyItemChanged(arrFolder.indexOf(folder))
        }
    }

    override fun onFilesUnhiddenSuccess(folder: FileModel) {
        folder.itemQuantity = 0
        folderAdapter.notifyItemChanged(arrFolder.indexOf(folder))

        // Show success message
        showInterstitialAd {
            ctx?.toast(R.string.msg_unhide_success)
        }

        // Clear screen on flag after done
        parentActivity.clearScreenOnFlag()
    }

    override fun onGettingFolderSizeSuccess(folder: FileModel, totalSize: Long) {
        DialogUtil.showFolderDetailDialog(ctx, folder, totalSize)
    }

    override fun onFolderChangedEvent() {
        // Refresh folder list
        arrFolder.clear()
        folderAdapter.notifyDataSetChanged()
        getFolders()
    }

    private fun getFolders() {
        externalStoragePermissionUtil.requestPermission(object :
            ExternalStoragePermissionUtil.Listener {
            override fun onGranted() {
                presenter.getFolders()
            }

            override fun onDenied() {
            }
        })
    }

    private fun fillFolders(folders: List<FileModel>) {
        arrFolder.addAll(0, folders)
        if (arrFolder.isNotEmpty()) {
            // Notify data changed
            folderAdapter.notifyItemRangeInserted(0, folders.size)
            rclFolder.smoothScrollToPosition(0)
        }
    }

    fun switchLayout() {
        folderAdapter.switchLayout()
    }

    fun showNewFolderDialog() {
        externalStoragePermissionUtil.requestPermission(object :
            ExternalStoragePermissionUtil.Listener {
            override fun onGranted() {
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

            override fun onDenied() {
            }
        })
    }

    private fun showRenameDialog(model: FileModel) {
        externalStoragePermissionUtil.requestPermission(object :
            ExternalStoragePermissionUtil.Listener {
            override fun onGranted() {
                DialogUtil.showRenameDialog(ctx, model) { dialog, newName ->
                    when {
                        model.name.equals(newName, true) -> {
                            dialog.dismiss()
                        }
                        arrFolder.all { it.name.lowercase() != newName.lowercase() } -> {
                            model.name = newName
                            presenter.rename(model)

                            dialog.dismiss()
                        }
                        else -> {
                            ctx?.toast(R.string.err_folder_exists_already)
                        }
                    }
                }
            }

            override fun onDenied() {
                ctx?.toast(R.string.msg_permission_storage)
            }
        })
    }

    private fun showUnhideDialog(folder: FileModel) {
        if (folder.itemQuantity > 0) {
            externalStoragePermissionUtil.requestPermission(object :
                ExternalStoragePermissionUtil.Listener {
                override fun onGranted() {
                    DialogUtil.showUnhideDialog(ctx, folder.itemQuantity) { isOriginalPath ->
                        presenter.unhideFolder(folder, isOriginalPath)

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
        } else {
            ctx?.toast(R.string.no_file_found)
        }
    }

    private fun showDeleteFilesDialog(folder: FileModel) {
        val isAlsoDeleteFolder = arrFolder.size > 1
        if (folder.itemQuantity > 0) {
            externalStoragePermissionUtil.requestPermission(object :
                ExternalStoragePermissionUtil.Listener {
                override fun onGranted() {
                    val title = String.format(getString(R.string.delete_title), folder.itemQuantity)
                    DialogUtil.showConfirmationDialog(
                        ctx, textTitle = title, textMessage = R.string.delete_message,
                        textCancel = R.string.cancel, textOk = R.string.delete, cancelable = true,
                        okListener = {
                            presenter.deleteFolder(folder, isAlsoDeleteFolder)
                        }
                    )
                }

                override fun onDenied() {
                    ctx?.toast(R.string.msg_permission_storage)
                }
            })
        } else {
            if (isAlsoDeleteFolder) {
                externalStoragePermissionUtil.requestPermission(object :
                    ExternalStoragePermissionUtil.Listener {
                    override fun onGranted() {
                        presenter.deleteFolder(folder, isAlsoDeleteFolder)
                    }

                    override fun onDenied() {
                        ctx?.toast(R.string.msg_permission_storage)
                    }
                })
            } else { // Don't let user delete if there is only one folder exists
                ctx?.toast(R.string.msg_cant_delete_last_folder)
            }
        }
    }

    private fun getFolderSize(model: FileModel) {
        externalStoragePermissionUtil.requestPermission(object :
            ExternalStoragePermissionUtil.Listener {
            override fun onGranted() {
                presenter.getFolderSize(model)
            }

            override fun onDenied() {
            }
        })
    }

    private fun showInterstitialAd(onAdDismissed: () -> Unit) {
        ctx?.adsManager?.showInterstitialAd(parentActivity) {
            onAdDismissed()
        }
    }
}