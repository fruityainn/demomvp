package com.hide.videophoto.widget

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import com.hide.videophoto.R

class MyProgressDialog(ctx: Context) : Dialog(ctx) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawableResource(R.color.transparent)
        setContentView(R.layout.layout_progressbar)
    }
}
