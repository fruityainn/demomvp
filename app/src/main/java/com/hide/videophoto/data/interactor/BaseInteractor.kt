package com.hide.videophoto.data.interactor

import android.content.Context
import com.hide.videophoto.data.AppDatabase

abstract class BaseInteractor(ctx: Context) {
    protected val roomDB by lazy { AppDatabase.getInstance(ctx) }
}