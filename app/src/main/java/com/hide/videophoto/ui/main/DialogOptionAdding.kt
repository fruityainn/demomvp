package com.hide.videophoto.ui.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hide.videophoto.R
import com.hide.videophoto.common.ext.adsManager
import com.hide.videophoto.common.ext.setOnSafeClickListener

class DialogOptionAdding(
    private val ctx: Context, theme: Int,
    private val videoOpt: () -> Unit,
    private val photoOpt: () -> Unit,
    private val cameraOpt: () -> Unit,
    private val audioOpt: () -> Unit,
    private val noteOpt: () -> Unit,
    private val otherOpt: () -> Unit
) : BottomSheetDialog(ctx, theme) {

    private lateinit var frlNativeAdAdding: FrameLayout
    private lateinit var frlVideo: FrameLayout
    private lateinit var frlPhoto: FrameLayout
    private lateinit var frlCamera: FrameLayout
    private lateinit var frlAudio: FrameLayout
    private lateinit var frlNote: FrameLayout
    private lateinit var frlOther: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        initView()
    }

    fun showDialog() {
        if (!isShowing) {
            show()

            // Show native ad
            ctx.adsManager.showNativeAdAddingOptions(frlNativeAdAdding)

            // Transparent window background
            window?.findViewById<FrameLayout>(R.id.design_bottom_sheet)
                ?.setBackgroundResource(R.color.transparent)
        }
    }

    private fun initView() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_option_adding, null)
        view.run {
            frlNativeAdAdding = findViewById(R.id.frl_native_ad_adding)
            frlVideo = findViewById(R.id.frl_add_video)
            frlPhoto = findViewById(R.id.frl_add_photo)
            frlCamera = findViewById(R.id.frl_add_camera)
            frlAudio = findViewById(R.id.frl_add_audio)
            frlNote = findViewById(R.id.frl_add_note)
            frlOther = findViewById(R.id.frl_add_other)
        }
        setContentView(view)

        // Listener
        frlVideo.setOnSafeClickListener {
            videoOpt()
            dismissOptions()
        }

        frlPhoto.setOnSafeClickListener {
            photoOpt()
            dismissOptions()
        }

        frlCamera.setOnSafeClickListener {
            cameraOpt()
            dismissOptions()
        }

        frlAudio.setOnSafeClickListener {
            audioOpt()
            dismissOptions()
        }

        frlNote.setOnSafeClickListener {
            noteOpt()
            dismissOptions()
        }

        frlOther.setOnSafeClickListener {
            otherOpt()
            dismissOptions()
        }
    }

    private fun dismissOptions() {
        if (isShowing) {
            dismiss()
        }
    }
}