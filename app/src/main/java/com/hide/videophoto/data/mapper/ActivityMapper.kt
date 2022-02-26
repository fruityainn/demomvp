package com.hide.videophoto.data.mapper

import com.hide.videophoto.data.entity.ActivityEntity
import com.hide.videophoto.data.model.ActivityModel

fun ActivityEntity.convertToModel(): ActivityModel {
    val entity = this
    return ActivityModel().apply {
        rowId = entity.rowId
        installedTime = entity.installedTime ?: 0
        lastLogin = entity.lastLogin ?: 0
        lastAddedContent = entity.lastAddedContent
        failedResetTimes = entity.failedResetTimes
        lastTimeAdClicked = entity.lastTimeAdClicked
        adClickedNumber = entity.adClickedNumber
    }
}

fun ActivityModel.convertToEntity(): ActivityEntity {
    val model = this
    return ActivityEntity().apply {
        rowId = model.rowId
        installedTime = model.installedTime
        lastLogin = model.lastLogin
        lastAddedContent = model.lastAddedContent
        failedResetTimes = model.failedResetTimes
        lastTimeAdClicked = model.lastTimeAdClicked
        adClickedNumber = model.adClickedNumber
    }
}

fun ActivityModel.mergeWith(model: ActivityModel) {
    rowId = model.rowId
    installedTime = model.installedTime
    lastLogin = model.lastLogin
    lastAddedContent = model.lastAddedContent
    failedResetTimes = model.failedResetTimes
    lastTimeAdClicked = model.lastTimeAdClicked
    adClickedNumber = model.adClickedNumber
}