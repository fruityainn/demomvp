package com.hide.videophoto.data.mapper

import com.hide.videophoto.data.entity.AuthEntity
import com.hide.videophoto.data.model.AuthModel

fun AuthEntity.convertToModel(): AuthModel {
    val entity = this
    return AuthModel().apply {
        rowId = entity.rowId
        password = entity.password
        passwordFake = entity.passwordFake
        email = entity.email
        securityQuestion = entity.securityQuestion
        securityAnswer = entity.securityAnswer
    }
}

fun AuthModel.convertToEntity(): AuthEntity {
    val model = this
    return AuthEntity().apply {
        rowId = model.rowId
        password = model.password
        passwordFake = model.passwordFake
        email = model.email
        securityQuestion = model.securityQuestion
        securityAnswer = model.securityAnswer?.lowercase()
    }
}

fun AuthModel.mergeWith(model: AuthModel) {
    rowId = model.rowId
    password = model.password
    passwordFake = model.passwordFake
    email = model.email
    securityQuestion = model.securityQuestion
    securityAnswer = model.securityAnswer
}