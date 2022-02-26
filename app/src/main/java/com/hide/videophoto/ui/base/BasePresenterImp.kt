package com.hide.videophoto.ui.base

import android.content.Context
import com.hide.videophoto.common.ext.addToCompositeDisposable
import com.hide.videophoto.common.ext.networkIsConnected
import com.hide.videophoto.widget.MyProgressDialog
import com.hide.videophoto.widget.ProgressDialogPercentage
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

open class BasePresenterImp<T : BaseView>(private val ctx: Context) : BasePresenter<T>() {

    private val progressDialog: MyProgressDialog by lazy { MyProgressDialog(ctx) }
    private val progressPercentageDialog: ProgressDialogPercentage by lazy {
        ProgressDialogPercentage(
            ctx
        )
    }

    protected var view: T? = null
    protected val compositeDisposable by lazy { CompositeDisposable() }

    override fun attachView(view: T) {
        this.view = view
    }

    override fun detachView() {
        view?.onDestroyAds()
        compositeDisposable.clear()
        view = null
    }

    protected fun showProgressDialog(cancelable: Boolean = false) {
        if (!progressDialog.isShowing) {
            progressDialog.setCancelable(cancelable)
            progressDialog.show()
        }
    }

    protected fun dismissProgressDialog() {
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    protected fun showProgressPercentageDialog(duration: Int) {
        if (!progressPercentageDialog.isShowing) {
            progressPercentageDialog.setCancelable(false)
            progressPercentageDialog.show(duration)
        }
    }

    protected fun dismissProgressPercentageDialog() {
        if (progressPercentageDialog.isShowing) {
            progressPercentageDialog.dismiss()
        }
    }

    protected fun networkIsAvailable(): Boolean {
        return ctx.networkIsConnected()
    }

    protected fun delayBeforeDoSomething(
        delayTime: Long,
        timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
        onSuccess: () -> Unit
    ) {
        view?.also {
            Single.timer(
                delayTime,
                timeUnit,
                AndroidSchedulers.mainThread()
            ).subscribe(object : SingleObserver<Any> {
                override fun onSubscribe(d: Disposable) {
                    d.addToCompositeDisposable(compositeDisposable)
                }

                override fun onSuccess(t: Any) {
                    onSuccess()
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                }
            })
        }
    }

    protected fun getProgressPercentageDuration(size: Long): Int {
        val mb = size / 1000f / 1000f
        return (mb * 15).toInt()
    }
}