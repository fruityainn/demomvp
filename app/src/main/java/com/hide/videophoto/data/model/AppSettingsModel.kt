package com.hide.videophoto.data.model

import com.hide.videophoto.common.Constants

class AppSettingsModel : BaseModel() {
    var didRemoveAds = false
    var didCheckVipStatus = false
    var hasUserAlready = false
    var layoutTypeFolder = Constants.Layout.LIST
    var layoutTypeFile: Int? = null
    var layoutTypeNote: Int? = null
    var sortType: Int = Constants.SortType.DATE
    var sortDirection: Int = Constants.SortType.DIRECTION_DESC
    var shouldUnhideToOriginalPath = false
    var appLanguage: String? = null
    var defaultLanguage: String? = null
    var dontShowRateDialogAgain = false
    var shouldCheckShowingRateDialog = false
    var countAddingFile: Int = 0
    var countAppOpened: Int = 0
    var saleOffTime: Int = 0
    var lastTimeInForeground: Int = 0
    var hasNavigationBar: Boolean? = null
}