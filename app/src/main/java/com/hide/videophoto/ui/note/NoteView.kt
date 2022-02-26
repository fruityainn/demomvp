package com.hide.videophoto.ui.note

import com.hide.videophoto.data.model.EventNoteModel
import com.hide.videophoto.data.model.FileModel
import com.hide.videophoto.ui.base.BaseView

interface NoteView : BaseView {
    fun onNotesLoadedSuccess(notes: List<FileModel>)

    fun onNotesDeletedSuccess(models: List<FileModel>)

    fun onNotesExportedSuccess(models: List<FileModel>)

    fun onNoteChangedEvent(eventNoteModel: EventNoteModel)
}