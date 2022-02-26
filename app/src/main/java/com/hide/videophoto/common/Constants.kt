package com.hide.videophoto.common

import android.os.Environment
import com.hide.videophoto.common.util.CommonUtil
import java.io.File

object Constants {

    val STORAGE_ROOT_FOLDER = Environment.getExternalStorageDirectory().absolutePath
    val UNHIDDEN_FOLDER = "$STORAGE_ROOT_FOLDER${File.separator}HideS${File.separator}Unhide"
    val NOTE_FOLDER = "$STORAGE_ROOT_FOLDER${File.separator}HideS${File.separator}Note"
    val CAPTURED_FOLDER = "$STORAGE_ROOT_FOLDER${File.separator}HideS${File.separator}Capture"
    val APP_ROOT_FOLDER = "$STORAGE_ROOT_FOLDER${File.separator}.hideS"
    val DB_PATH = "$APP_ROOT_FOLDER${File.separator}db"

    val LINK_APP = "https://play.google.com/store/apps/details?id=${CommonUtil.getAppID()}"
    const val LINK_POLICY = "https://google.com"
    const val SUPPORT_EMAIL = "mklab.apps@gmail.com"
    const val ADDRESS = "230 Tran Cung, Co Nhue, Bac Tu Liem, Ha Noi, Viet Nam"
    const val MAX_ACTIVITY_VERIFICATION_TIMES = 3

    val videoExtensions = listOf(
        ".mp4",
        ".3gp",
        ".3g2",
        ".avi",
        ".mkv",
        ".ts",
        ".webm",
        ".m4v",
        ".vob",
        ".rrc",
        ".mov",
        ".yuv",
        ".asf",
        ".amv",
        ".mng",
        ".mpe",
        ".mpeg",
        ".mpv",
        ".mp2",
        ".mpg",
        ".mxf",
        ".qt",
        ".wmv",
        ".gifv",
        ".rm",
        ".roq",
        ".svi",
        ".nsv",
        ".flv",
        ".f4v",
        ".f4p",
        ".f4a",
        ".f4b"
    )
    val audioExtensions = listOf(
        ".mp3",
        ".aac",
        ".m4a",
        ".flac",
        ".ogg",
        ".wav",
        ".ota",
        ".mid",
        ".xmf",
        ".mxmf",
        ".amr"
    )
    val imageExtensions = listOf(".jpg", ".png", ".bmp", ".gif", ".webp")
    val mediaSupportedFormats = listOf(
        ".mp4",
        ".m4a",
        ".fmp4",
        ".webm",
        ".mkv",
        ".mk3d",
        ".mk3d",
        ".mka",
        ".mks",
        ".mp3",
        ".ogg",
        ".wav",
        ".ts",
        ".tsv",
        ".tsa",
        ".m2t",
        ".mpg",
        ".mpeg",
        ".m2p",
        ".ps",
        ".flv",
        ".f4v",
        ".f4p",
        ".f4a",
        ".f4b",
        ".flac",
        ".amr",
        ".aac"
    )

    object DataType {
        const val AUDIO = "audio"
        const val VIDEO = "video"
        const val IMAGE = "image"
        const val NOTE = "note"
        const val OTHER = "other"
    }

    object DB {
        // Table name - WARNING: Must not change table name
        const val FILE_ENTITY = "file_entity"
        const val AUTH_ENTITY = "auth_entity"
        const val ACTIVITY_ENTITY = "activity_entity"

        // Column name - WARNING: Must not change column name
        const val ROWID = "rowid"
        const val NAME = "name"
        const val EXTENSION = "extension"
        const val MIME_TYPE = "mime_type"
        const val ENCRYPTED_NAME = "encrypted_name"
        const val PARENT_ID = "parent_id"
        const val ORIGINAL_FOLDER = "original_folder"
        const val CACHE_PATH = "cache_path"
        const val SIZE = "size"
        const val DURATION = "duration"
        const val TITLE = "title"
        const val ARTIST = "artist"
        const val ORIENTATION = "orientation"
        const val RESOLUTION = "resolution"
        const val ADDED_DATE = "added_date"
        const val MODIFIED_DATE = "modified_date"
        const val ENCRYPTED_TYPE = "encrypted_type"
        const val DIRECTORY = "directory"
        const val FAVORITE = "favorite"
        const val NOTE = "note"
        const val CONTENT = "content"
        const val PASSWORD = "password"
        const val PASSWORD_FAKE = "password_fake"
        const val EMAIL = "email"
        const val SECURITY_QUESTION = "security_question"
        const val SECURITY_ANSWER = "security_answer"
        const val INSTALLED_TIME = "installed_time"
        const val LAST_LOGGED_IN = "last_logged_in"
        const val LAST_ADDED_CONTENT = "last_added_content"
        const val FAILED_RESET_TIMES = "failed_reset_times"
        const val LAST_TIME_AD_CLICKED = "last_time_ad_clicked"
        const val AD_CLICKED_NUMBER = "ad_clicked_number"
    }

    object EncryptedType {
        const val NONE = "none"
        const val AES256_GCM = "aes256_gcm"
    }

    object Key {
        const val FILE_MODEL = "file_model"
        const val FILE_MODELS = "file_models"
        const val POSITION = "position"
        const val OPENED_BY_USER = "opened_by_user"
        const val IS_FROM_BACKGROUND = "is_from_background"
    }

    object Layout {
        const val LIST = 1
        const val GRID_NOTE = 2
        const val GRID = 2
    }

    object SortType {
        const val DATE = 1
        const val NAME = 2
        const val SIZE = 3
        const val DIRECTION_ASC = 1
        const val DIRECTION_DESC = 2
    }

    object Event {
        const val ADD = "event_add"
        const val EDIT = "event_edit"
        const val MOVE = "event_move"
        const val RENAME = "event_rename"
        const val UNHIDE = "event_unhide"
        const val DELETE = "event_delete"
        const val REMOVE_AD = "remove_ad"
        const val CHANGE_LANGUAGE = "change_language"
    }
}