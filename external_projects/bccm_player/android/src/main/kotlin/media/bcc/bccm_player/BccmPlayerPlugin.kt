package media.bcc.bccm_player

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.NonNull
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.android.gms.cast.framework.CastContext
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import media.bcc.bccm_player.PlaybackPlatformApi.PlaybackPlatformPigeon
import media.bcc.bccm_player.chromecast.BccmCastPlayerViewFactory
import media.bcc.bccm_player.chromecast.CastPlayerController
import media.bcc.bccm_player.chromecast.FLCastButtonFactory

class BccmPlayerPlugin : FlutterPlugin, ActivityAware, PluginRegistry.UserLeaveHintListener {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private var pluginBinding: FlutterPlugin.FlutterPluginBinding? = null
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private var playbackService: PlaybackService? = null
    private var playbackServiceFuture: ListenableFuture<PlaybackService>? = null
    private var mBound: Boolean = false
    private var activity: Activity? = null
    private var activityBinding: ActivityPluginBinding? = null
    private var castController: CastPlayerController? = null
    private val mainScope = CoroutineScope(Dispatchers.Main)
    var playbackPigeon: PlaybackPlatformApi.PlaybackListenerPigeon? = null
        private set

    private val playbackServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            playbackService = (binder as PlaybackService.LocalBinder).getService()
            if (castController != null) {
                playbackService?.addController(castController!!)
            }
            pluginBinding!!
                .platformViewRegistry
                .registerViewFactory("bccm-player", BccmInlinePlayerView.Factory(playbackService))
        }

        override fun onServiceDisconnected(arg0: ComponentName) {

        }
    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        pluginBinding = flutterPluginBinding
        playbackPigeon =
            PlaybackPlatformApi.PlaybackListenerPigeon(flutterPluginBinding.binaryMessenger)


        Intent(pluginBinding?.applicationContext, PlaybackService::class.java).also { intent ->
            mBound = pluginBinding?.applicationContext?.bindService(
                intent,
                playbackServiceConnection,
                Context.BIND_AUTO_CREATE
            )
                ?: false
        }

        var castContext: CastContext? = null
        try {
            castContext = CastContext.getSharedInstance(flutterPluginBinding.applicationContext)
            val ccPigeon =
                ChromecastControllerPigeon.ChromecastPigeon(flutterPluginBinding.binaryMessenger)
            castController = CastPlayerController(castContext, ccPigeon, this)
            castContext.sessionManager.addSessionManagerListener(castController!!)
            flutterPluginBinding
                .platformViewRegistry
                .registerViewFactory(
                    "bccm-cast-player",
                    BccmCastPlayerViewFactory(castController!!)
                )
        } catch (e: Exception) {
            //TODO: log exception

            flutterPluginBinding
                .platformViewRegistry
                .registerViewFactory("bccm-cast-player", EmptyView.Factory())
        }
        flutterPluginBinding
            .platformViewRegistry
            .registerViewFactory("bccm_player/cast_button", FLCastButtonFactory())
    }


    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        if (mBound) {
            Log.d("bccm", "detaching. mBound: $mBound")
            pluginBinding!!.applicationContext.unbindService(playbackServiceConnection)
            mBound = false
        }

        Intent(binding.applicationContext, PlaybackService::class.java).also { intent ->
            binding.applicationContext.stopService(intent)
        }
        pluginBinding = null
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        activityBinding = binding
        activityBinding?.addOnUserLeaveHintListener(this)

        PlaybackPlatformPigeon.setup(pluginBinding!!.binaryMessenger, PlaybackApiImpl(this))
        channel = MethodChannel(pluginBinding!!.binaryMessenger, "bccm_player")

        // Bind to LocalService

        val sessionToken = SessionToken(
            binding.activity,
            ComponentName(binding.activity, PlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(binding.activity, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                1
            }, MoreExecutors.directExecutor()
        )
        mainScope.launch {
            Log.d("bccm", "OnAttachedToActivity")
            BccmPlayerPluginSingleton.activityState.update { binding.activity }
            BccmPlayerPluginSingleton.eventBus.emit(AttachedToActivityEvent(binding.activity))
        }
        mainScope.launch {
            BccmPlayerPluginSingleton.eventBus.filter { event -> event is PictureInPictureModeChangedEvent }
                .collect { event ->
                    val event = event as PictureInPictureModeChangedEvent
                    var builder = PlaybackPlatformApi.PictureInPictureModeChangedEvent.Builder()
                    builder.setPlayerId(event.playerId)
                    builder.setIsInPipMode(event.isInPictureInPictureMode)
                    playbackPigeon?.onPictureInPictureModeChanged(builder.build()) {}
                }
        }
    }

    fun onStop() {
        mainScope.launch {
            BccmPlayerPluginSingleton.eventBus.emit(OnActivityStop())
        }
    }

    fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        mainScope.launch {
            BccmPlayerPluginSingleton.eventBus.emit(
                PictureInPictureModeChangedEvent2(
                    isInPictureInPictureMode
                )
            )
        }
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivity() {
        activityBinding?.removeOnUserLeaveHintListener(this)
        mainScope.launch {
            Log.d("bccm", "OnDetachedFromActivity")
            BccmPlayerPluginSingleton.eventBus.emit(DetachedFromActivityEvent())
        }
        channel.setMethodCallHandler(null)
        MediaController.releaseFuture(controllerFuture)
    }

    fun getPlaybackService(): PlaybackService? {
        return playbackService
    }

    override fun onUserLeaveHint() {
        val currentPlayerViewController =
            playbackService?.getPrimaryController()?.currentPlayerViewController
        if (currentPlayerViewController?.shouldPipAutomatically() == true && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            currentPlayerViewController.enterPictureInPicture()
        }
    }
}
