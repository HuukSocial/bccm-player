package media.bcc.bccm_player

import android.app.MediaRouteButton
import android.content.Intent
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.mediarouter.app.MediaRouteChooserDialogFragment
import androidx.mediarouter.app.MediaRouteDialogFactory
import com.google.android.gms.cast.framework.CastButtonFactory
import io.flutter.embedding.android.FlutterFragmentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import media.bcc.bccm_player.pigeon.PlaybackPlatformApi
import media.bcc.bccm_player.players.chromecast.CastExpandedControlsActivity
import media.bcc.bccm_player.players.chromecast.CastPlayerController
import media.bcc.bccm_player.utils.toMedia3Type


class PlaybackApiImpl(private val plugin: BccmPlayerPlugin) :
    PlaybackPlatformApi.PlaybackPlatformPigeon {
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())

    override fun attach(result: PlaybackPlatformApi.Result<Void>) {
        Log.d("bccm", "attaching plugin")
        // Extremely important to call result.success or result.fail
        plugin.attach(onComplete = {
            result.success(null)
        })
    }

    override fun setNpawConfig(config: PlaybackPlatformApi.NpawConfig?) {
        Log.d("bccm", "PlaybackPigeon: Setting npawConfig")
        mainScope.launch {
            BccmPlayerPluginSingleton.npawConfigState.update { config }
        }
    }

    override fun setAppConfig(config: PlaybackPlatformApi.AppConfig?) {
        Log.d("bccm", "PlaybackPigeon: Setting appConfig")
        mainScope.launch {
            BccmPlayerPluginSingleton.appConfigState.update { config }
        }
    }

    override fun getTracks(
        playerId: String?,
        result: PlaybackPlatformApi.Result<PlaybackPlatformApi.PlayerTracksSnapshot>
    ) {
        val playbackService = plugin.getPlaybackService()
        if (playbackService == null) {
            result.error(Error())
            return
        }
        val playerController =
            if (playerId != null) playbackService.getController(playerId) else playbackService.getPrimaryController()
        if (playerController == null) {
            result.error(Error("Player with id $playerId does not exist."))
            return
        }
        result.success(playerController.getTracksSnapshot())
    }

    override fun getPlayerState(
        playerId: String?,
        result: PlaybackPlatformApi.Result<PlaybackPlatformApi.PlayerStateSnapshot>
    ) {
        val playbackService = plugin.getPlaybackService()
        if (playbackService == null) {
            result.error(Error())
            return
        }
        val playerController =
            if (playerId != null) playbackService.getController(playerId) else playbackService.getPrimaryController()
        if (playerController == null) {
            result.error(Error("Player with id $playerId does not exist."))
            return
        }
        result.success(playerController.getPlayerStateSnapshot())
    }

    override fun setSelectedTrack(
        playerId: String,
        type: PlaybackPlatformApi.TrackType,
        trackId: String?,
        result: PlaybackPlatformApi.Result<Void>
    ) {
        val playbackService = plugin.getPlaybackService()
        if (playbackService == null) {
            result.error(Error())
            return
        }
        val playerController = playbackService.getController(playerId)
        if (playerController == null) {
            result.error(Error("Player with id $playerId does not exist."))
            return
        }
        playerController.setSelectedTrack(type.toMedia3Type(), trackId)
        result.success(null)
    }

    override fun setPlaybackSpeed(
        playerId: String,
        speed: Double,
        result: PlaybackPlatformApi.Result<Void>
    ) {
        val playbackService = plugin.getPlaybackService()
        if (playbackService == null) {
            result.error(Error())
            return
        }
        val playerController = playbackService.getController(playerId)
        if (playerController == null) {
            result.error(Error("Player with id $playerId does not exist."))
            return
        }
        playerController.setPlaybackSpeed(speed.toFloat())
        result.success(null)
    }

    override fun newPlayer(url: String?, result: PlaybackPlatformApi.Result<String>) {
        Log.d("bccm", "PlaybackPigeon: newPlayer()")
        val playbackService = plugin.getPlaybackService()
        if (playbackService == null) {
            result.error(Error())
            return
        }
        val playerController = playbackService.newPlayer()
        if (url != null) {
            playerController.replaceCurrentMediaItem(
                PlaybackPlatformApi.MediaItem.Builder().setUrl(url).build(), false
            )
        }
        result.success(playerController.id)
    }

    override fun disposePlayer(playerId: String, result: PlaybackPlatformApi.Result<Boolean>) {
        val playbackService = plugin.getPlaybackService()
        if (playbackService == null) {
            result.error(Error("Playback service doesnt exist"))
            return
        }
        val didDispose = playbackService.disposePlayer(playerId)
        result.success(didDispose);
    }

    override fun replaceCurrentMediaItem(
        playerId: String,
        mediaItem: PlaybackPlatformApi.MediaItem,
        playbackPositionFromPrimary: Boolean?,
        autoplay: Boolean?,
        result: PlaybackPlatformApi.Result<Void>
    ) {
        val playbackService = plugin.getPlaybackService()
        if (playbackService == null) {
            result.error(Error())
            return
        }
        if (playbackPositionFromPrimary == true) {
            mediaItem.playbackStartPositionMs =
                playbackService.getPrimaryController()?.player?.currentPosition?.toDouble()
        }

        val playerController = playbackService.getController(playerId)
        if (playerController == null) {
            result.error(Error("Player with id $playerId does not exist."))
            return
        }

        playerController.replaceCurrentMediaItem(mediaItem, autoplay)
        result.success(null)
    }

    override fun setPlayerViewVisibility(viewId: Long, visible: Boolean) {
        mainScope.launch {
            BccmPlayerPluginSingleton.eventBus.emit(SetPlayerViewVisibilityEvent(viewId, visible))
        }
    }

    override fun queueMediaItem(
        playerId: String,
        mediaItem: PlaybackPlatformApi.MediaItem,
        result: PlaybackPlatformApi.Result<Void>
    ) {
        val playbackService = plugin.getPlaybackService()
        if (playbackService == null) {
            result.error(Error())
            return
        }
        val playerController = playbackService.getController(playerId)
            ?: throw Error("Player with id $playerId does not exist.")
        playerController.queueMediaItem(mediaItem)
        result.success(null)
    }

    override fun setPrimary(id: String, result: PlaybackPlatformApi.Result<Void>) {
        val playbackService = plugin.getPlaybackService()
        if (playbackService == null) {
            result.error(Error())
            return
        }

        playbackService.setPrimary(id)
        result.success(null)
    }

    override fun play(playerId: String) {
        val playbackService = plugin.getPlaybackService() ?: return
        val playerController = playbackService.getController(playerId)
            ?: throw Error("Player with id $playerId does not exist.")

        playerController.play()
    }

    override fun seekTo(
        playerId: String,
        positionMs: Double,
        result: PlaybackPlatformApi.Result<Void>
    ) {
        val playbackService = plugin.getPlaybackService() ?: return
        val playerController = playbackService.getController(playerId)
            ?: throw Error("Player with id $playerId does not exist.")
        try {
            playerController.player.seekTo(positionMs.toLong());
            result.success(null);
        } catch (e: Exception) {
            result.error(e)
        }
    }

    override fun pause(playerId: String) {
        val playbackService = plugin.getPlaybackService() ?: return
        val playerController = playbackService.getController(playerId)
            ?: throw Error("Player with id $playerId does not exist.")

        playerController.pause()
    }

    override fun setVolume(
        playerId: String,
        volume: Double,
        result: PlaybackPlatformApi.Result<Void>
    ) {
        val playbackService = plugin.getPlaybackService() ?: return
        val playerController = playbackService.getController(playerId)
            ?: return result.error(Error("Player with id $playerId does not exist."))

        playerController.setVolume(volume)
        result.success(null)
    }

    override fun stop(playerId: String, reset: Boolean) {
        val playbackService = plugin.getPlaybackService() ?: return
        val playerController = playbackService.getController(playerId)
            ?: throw Error("Player with id $playerId does not exist.")

        playerController.stop(reset)
    }

    override fun exitFullscreen(playerId: String) {
        val playbackService = plugin.getPlaybackService() ?: return
        val playerController = playbackService.getController(playerId)
            ?: throw Error("Player with id $playerId does not exist.")

        playerController.currentPlayerViewController?.exitFullscreen()
    }

    override fun enterFullscreen(playerId: String) {
        val playbackService = plugin.getPlaybackService() ?: return
        val playerController = playbackService.getController(playerId)
            ?: throw Error("Player with id $playerId does not exist.")

        playerController.currentPlayerViewController?.enterFullscreen()
    }

    override fun setMixWithOthers(
        playerId: String,
        mixWithOthers: Boolean,
        result: PlaybackPlatformApi.Result<Void>
    ) {
        val playbackService = plugin.getPlaybackService() ?: return
        val playerController = playbackService.getController(playerId)
            ?: throw Error("Player with id $playerId does not exist.")
        playerController.setMixWithOthers(mixWithOthers)
    }

    override fun getChromecastState(result: PlaybackPlatformApi.Result<PlaybackPlatformApi.ChromecastState>) {
        val playbackService = plugin.getPlaybackService()
        if (playbackService == null) {
            result.error(Error())
            return
        }
        val cc = playbackService.getController("chromecast")
        if (cc == null || cc !is CastPlayerController) {
            return
        }
        result.success(cc.getState())
    }

    override fun openExpandedCastController() {
        val intent = Intent(
            BccmPlayerPluginSingleton.activityState.value,
            CastExpandedControlsActivity::class.java
        )
        BccmPlayerPluginSingleton.activityState.value?.startActivity(intent)
    }

    override fun openCastDialog() {
        val activity =
            (BccmPlayerPluginSingleton.activityState.value as? FlutterFragmentActivity) ?: return
        val fm =
            activity.supportFragmentManager
                ?: return
        val btn = androidx.mediarouter.app.MediaRouteButton(activity)
        CastButtonFactory.setUpMediaRouteButton(activity, btn)
        btn.onAttachedToWindow()
        btn.showDialog()
    }

    override fun fetchMediaInfo(
        url: String,
        mimeType: String?,
        result: PlaybackPlatformApi.Result<PlaybackPlatformApi.MediaInfo>
    ) {
        val context = BccmPlayerPluginSingleton.activityState.value;
        if (context != null) {
            mainScope.launch {
                try {
                    val mediaInfo = MediaInfoFetcher.fetchMediaInfo(context, url, mimeType)
                    result.success(mediaInfo)
                } catch (e: Exception) {
                    result.error(e)
                }
            }
        } else {
            result.error(Error("Not attached to activity"))
        }
    }
}