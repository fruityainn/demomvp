package com.hide.videophoto.ui.security.email

import com.hide.videophoto.ui.base.BaseView

interface EmailView : BaseView {

    fun onSettingEmailSuccess()

    fun onQueryDbError()
}