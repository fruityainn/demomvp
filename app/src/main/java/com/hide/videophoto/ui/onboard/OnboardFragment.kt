package com.hide.videophoto.ui.onboard

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.hide.videophoto.R
import com.hide.videophoto.common.ext.ctx
import com.hide.videophoto.common.ext.loadImage
import com.hide.videophoto.ui.base.BaseFragment

class OnboardFragment : BaseFragment<OnboardView, OnboardPresenterImp>(), OnboardView {

    private lateinit var imgOnboard: ImageView
    private lateinit var lblTitle: TextView
    private lateinit var lblDesc: TextView

    private var imgRes: Int? = null
    private var titleRes: Int? = null
    private var descRes: Int? = null

    companion object {
        private const val PARAM_IMAGE = "param_image"
        private const val PARAM_TITLE = "param_title"
        private const val PARAM_DESC = "param_desc"

        fun newInstance(imgRes: Int, titleRes: Int, descRes: Int): OnboardFragment {
            return OnboardFragment().apply {
                arguments = Bundle().apply {
                    putInt(PARAM_IMAGE, imgRes)
                    putInt(PARAM_TITLE, titleRes)
                    putInt(PARAM_DESC, descRes)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { arg ->
            imgRes = arg.getInt(PARAM_IMAGE)
            titleRes = arg.getInt(PARAM_TITLE)
            descRes = arg.getInt(PARAM_DESC)
        }
    }

    override fun initView(): OnboardView {
        return this
    }

    override fun initPresenter(): OnboardPresenterImp {
        return OnboardPresenterImp(ctx!!)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_onboard
    }

    override fun initWidgets(rootView: View) {
        // Find views
        rootView.run {
            imgOnboard = findViewById(R.id.img_onboard)
            lblTitle = findViewById(R.id.lbl_title)
            lblDesc = findViewById(R.id.lbl_desc)
        }

        // Fill data
        imgRes?.run {
            imgOnboard.loadImage(this)
        }
        titleRes?.run {
            lblTitle.setText(this)
        }
        descRes?.run {
            lblDesc.setText(this)
        }
    }
}