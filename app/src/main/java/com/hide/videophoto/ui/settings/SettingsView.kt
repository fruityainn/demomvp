package com.hide.videophoto.ui.settings

import com.hide.videophoto.data.model.EventAppSettingsModel
import com.hide.videophoto.ui.base.BaseView

interface SettingsView : BaseView {
    fun onAppSettingsChanged(model: EventAppSettingsModel)
}