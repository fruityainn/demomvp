package com.hide.videophoto.ui.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hide.videophoto.R
import com.hide.videophoto.common.ext.adsManager
import com.hide.videophoto.common.ext.setOnSafeClickListener

class DialogExitApp(private val ctx: Context, theme: Int, private val onExit: () -> Unit) :
    BottomSheetDialog(ctx, theme) {

    private lateinit var frlNativeAdExit: FrameLayout
    private lateinit var btnExit: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        onExit()
    }

    fun showDialog() {
        if (!isShowing) {
            show()

            // Show native ad
            ctx.adsManager.showNativeAdExitApp(frlNativeAdExit)

            // Transparent window background
            window?.findViewById<FrameLayout>(R.id.design_bottom_sheet)
                ?.setBackgroundResource(R.color.transparent)
        }
    }

    private fun initView() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_exit_app, null)
        view.run {
            frlNativeAdExit = findViewById(R.id.frl_native_ad_exit)
            btnExit = findViewById(R.id.btn_exit)
        }
        setContentView(view)

        // Listener
        btnExit.setOnSafeClickListener {
            if (isShowing) {
                dismiss()
            }

            // Exit app
            onExit()
        }
    }
}