package com.hide.videophoto.common.ext

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.hide.videophoto.R

fun ImageView.loadImage(source: Any?, radius: Int = 0, errImg: Int = R.drawable.ic_photo) {
    val requestOptions = if (radius > 0) {
        RequestOptions().transform(CenterCrop(), RoundedCorners(radius))
    } else {
        null
    }

    requestOptions?.also { options ->
        Glide.with(ctx).load(source)
            .apply(options)
            .error(errImg)
            .into(this)
    } ?: run {
        Glide.with(ctx).load(source)
            .error(errImg)
            .into(this)
    }
}

fun SubsamplingScaleImageView.loadImage(source: Any?) {
    when (source) {
        is String -> setImage(ImageSource.uri(source))
    }
}