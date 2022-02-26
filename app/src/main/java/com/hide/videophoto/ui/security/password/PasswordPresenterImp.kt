package com.hide.videophoto.ui.security.password

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.hide.videophoto.common.ext.*
import com.hide.videophoto.data.interactor.DBInteractor
import com.hide.videophoto.data.interactor.FirebaseRemoteInteractor
import com.hide.videophoto.data.interactor.IapInteractor
import com.hide.videophoto.ui.base.BasePresenterImp
import io.reactivex.schedulers.Schedulers

class PasswordPresenterImp(private val ctx: Context) : BasePresenterImp<PasswordView>(ctx) {

    private val iapInteractor by lazy { IapInteractor(ctx) }
    private val firebaseRemoteInteractor by lazy { FirebaseRemoteInteractor(ctx) }
    private val dbInteractor by lazy { DBInteractor(ctx) }

    fun login(password: String) {
        view?.also { v ->
            if (v.passwordIsValid()) {
                dbInteractor.login(password)
                    .applyIOWithAndroidMainThread()
                    .subscribe(
                        {
                            v.onLoggedInSuccess(it)
                        },
                        {
                            v.onLoggingInError(password)
                            it.printStackTrace()
                        }
                    )
                    .addToCompositeDisposable(compositeDisposable)
            }
        }
    }

    fun changePassword(oldPassword: String?, newPassword: String) {
        view?.also { v ->
            if (v.passwordIsValid()) {
                if (v.passwordsAreMatch()) {
                    ctx.authModel.password = newPassword
                    dbInteractor.updateUser(ctx.authModel)
                        .applyIOWithAndroidMainThread()
                        .subscribe(
                            {
                                if (it > 0) {
                                    // Must revert old password after being changed success to avoid error in case user has not logged in
                                    ctx.authModel.password = oldPassword

                                    // Notify to view
                                    v.onPasswordChangedSuccess(newPassword)
                                } else {
                                    v.onQueryDbError()
                                }
                            },
                            {
                                ctx.authModel.password = oldPassword
                                v.onQueryDbError()
                                it.printStackTrace()
                            }
                        )
                        .addToCompositeDisposable(compositeDisposable)
                } else {
                    v.onPasswordNotMatchError()
                }
            } else {
                v.onPasswordInvalidError()
            }
        }
    }

    fun createUser(password: String) {
        view?.also { v ->
            if (v.passwordIsValid()) {
                if (v.passwordsAreMatch()) {
                    ctx.authModel.password = password
                    dbInteractor.createUser(ctx.authModel)
                        .applyIOWithAndroidMainThread()
                        .subscribe(
                            { rowId ->
                                v.onCreatedUserSuccess(rowId)
                            },
                            {
                                ctx.authModel.password = null
                                v.onQueryDbError()
                                it.printStackTrace()
                            }
                        )
                        .addToCompositeDisposable(compositeDisposable)
                } else {
                    v.onPasswordNotMatchError()
                }
            } else {
                v.onPasswordInvalidError()
            }
        }
    }

    fun countUser() {
        view?.also { v ->
            dbInteractor.countUser()
                .applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        v.onCountingUserSuccess(it)
                    },
                    {
                        v.onQueryDbError()
                        it.printStackTrace()
                    }
                )
                .addToCompositeDisposable(compositeDisposable)
        }
    }

    fun getBasicUserInfo() {
        view?.also { v ->
            dbInteractor.getBasicUserInfo()
                .applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        v.onGettingBasicUserInfoSuccess(it)
                    },
                    {
                        v.onQueryDbError()
                        it.printStackTrace()
                    }
                )
                .addToCompositeDisposable(compositeDisposable)
        }
    }

    fun getActivity() {
        view?.also { v ->
            dbInteractor.getActivity()
                .applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        v.onGettingActivitySuccess(it)
                    },
                    {
                        it.printStackTrace()
                    }
                )
                .addToCompositeDisposable(compositeDisposable)
        }
    }

    fun createActivity() {
        view?.also { v ->
            ctx.activityModel.apply {
                installedTime = ctx.currentTimeInSecond
                lastLogin = ctx.currentTimeInSecond
            }
            dbInteractor.createActivity(ctx.activityModel)
                .applyIOWithAndroidMainThread()
                .subscribe(
                    { rowId ->
                        ctx.activityModel.rowId = rowId
                    },
                    {
                        it.printStackTrace()
                    }
                )
                .addToCompositeDisposable(compositeDisposable)
        }
    }

    fun saveLoginTime() {
        view?.also {
            ctx.activityModel.lastLogin = ctx.currentTimeInSecond
            dbInteractor.updateActivity(ctx.activityModel)
                .subscribeOn(Schedulers.io())
                .subscribe()
                .addToCompositeDisposable(compositeDisposable)
        }
    }

    fun checkVipStatus() {
        view?.run {
            with(ctx.appSettingsModel) {
                if (!didRemoveAds && !didCheckVipStatus) {
                    iapInteractor.checkVipStatus()
                }
            }
        }
    }

    fun fetchRemoteConfiguration(activity: AppCompatActivity, onCompleteListener: () -> Unit) {
        view?.run {
            firebaseRemoteInteractor.fetchConfig(activity, onCompleteListener)
        }
    }
}