package com.hide.videophoto.ui.dashboard

import com.hide.videophoto.data.model.EventAppSettingsModel
import com.hide.videophoto.ui.base.BaseView

interface HomeView : BaseView {
    fun onAppSettingsChanged(model: EventAppSettingsModel)
}