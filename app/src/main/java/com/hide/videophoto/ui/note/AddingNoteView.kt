package com.hide.videophoto.ui.note

import com.hide.videophoto.data.model.FileModel
import com.hide.videophoto.ui.base.BaseView

interface AddingNoteView : BaseView {
    fun onNoteAddedSuccess(model: FileModel)

    fun onNoteEditedSuccess(model: FileModel)

    fun onQueryDbError()
}