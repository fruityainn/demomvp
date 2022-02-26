package com.hide.videophoto.ui.dashboard

import com.hide.videophoto.data.model.FileModel
import com.hide.videophoto.ui.base.BaseView

interface FolderView : BaseView {
    fun onFoldersLoadedSuccess(folders: List<FileModel>)

    fun onFolderAddedSuccess(folder: FileModel)

    fun onFolderRenamedSuccess(model: FileModel)

    fun onFilesDeletedSuccess(folder: FileModel)

    fun onFilesUnhiddenSuccess(folder: FileModel)

    fun onGettingFolderSizeSuccess(folder: FileModel, totalSize: Long)

    fun onFolderChangedEvent()
}