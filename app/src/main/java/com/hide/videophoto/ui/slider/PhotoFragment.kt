package com.hide.videophoto.ui.slider

import android.content.Context
import android.os.Bundle
import android.view.View
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.hide.videophoto.R
import com.hide.videophoto.common.ext.*
import com.hide.videophoto.data.model.FileModel
import com.hide.videophoto.ui.base.BaseFragment

class PhotoFragment : BaseFragment<PhotoView, PhotoPresenterImp>(), PhotoView {

    private lateinit var imgPhoto: SubsamplingScaleImageView

    private var fileModel: FileModel? = null
    private var listener: Listener? = null

    companion object {
        private const val PARAM_FILE_MODEL = "param_file_model"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param fileModel Image model to show.
         * @return A new instance of fragment PhotoFragment.
         */
        fun newInstance(fileModel: FileModel? = null): PhotoFragment {
            return PhotoFragment().apply {
                arguments = Bundle().apply {
                    fileModel?.also { model ->
                        putSerializable(PARAM_FILE_MODEL, model)
                    }
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as Listener?
        } catch (e: ClassCastException) {
            logE(e.message)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { arg ->
            fileModel = arg.getSerializable(PARAM_FILE_MODEL) as FileModel?
        }
    }

    override fun initView(): PhotoView {
        return this
    }

    override fun initPresenter(): PhotoPresenterImp {
        return PhotoPresenterImp(ctx!!)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_photo
    }

    override fun initWidgets(rootView: View) {
        // Find views
        rootView.run {
            imgPhoto = findViewById(R.id.img_photo)
        }

        // Fill image if it's available
        fileModel?.also { model ->
            try {
                imgPhoto.apply {
                    if (model.orientation ?: 0 > 0) {
                        orientation = model.orientation ?: 0
                    }
                    maxScale = 5f
                    loadImage(model.getEncryptedPath())

                    // Click listener
                    setOnSafeClickListener {
                        listener?.onPhotoClicked(model)
                    }
                }
            } catch (e: Exception) {
                ctx?.toast("Can not load this content")
            }
        }
    }

    interface Listener {
        fun onPhotoClicked(model: FileModel)
    }
}