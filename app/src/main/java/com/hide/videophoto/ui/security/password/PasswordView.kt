package com.hide.videophoto.ui.security.password

import com.hide.videophoto.data.model.ActivityModel
import com.hide.videophoto.data.model.AuthModel
import com.hide.videophoto.ui.base.BaseView

interface PasswordView : BaseView {

    fun onLoggedInSuccess(model: AuthModel)

    fun onLoggingInError(password: String)

    fun onPasswordChangedSuccess(newPassword: String)

    fun onCreatedUserSuccess(rowId: Long)

    fun onCountingUserSuccess(count: Int)

    fun onGettingBasicUserInfoSuccess(model: AuthModel)

    fun onGettingActivitySuccess(model: ActivityModel)

    fun passwordIsValid(): Boolean

    fun onPasswordInvalidError()

    fun passwordsAreMatch(): Boolean

    fun onPasswordNotMatchError()

    fun onQueryDbError()
}