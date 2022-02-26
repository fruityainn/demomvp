package com.hide.videophoto.ui.main

import com.hide.videophoto.data.model.EventAppStateChangeModel
import com.hide.videophoto.data.model.FileModel
import com.hide.videophoto.ui.base.BaseView

interface MainView : BaseView {
    fun onGettingFileDetailSuccess(model: FileModel)

    fun onGettingFileDetailError()

    fun onFileEncryptedSuccess(models: List<FileModel>)

    fun onFoldersLoadedSuccess(folders: List<FileModel>)

    fun onFolderAddedSuccess(folder: FileModel)

    fun onAppStateChanged(model: EventAppStateChangeModel)
}