package com.hide.videophoto.widget

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import com.hide.videophoto.R

class ProgressDialogPercentage(private val ctx: Context) : Dialog(ctx) {

    private val pbPercentage by lazy { findViewById<PercentageView>(R.id.pb_percentage) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawableResource(R.color.transparent)
        setContentView(R.layout.layout_progressbar_percentage)
    }

    fun show(duration: Int) {
        if (!isShowing) {
            show()
            pbPercentage.setDuration(duration)
        }
    }
}
