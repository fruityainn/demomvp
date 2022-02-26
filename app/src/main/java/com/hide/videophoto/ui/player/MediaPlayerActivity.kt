package com.hide.videophoto.ui.player

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.hide.videophoto.R
import com.hide.videophoto.common.Constants
import com.hide.videophoto.common.ext.*
import com.hide.videophoto.common.util.PermissionUtil
import com.hide.videophoto.data.mapper.convertToFileModels
import com.hide.videophoto.data.mapper.toJson
import com.hide.videophoto.data.model.FileModel
import com.hide.videophoto.ui.base.BaseActivity
import com.hide.videophoto.widget.ProgressWheel

class MediaPlayerActivity : BaseActivity<MediaPlayerView, MediaPlayerPresenterImp>(),
    MediaPlayerView, PlayerControlView.VisibilityListener {

    private val toolbarPlayer by lazy { findViewById<Toolbar>(R.id.toolbar_player) }
    private val playerView by lazy { findViewById<PlayerView>(R.id.player_view) }
    private val progressPlayer by lazy { findViewById<ProgressWheel>(R.id.pb_player) }
    private val imgFullScreen by lazy { findViewById<ImageView>(R.id.img_full_screen) }
    private val imgMute by lazy { findViewById<ImageView>(R.id.img_mute) }
    private val imgNext by lazy { findViewById<ImageView>(R.id.img_next) }
    private val imgPrevious by lazy { findViewById<ImageView>(R.id.img_prev) }

    private val fileModels by lazy { arrayListOf<FileModel>() }
    private var currentWindow = 0
    private var playbackPosition = 0L
    private var playWhenReady = true
    private var player: ExoPlayer? = null
    private val playbackStateListener by lazy { PlaybackStateListener(this) }
    private var isMuted = false

    companion object {
        fun start(ctx: Context, fileModels: List<FileModel>, startPosition: Int) {
            Intent(ctx, MediaPlayerActivity::class.java).apply {
                putExtra(Constants.Key.FILE_MODELS, fileModels.toJson())
                putExtra(Constants.Key.POSITION, startPosition)
            }.run {
                ctx.startActivity(this)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        logE("onStart")
        if (PermissionUtil.isApi24orHigher()) {
            initPlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        logE("onResume")
        hideSystemBar()
        if (PermissionUtil.isApi23()) {
            initPlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        logE("onPause")
        if (PermissionUtil.isApi23()) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        logE("onStop")
        if (PermissionUtil.isApi24orHigher()) {
            releasePlayer()
        }
    }

    override fun onBackPressed() {
        if (isSensorLandscapeOrientation()) {
            imgFullScreen.setImageResource(R.drawable.ic_full_screen_white)
            setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT)
        } else {
            releasePlayer()
            showInterstitialAd {
                super.onBackPressed()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Get values from extras
        getValuesFromExtras(intent)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBar()
        }
    }

    override fun initView(): MediaPlayerView {
        return this
    }

    override fun initPresenter(): MediaPlayerPresenterImp {
        return MediaPlayerPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_media_player
    }

    override fun initWidgets() {
        // Get data from intent first
        getValuesFromExtras(intent)

        // Init toolbar
        applyToolbar(toolbarPlayer)
        enableHomeAsUp {
            onBackPressed()
        }

        // Listeners
        imgFullScreen.setOnSafeClickListener {
            rotateScreen(
                portraitMode = {
                    imgFullScreen.setImageResource(R.drawable.ic_full_screen_white)
                },
                landscapeMode = {
                    imgFullScreen.setImageResource(R.drawable.ic_exit_full_screen_white)
                }
            )
        }

        imgMute.setOnSafeClickListener {
            player?.run {
                if (isMuted) {
                    volume = deviceVolume.toFloat()
                    imgMute.setImageResource(R.drawable.ic_volume)
                } else {
                    volume = 0f
                    imgMute.setImageResource(R.drawable.ic_mute)
                }

                isMuted = !isMuted
            }
        }

        imgNext.setOnSafeClickListener {
            player?.run {
                if (hasNextMediaItem()) {
                    seekToNextMediaItem()
                    updatePlayControlUI()
                }
            }
        }

        imgPrevious.setOnSafeClickListener {
            player?.run {
                if (hasPreviousMediaItem()) {
                    seekToPreviousMediaItem()
                    updatePlayControlUI()
                }
            }
        }
    }

    override fun onVisibilityChange(visibility: Int) {
    }

    private fun getValuesFromExtras(intent: Intent?) {
        intent?.extras?.run {
            if (containsKey(Constants.Key.FILE_MODELS)) {
                val json = getString(Constants.Key.FILE_MODELS)
                json?.run {
                    fileModels.addAll(convertToFileModels())
                }
            }
            if (containsKey(Constants.Key.POSITION)) {
                currentWindow = getInt(Constants.Key.POSITION)
            }
        }
    }

    private fun initPlayer() {
        if (player == null) {
            // Init exo player
            player = ExoPlayer.Builder(ctx).build().apply {
                playerView.player = this

                fileModels.forEachIndexed { _, video ->
                    MediaItem.fromUri(Uri.parse(video.getEncryptedPath())).also { item ->
                        addMediaItem(item)
                    }
                }

                this.playWhenReady = this@MediaPlayerActivity.playWhenReady
                seekTo(currentWindow, playbackPosition)
                addListener(playbackStateListener)
                prepare()
            }

            playerView.setControllerVisibilityListener(this)

            updatePlayControlUI()
        }
    }

    private fun releasePlayer() {
        logE("releasePlayer")
        player?.run {
            this@MediaPlayerActivity.playWhenReady = playWhenReady
            this@MediaPlayerActivity.playbackPosition = currentPosition
            this@MediaPlayerActivity.currentWindow = currentMediaItemIndex
            removeListener(playbackStateListener)
            release()
            player = null
            logE("Released player")
        }
    }

    private fun showCenterProgress() {
        progressPlayer.visible()
    }

    private fun hideCenterProgress() {
        progressPlayer.gone()
    }

    private fun updatePlayControlUI() {
        player?.run {
            if (hasNextMediaItem()) {
                imgNext.setImageResource(R.drawable.ic_next_active)
            } else {
                imgNext.setImageResource(R.drawable.ic_next_inactive)
            }

            if (hasPreviousMediaItem()) {
                imgPrevious.setImageResource(R.drawable.ic_prev_active)
            } else {
                imgPrevious.setImageResource(R.drawable.ic_prev_inactive)
            }
        }
    }

    private fun showInterstitialAd(onAdDismissed: () -> Unit) {
        adsManager.showInterstitialAd(self) {
            onAdDismissed()
        }
    }

    class PlaybackStateListener(private val mediaPlayerActivity: MediaPlayerActivity) :
        Player.Listener {

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)

            when (playbackState) {
                ExoPlayer.STATE_IDLE -> {
                    mediaPlayerActivity.hideCenterProgress()
                }
                ExoPlayer.STATE_BUFFERING -> {
                    mediaPlayerActivity.showCenterProgress()
                }
                ExoPlayer.STATE_READY -> {
                    mediaPlayerActivity.run {
                        hideCenterProgress()

                        showTitle(fileModels[player?.currentMediaItemIndex ?: 0].name)
                        updatePlayControlUI()
                    }
                }
                ExoPlayer.STATE_ENDED -> {
                    mediaPlayerActivity.hideCenterProgress()
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            mediaPlayerActivity.run {
                releasePlayer()
                hideCenterProgress()
            }
        }
    }
}
