package com.hide.videophoto.ui.settings

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.hide.videophoto.common.ext.addToCompositeDisposable
import com.hide.videophoto.common.ext.applyIOWithAndroidMainThread
import com.hide.videophoto.common.util.RxBus
import com.hide.videophoto.data.interactor.IapInteractor
import com.hide.videophoto.ui.base.BasePresenterImp
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class SettingsPresenterImp(ctx: Context) : BasePresenterImp<SettingsView>(ctx) {

    private val iapInteractor by lazy { IapInteractor(ctx) }
    private var disposableSaleOff: Disposable? = null

    fun removeAds(activity: AppCompatActivity) {
        view?.also { v ->
            if (networkIsAvailable()) {
                iapInteractor.removeAds(activity)
            } else {
                v.onNetworkError()
            }
        }
    }

    fun listenAppSettingsChanged() {
        view?.also { v ->
            RxBus.listenAppSettingsChanged()
                .applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        v.onAppSettingsChanged(it)
                    },
                    {}
                )
                .addToCompositeDisposable(compositeDisposable)
        }
    }

    fun countdown(timeOut: Int, onNext: (String) -> Unit, onComplete: () -> Unit) {
        disposableSaleOff = Observable.interval(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                onNext(parseTime(timeOut - it))
            }
            .takeUntil {
                it == timeOut.toLong()
            }
            .doOnComplete {
                onComplete()
                disposeCountdown()
            }
            .subscribe()
    }

    fun disposeCountdown() {
        disposableSaleOff?.run {
            if (!isDisposed) {
                dispose()
            }
        }
    }

    private fun parseTime(number: Long): String {
        val second = number % 60
        val minute = if (number >= 60) {
            ((number / 60) % 60)
        } else {
            0
        }
        val hour = number / 60 / 60

        val strHour = if (hour < 10) {
            "0$hour"
        } else {
            "$hour"
        }

        val strMinute = if (minute < 10) {
            "0$minute"
        } else {
            "$minute"
        }

        val strSecond = if (second < 10) {
            "0$second"
        } else {
            "$second"
        }

        return "$strHour:$strMinute:$strSecond"
    }
}