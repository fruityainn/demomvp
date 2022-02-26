package com.hide.videophoto.ui.vault

import com.hide.videophoto.data.model.FileModel
import com.hide.videophoto.ui.base.BaseView

interface VaultView : BaseView {
    fun onFilesLoadedSuccess(files: List<FileModel>)

    fun onFoldersLoadedSuccess(folders: List<FileModel>, selectedModels: List<FileModel>)

    fun onFolderAddedSuccess(folder: FileModel)

    fun onFilesEncryptedSuccess(models: List<FileModel>)

    fun onFilesMovedSuccess(models: List<FileModel>)

    fun onFileRenamedSuccess(model: FileModel)

    fun onFilesDeletedSuccess(models: List<FileModel>)

    fun onFilesUnhiddenSuccess(models: List<FileModel>)

    fun onFolderDetailLoadedSuccess(file: FileModel, folder: FileModel)
}