package com.hide.videophoto.common.ext

import android.content.Context
import android.view.MenuItem
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.hide.videophoto.R
import com.hide.videophoto.common.util.SafeOnClickListener

inline val View.ctx: Context
    get() = context

var TextView.textColor: Int
    get() = currentTextColor
    set(value) = setTextColor(ContextCompat.getColor(ctx, value))

fun View.gone() {
    visibility = GONE
}

fun View.visible() {
    visibility = VISIBLE
}

fun View.invisible() {
    visibility = INVISIBLE
}

fun View.rotate(degree: Float, duration: Long = 200) {
    ViewCompat.animate(this)
        .rotation(degree)
        .withLayer()
        .setDuration(duration)
//                .setInterpolator(OvershootInterpolator(10.0F))
        .start()
}

fun ViewGroup.setAnimation(visibility: Int, animation: Int) {
    val anim = AnimationUtils.loadAnimation(ctx, animation)
    val animController = LayoutAnimationController(anim)

    this.visibility = visibility
    layoutAnimation = animController
    startAnimation(anim)
}

fun View.calRatio(width: Int, heightRatio: Float) {
    layoutParams.width = width
    layoutParams.height = (width * heightRatio).toInt()
}

fun View.setOnSafeClickListener(safeTime: Long = 300, clickListener: (View?) -> Unit) {
    setOnClickListener(SafeOnClickListener.newInstance(safeTime) {
        clickListener(it)
    })
}

fun View.showPopupMenu(menuRes: Int, listener: (MenuItem) -> Unit) {
    val themeWrapper = ContextThemeWrapper(ctx, R.style.PopupMenu)
    PopupMenu(themeWrapper, this).apply {
        menuInflater.inflate(menuRes, menu)

        setOnMenuItemClickListener { item ->
            listener(item)
            return@setOnMenuItemClickListener true
        }

        show()
    }
}