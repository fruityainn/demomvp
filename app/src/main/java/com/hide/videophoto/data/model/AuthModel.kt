package com.hide.videophoto.data.model

class AuthModel : BaseModel() {
    var rowId: Long? = null

    var password: String? = null

    var passwordFake: String? = null

    var email: String? = null

    var securityQuestion: String? = null

    var securityAnswer: String? = null

    fun hasLoggedInAlready(): Boolean {
        return rowId != null && password?.isNotEmpty() ?: false
    }

    fun logOut() {
        rowId = null
        password = null
    }
}