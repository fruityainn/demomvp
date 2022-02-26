package com.hide.videophoto.common.ext

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

fun Disposable.addToCompositeDisposable(compositeDisposable: CompositeDisposable) {
    compositeDisposable.add(this)
}

fun <T> Single<T>.applyIOWithAndroidMainThread(): Single<T> {
    return subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}

fun <T> Single<T>.applyComputationWithAndroidMainThread(): Single<T> {
    return subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
}

fun <T> Observable<T>.applyIOWithAndroidMainThread(): Observable<T> {
    return subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}

fun <T> Observable<T>.applyComputationWithAndroidMainThread(): Observable<T> {
    return subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
}

fun Completable.applyIOWithAndroidMainThread(): Completable {
    return subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}

fun Completable.applyComputationWithAndroidMainThread(): Completable {
    return subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
}

fun <T> Flowable<T>.applyIOWithAndroidMainThread(): Flowable<T> {
    return subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}