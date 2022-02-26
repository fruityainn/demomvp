package com.hide.videophoto.data.model

class ActivityModel : BaseModel() {
    var rowId: Long? = null

    var installedTime: Int = 0

    var lastLogin: Int = 0

    var lastAddedContent: String? = null

    var failedResetTimes: Int = 0

    var lastTimeAdClicked: Int = 0 // Second

    var adClickedNumber: Int = 0
}