import AVFoundation
import AVKit
import Foundation
import MediaPlayer
import YouboraAVPlayerAdapter
import YouboraLib

public class AVQueuePlayerController: NSObject, PlayerController, AVPlayerViewControllerDelegate {
    lazy var player: AVQueuePlayer = .init()
    public final let id: String
    final let playbackListener: PlaybackListenerPigeon
    final var observers = [NSKeyValueObservation]()
    var temporaryStatusObserver: NSKeyValueObservation? = nil
    var youboraPlugin: YBPlugin?
    var pipController: AVPlayerViewController? = nil
    var appConfig: AppConfig? = nil
    var refreshStateTimer: Timer? = nil
    var currentViewController: AVPlayerViewController? = nil
    var fullscreenViewController: AVPlayerViewController? = nil
    var isPrimary = false

    init(id: String? = nil, playbackListener: PlaybackListenerPigeon, npawConfig: NpawConfig?, appConfig: AppConfig?) {
        self.id = id ?? UUID().uuidString
        self.playbackListener = playbackListener
        super.init()
        updateAppConfig(appConfig: appConfig)
        addObservers()
        refreshStateTimer = Timer.scheduledTimer(withTimeInterval: 15, repeats: true) { _ in
            self.onManualPlayerStateUpdate()
        }
        if let npawConfig = npawConfig {
            initYoubora(npawConfig)
        }
        print("BTV DEBUG: end of init playerController")
    }
    
    deinit {
        refreshStateTimer?.invalidate()
    }
    
    public func onManualPlayerStateUpdate() {
        let event = PlayerStateUpdateEvent.make(withPlayerId: id, snapshot: getPlayerStateSnapshot())
        playbackListener.onPlayerStateUpdate(event, completion: { _ in })
    }
    
    public func getPlayerStateSnapshot() -> PlayerStateSnapshot {
        return PlayerStateSnapshot.make(
            withPlayerId: id,
            playbackState: isPlaying() ? PlaybackState.playing : PlaybackState.paused,
            isBuffering: NSNumber(booleanLiteral: isBuffering()),
            isFullscreen: (currentViewController != nil && currentViewController == fullscreenViewController) as NSNumber,
            playbackSpeed: getPlaybackSpeed() as NSNumber,
            videoSize: getVideoSize(),
            currentMediaItem: MediaItemMapper.mapPlayerItem(player.currentItem),
            playbackPositionMs: NSNumber(value: player.currentTime().seconds * 1000)
        )
    }
    
    public func getVideoSize() -> VideoSize? {
        guard let width = player.currentItem?.presentationSize.width, let height = player.currentItem?.presentationSize.height else {
            return nil
        }
        if width <= 0 || height <= 0 {
            return nil
        }
        return VideoSize.make(withWidth: Int(width) as NSNumber, height: Int(height) as NSNumber)
    }
    
    public func getPlayerTracksSnapshot() -> PlayerTracksSnapshot {
        guard let currentItem = player.currentItem else {
            return PlayerTracksSnapshot.make(withPlayerId: id, audioTracks: [], textTracks: [], videoTracks: [])
        }
            
        var audioTracks: [Track] = TrackUtils.getAudioTracksForAsset(currentItem.asset, playerItem: currentItem)
        var textTracks: [Track] = TrackUtils.getTextTracksForAsset(currentItem.asset, playerItem: currentItem)
        var videoTracks: [Track] = TrackUtils.getVideoTracksForAsset(currentItem.asset, playerItem: currentItem)
        
        let playerData = MetadataUtils.getNamespacedMetadata(currentItem.externalMetadata, namespace: .BccmPlayer)
        if playerData[PlayerMetadataConstants.IsOffline] == "true" {
            audioTracks = audioTracks.filter { el in el.downloaded == true }
            textTracks = textTracks.filter { el in el.downloaded == true }
            videoTracks = videoTracks.filter { el in el.downloaded == true }
        }

        return PlayerTracksSnapshot.make(
            withPlayerId: id,
            audioTracks: audioTracks,
            textTracks: textTracks,
            videoTracks: videoTracks.sorted { (($0.height?.intValue) ?? 0) > (($1.height?.intValue) ?? 0) }
        )
    }
    
    public func setSelectedTrack(type: TrackType, trackId: String?) {
        guard let currentItem = player.currentItem else {
            debugPrint("Tried to setSelectedTrack, but no item is currently loaded in the player")
            return
        }
        
        let urlAsset = currentItem.asset as? AVURLAsset
        let playerData = MetadataUtils.getNamespacedMetadata(currentItem.externalMetadata, namespace: .BccmPlayer)
        let isOffline = urlAsset != nil && playerData[PlayerMetadataConstants.IsOffline] == "true"
        
        if type == .video {
            if trackId == "auto" {
                currentItem.preferredPeakBitRate = 0
            }
            guard let trackId = trackId, let bitrate = Int(trackId) else {
                debugPrint("Tried to setSelectedTrack for video, but trackId (bitrate): \(trackId?.debugDescription ?? "null") is not an int")
                return
            }
            currentItem.preferredPeakBitRate = Double(bitrate)
            return
        }
        
        guard let mediaCharacteristic = type.asAVMediaCharacteristic() else {
            debugPrint("Tried to setSelectedTrack, but type is unknown: " + type.rawValue.description)
            return
        }
        
        let selectionGroup = currentItem.asset.mediaSelectionGroup(forMediaCharacteristic: mediaCharacteristic)
        guard let selectionGroup = selectionGroup else {
            debugPrint("Tried to setSelectedTrack, but couldn't find a mediaSelectionGroup with characteristic: " + mediaCharacteristic.rawValue)
            return
        }
        guard let trackId = trackId else {
            currentItem.select(nil, in: selectionGroup)
            return
        }
        guard let trackIdInt = Int(trackId) else {
            debugPrint("Tried to setSelectedTrack, but invalid trackId: " + trackId)
            return
        }
        if trackIdInt < selectionGroup.options.count {
            let optionToSelect = selectionGroup.options[trackIdInt]
            
            let offlineOptions = urlAsset?.assetCache?.mediaSelectionOptions(in: selectionGroup)
            let isDownloaded = offlineOptions?.contains(optionToSelect) ?? false
            if isOffline && !isDownloaded {
                debugPrint("Tried to setSelectedTrack offline, but trackId is not downloaded: " + trackId)
                return
            } else {
                currentItem.select(optionToSelect, in: selectionGroup)
            }
        } else {
            print("trackId is out of bounds: " + trackId + " of " + selectionGroup.options.count.description)
        }
    }
    
    public func setPlaybackSpeed(_ speed: Float) {
        if #available(iOS 16, *) {
            player.defaultRate = speed
        }
        if isPlaying() {
            player.rate = speed
        }
        onManualPlayerStateUpdate()
    }
    
    public func getPlaybackSpeed() -> Float {
        if #available(iOS 16, *) {
            return player.defaultRate
        }
        if isPlaying() {
            return player.rate
        } else {
            return 1.0
        }
    }
    
    public func setVolume(_ volume: Float) {
        player.volume = volume
        onManualPlayerStateUpdate()
    }
    
    public func getCurrentItem() -> MediaItem? {
        return MediaItemMapper.mapPlayerItem(player.currentItem)
    }
    
    public func hasBecomePrimary() {
        isPrimary = true
        setupCommandCenter()
    }
    
    public func play() {
        player.play()
    }
    
    public func seekTo(_ positionMs: NSNumber, _ completion: @escaping (Bool) -> Void) {
        player.seek(to: CMTime(value: Int64(truncating: positionMs), timescale: 1000),
                    toleranceBefore: CMTime.zero,
                    toleranceAfter: CMTime.zero,
                    completionHandler: { result in
                        self.onManualPlayerStateUpdate()
                        completion(result)
                    })
    }
    
    public func pause() {
        player.pause()
    }
    
    public func stop(reset: Bool) {
        if reset {
            player.removeAllItems()
        } else {
            player.pause()
        }
    }
    
    public func exitFullscreen() {
        if fullscreenViewController == nil { return }
        fullscreenViewController?.dismiss(animated: true)
        fullscreenViewController = nil
        onManualPlayerStateUpdate()
    }
    
    public func enterFullscreen() {
        // present currentViewController fullscreen on rootviewcontroller
        guard let currentViewController = currentViewController else {
            return
        }
        currentViewController.willMove(toParent: nil)
        currentViewController.removeFromParent()
        // do the following next tick
        DispatchQueue.main.async {
            let rootViewController = UIApplication.shared.keyWindow?.rootViewController
            rootViewController?.present(currentViewController, animated: true)
            self.fullscreenViewController = currentViewController
            self.onManualPlayerStateUpdate()
        }
    }
    
    public func playerViewController(_ playerViewController: AVPlayerViewController, willBeginFullScreenPresentationWithAnimationCoordinator coordinator: UIViewControllerTransitionCoordinator) {
        // Disable animations while transitioning to fullscreen, because it sometimes does a 360deg spin.
        UIView.setAnimationsEnabled(false)
        coordinator.animate(alongsideTransition: { _ in }, completion: {
            _ in
            UIView.setAnimationsEnabled(true)
        })
        fullscreenViewController = playerViewController
        onManualPlayerStateUpdate()
    }
    
    public func playerViewController(_ playerViewController: AVPlayerViewController, willEndFullScreenPresentationWithAnimationCoordinator coordinator: UIViewControllerTransitionCoordinator) {
        UIView.setAnimationsEnabled(false)
        // fullscreenViewController = nil
        coordinator.animate(alongsideTransition: { _ in }, completion: {
            _ in
            UIView.setAnimationsEnabled(true)
            self.fullscreenViewController = nil
            self.onManualPlayerStateUpdate()
        })
    }

    public func playerViewControllerWillStartPictureInPicture(_ playerViewController: AVPlayerViewController) {
        print("bccm: audiosession category willstart: " + AVAudioSession.sharedInstance().category.rawValue)
        registerPipController(playerViewController)
    }
    
    public func playerViewControllerShouldAutomaticallyDismissAtPictureInPictureStart(_ playerViewController: AVPlayerViewController) -> Bool {
        print("playerViewControllerShouldAutomaticallyDismissAtPictureInPictureStart")
        return false
    }
    
    public func playerViewControllerWillStopPictureInPicture(_ playerViewController: AVPlayerViewController) {
        print("bccm: audiosession category willstop: " + AVAudioSession.sharedInstance().category.rawValue)
        registerPipController(nil)
        let audioSession = AVAudioSession.sharedInstance()
        print("bccm: audiosession category before: " + audioSession.category.rawValue)
        do {
            try audioSession.setCategory(.playback)
            try audioSession.setActive(true)
        } catch {
            print("Setting category to AVAudioSessionCategoryPlayback failed.")
        }
    }
    
    func registerPipController(_ playerView: AVPlayerViewController?) {
        pipController = playerView
        let event = PictureInPictureModeChangedEvent.make(withPlayerId: id, isInPipMode: (playerView != nil) as NSNumber)
        playbackListener.onPicture(inPictureModeChanged: event, completion: { _ in })
    }
    
    func releasePlayerView(_ playerView: AVPlayerViewController) {
        if playerView != pipController && playerView != fullscreenViewController {
            print("releasing")
            playerView.player = nil
            if currentViewController == playerView {
                currentViewController = nil
            }
        }
    }
    
    func setupCommandCenter() {
        let commandCenter = MPRemoteCommandCenter.shared()
        commandCenter.playCommand.isEnabled = true
        commandCenter.pauseCommand.isEnabled = true
        commandCenter.playCommand.addTarget { [weak self] _ -> MPRemoteCommandHandlerStatus in
            self?.player.play()
            return .success
        }
        commandCenter.pauseCommand.addTarget { [weak self] _ -> MPRemoteCommandHandlerStatus in
            self?.player.pause()
            return .success
        }
        
        let nowPlayingInfoCenter = MPNowPlayingInfoCenter.default()
        nowPlayingInfoCenter.nowPlayingInfo = [:]
    }
    
    @objc func playAudio() {
        player.play()
    }

    @objc func pauseAudio() {
        player.pause()
    }
    
    private func initYoubora(_ npawConfig: NpawConfig) {
        print("Initializing youbora")
        let youboraOptions = YBOptions()
        youboraOptions.enabled = true
        youboraOptions.accountCode = npawConfig.accountCode
        youboraOptions.appName = npawConfig.appName
        youboraOptions.autoDetectBackground = false
        youboraOptions.userObfuscateIp = true as NSValue
        youboraOptions.appReleaseVersion = npawConfig.appReleaseVersion
        if let deviceIsAnonymous = npawConfig.deviceIsAnonymous?.boolValue {
            youboraOptions.deviceIsAnonymous = deviceIsAnonymous
        }
        youboraPlugin = YBPlugin(options: youboraOptions)
        youboraPlugin!.adapter = YBAVPlayerAdapterSwiftTranformer.transform(from: YBAVPlayerAdapter(player: player))
        updateYouboraOptions()
    }
    
    func updateYouboraOptions(mediaItemOverride: MediaItem? = nil) {
        guard let youboraPlugin = youboraPlugin else {
            return
        }
        youboraPlugin.options.username = appConfig?.analyticsId
        youboraPlugin.options.contentCustomDimension1 = appConfig?.sessionId != nil ? appConfig?.sessionId?.stringValue : nil
        guard let mediaItem = mediaItemOverride ?? getCurrentItem() else {
            youboraPlugin.options.contentIsLive = nil
            youboraPlugin.options.contentId = nil
            youboraPlugin.options.contentTitle = nil
            youboraPlugin.options.contentTvShow = nil
            youboraPlugin.options.contentSeason = nil
            youboraPlugin.options.contentEpisodeTitle = nil
            youboraPlugin.options.offline = false
            return
        }
        let extras = mediaItem.metadata?.safeExtras()
        let isLive = ((extras?["npaw.content.isLive"] as? String) == "true") || (mediaItem.isLive?.boolValue) == true
        youboraPlugin.options.contentIsLive = isLive as NSValue?
        youboraPlugin.options.contentId = extras?["npaw.content.id"] as? String ?? extras?["id"] as? String
        youboraPlugin.options.contentTitle = extras?["npaw.content.title"] as? String ?? mediaItem.metadata?.title
        youboraPlugin.options.contentTvShow = extras?["npaw.content.tvShow"] as? String
        youboraPlugin.options.contentSeason = extras?["npaw.content.season"] as? String
        youboraPlugin.options.contentEpisodeTitle = extras?["npaw.content.episodeTitle"] as? String
        youboraPlugin.options.offline = extras?["npaw.isOffline"] as? String == "true" || (mediaItem.isOffline?.boolValue) == true
    }

    public func setNpawConfig(npawConfig: NpawConfig?) {
        guard let npawConfig = npawConfig else {
            youboraPlugin?.disable()
            return
        }
        if youboraPlugin != nil {
            youboraPlugin?.enable()
            return
        }
        initYoubora(npawConfig)
    }

    public func updateAppConfig(appConfig: AppConfig?) {
        self.appConfig = appConfig
        updateYouboraOptions()
    }
    
    public func replaceCurrentMediaItem(_ mediaItem: MediaItem, autoplay: NSNumber?, completion: @escaping (FlutterError?) -> Void) {
        createPlayerItem(mediaItem) { playerItem in
            guard let playerItem = playerItem else {
                return
            }
            self.updateYouboraOptions(mediaItemOverride: mediaItem)
            DispatchQueue.main.async {
                self.player.replaceCurrentItem(with: playerItem)
                self.temporaryStatusObserver = playerItem.observe(\.status, options: [.new, .old]) {
                    playerItem, _ in
                    if playerItem.status == .readyToPlay {
                        if let playbackStartPositionMs = mediaItem.playbackStartPositionMs {
                            playerItem.seek(to: CMTime(value: Int64(truncating: playbackStartPositionMs), timescale: 1000), completionHandler: nil)
                        }
                        if autoplay?.boolValue == true {
                            self.player.play()
                        }
                        if let audioLanguages = self.appConfig?.audioLanguages {
                            _ = playerItem.setAudioLanguagePrioritized(audioLanguages)
                        }
                        if let subtitleLanguages = self.appConfig?.subtitleLanguages {
                            _ = playerItem.setSubtitleLanguagePrioritized(subtitleLanguages)
                        }
                        
                        // This is the initial signal. If this is not set the language is generally empty in NPAW
                        self.youboraPlugin?.options.contentSubtitles = self.player.currentItem?.getSelectedSubtitleLanguage()
                        self.youboraPlugin?.options.contentLanguage = self.player.currentItem?.getSelectedAudioLanguage()
                        
                        completion(nil)
                    } else if playerItem.status == .failed || playerItem.status == .unknown {
                        print("Mediaitem failed to play")
                        completion(FlutterError(code: "", message: "MediaItem failed to load", details: ["playerItem.status", playerItem.status.rawValue, "playerItem.error", playerItem.error?.localizedDescription]))
                    }
                    self.temporaryStatusObserver?.invalidate()
                    self.temporaryStatusObserver = nil
                }
            }
        }
    }
    
    public func getPlayer() -> AVQueuePlayer {
        return player
    }

    public func queueItem(_ mediaItem: MediaItem) {
        createPlayerItem(mediaItem) { playerItem in
            guard let playerItem = playerItem else {
                return
            }
            DispatchQueue.main.async {
                self.player.insert(playerItem, after: nil)
            }
        }
    }
    
    func takeOwnership(_ playerViewController: AVPlayerViewController) {
        playerViewController.player = player
        currentViewController = playerViewController
    }
    
    func createPlayerItem(_ mediaItem: MediaItem, _ completion: @escaping (AVPlayerItem?) -> Void) {
        DispatchQueue.global(qos: .userInitiated).async {
            guard let urlString = mediaItem.url, let url = URL(string: urlString) else {
                return completion(nil)
            }
            let asset = AVURLAsset(url: url, options: mediaItem.mimeType != nil ? ["AVURLAssetOutOfBandMIMETypeKey": mediaItem.mimeType!] : nil)
            
            asset.resourceLoader.setDelegate(DebugResourceLoaderDelegate(), queue: DispatchQueue.main)
            
            asset.loadValuesAsynchronously(forKeys: ["playable", "duration", "tracks", "availableMediaCharacteristicsWithMediaSelectionOptions"]) {
                let playerItem = self._createPlayerItem(mediaItem, asset)
                completion(playerItem)
            }
        }
    }
    
    private func _setDefaultAudioForOfflinePlayback(_ playerItem: AVPlayerItem, _ asset: AVURLAsset) {
        if let audioLangs = appConfig?.audioLanguages, playerItem.setAudioLanguagePrioritized(audioLangs) {
            return
        }
        if let selectionGroup = asset.mediaSelectionGroup(forMediaCharacteristic: .audible) {
            let offlineOptions = asset.assetCache?.mediaSelectionOptions(in: selectionGroup)
            if offlineOptions?.isEmpty == false {
                playerItem.select(offlineOptions!.first, in: selectionGroup)
            } else {
                debugPrint("No audio track playable offline")
            }
        }
    }
    
    private func _createPlayerItem(_ mediaItem: MediaItem, _ asset: AVAsset) -> AVPlayerItem? {
        let playerItem = AVPlayerItem(asset: asset)
        
        if #available(iOS 12.2, *) {
            var allItems: [AVMetadataItem] = []
            if let metadataItem = MetadataUtils.metadataItem(identifier: AVMetadataIdentifier.commonIdentifierTitle.rawValue, value: mediaItem.metadata?.title as (NSCopying & NSObjectProtocol)?) {
                allItems.append(metadataItem)
            }
            if let artist = mediaItem.metadata?.artist {
                if let metadataItem = MetadataUtils.metadataItem(identifier: AVMetadataIdentifier.commonIdentifierArtist.rawValue, value: artist as (NSCopying & NSObjectProtocol)?) {
                    allItems.append(metadataItem)
                }
            }
            if let artworkUri = mediaItem.metadata?.artworkUri {
                if let url = URL(string: artworkUri) {
                    if let data = try? Data(contentsOf: url) {
                        if let image = UIImage(data: data) {
                            if let artworkItem = MetadataUtils.metadataArtworkItem(image: image) {
                                var externalMetadata = playerItem.externalMetadata
                                externalMetadata.append(artworkItem)
                                playerItem.externalMetadata = externalMetadata
                            }
                        }
                    }
                    if let artworkUriMeta = MetadataUtils.metadataItem(identifier: PlayerMetadataConstants.ArtworkUri, value: artworkUri as (NSCopying & NSObjectProtocol)?, namespace: .BccmPlayer) {
                        playerItem.externalMetadata.append(artworkUriMeta)
                    }
                }
            }
            if let extras = mediaItem.metadata?.safeExtras() {
                for item in extras {
                    if let value = item.value as? (NSCopying & NSObjectProtocol)?,
                       let metadataItem = MetadataUtils.metadataItem(identifier: item.key, value: value, namespace: .BccmExtras)
                    {
                        allItems.append(metadataItem)
                    }
                }
            }
            if let mimeType = mediaItem.mimeType {
                if let metadataItem = MetadataUtils.metadataItem(identifier: PlayerMetadataConstants.MimeType, value: mimeType as (NSCopying & NSObjectProtocol)?, namespace: .BccmPlayer) {
                    allItems.append(metadataItem)
                }
            }
            if let isLive = mediaItem.isLive {
                if let metadataItem = MetadataUtils.metadataItem(identifier: PlayerMetadataConstants.IsLive, value: (isLive.boolValue ? "true" : "false") as (NSCopying & NSObjectProtocol), namespace: .BccmPlayer) {
                    allItems.append(metadataItem)
                }
            }
            if let isOffline = mediaItem.isOffline {
                if let metadataItem = MetadataUtils.metadataItem(identifier: PlayerMetadataConstants.IsOffline, value: (isOffline.boolValue ? "true" : "false") as (NSCopying & NSObjectProtocol), namespace: .BccmPlayer) {
                    allItems.append(metadataItem)
                }
            }
            
            playerItem.externalMetadata.append(contentsOf: allItems)
        }
        
        if mediaItem.isOffline == true, let asset = (asset as? AVURLAsset) {
            _setDefaultAudioForOfflinePlayback(playerItem, asset)
            playerItem.preferredPeakBitRate = 0
        }
        
        return playerItem
    }

    func addObservers() {
        // listening for current item change
        observers.append(player.observe(\.currentItem, options: [.old, .new]) {
            player, _ in
            let mediaItem = MediaItemMapper.mapPlayerItem(player.currentItem)
            let event = MediaItemTransitionEvent.make(withPlayerId: self.id, mediaItem: mediaItem)
            self.playbackListener.onMediaItemTransition(event, completion: { _ in })
            
            self.updateYouboraOptions(mediaItemOverride: mediaItem)
            
            self.observers.append(player.observe(\.currentItem?.duration, options: [.old, .new]) {
                player, _ in
                MPNowPlayingInfoCenter.default().nowPlayingInfo?[MPMediaItemPropertyPlaybackDuration] = player.currentItem?.duration.seconds
            })
            
            self.observers.append(player.observe(\.currentItem?.currentMediaSelection, options: [.old, .new]) {
                player, _ in
                // Update language in NPAW
                self.youboraPlugin?.options.contentLanguage = player.currentItem?.getSelectedAudioLanguage()
                self.youboraPlugin?.options.contentSubtitles = player.currentItem?.getSelectedSubtitleLanguage()
            })
            
            self.observers.append(player.observe(\.currentItem?.presentationSize, options: [.old, .new]) {
                _, _ in
                self.onManualPlayerStateUpdate()
            })
            NotificationCenter.default
                .addObserver(self,
                             selector: #selector(self.playerDidFinishPlaying),
                             name: .AVPlayerItemDidPlayToEndTime,
                             object: player.currentItem)
        })
        observers.append(player.observe(\.rate, options: [.old, .new]) {
            player, change in
            let nowPlayingInfoCenter = MPNowPlayingInfoCenter.default()
            nowPlayingInfoCenter.nowPlayingInfo?[MPNowPlayingInfoPropertyPlaybackRate] = change.newValue
            nowPlayingInfoCenter.nowPlayingInfo?[MPNowPlayingInfoPropertyElapsedPlaybackTime] = player.currentTime().seconds
            let positionDiscontinuityEvent = PositionDiscontinuityEvent.make(withPlayerId: self.id, playbackPositionMs: (player.currentTime().seconds * 1000).rounded() as NSNumber)
            self.playbackListener.onPositionDiscontinuity(positionDiscontinuityEvent, completion: { _ in })
        })
        observers.append(player.observe(\.timeControlStatus, options: [.old, .new]) {
            player, _ in
            let nowPlayingInfoCenter = MPNowPlayingInfoCenter.default()
            nowPlayingInfoCenter.nowPlayingInfo?[MPNowPlayingInfoPropertyPlaybackRate] = player.rate
            nowPlayingInfoCenter.nowPlayingInfo?[MPNowPlayingInfoPropertyElapsedPlaybackTime] = player.currentTime().seconds
            let isPlayingEvent = PlaybackStateChangedEvent.make(
                withPlayerId: self.id,
                playbackState: self.isPlaying() ? PlaybackState.playing : PlaybackState.stopped,
                isBuffering: NSNumber(booleanLiteral: self.isBuffering())
            )
            self.playbackListener.onPlaybackStateChanged(isPlayingEvent, completion: { _ in })
            let positionDiscontinuityEvent = PositionDiscontinuityEvent.make(withPlayerId: self.id, playbackPositionMs: (player.currentTime().seconds * 1000).rounded() as NSNumber)
            self.playbackListener.onPositionDiscontinuity(positionDiscontinuityEvent, completion: { _ in })
        })
    }
    
    func isBuffering() -> Bool {
        return player.timeControlStatus == AVPlayer.TimeControlStatus.waitingToPlayAtSpecifiedRate && player.reasonForWaitingToPlay != .noItemToPlay
    }
    
    func isPlaying() -> Bool {
        // We don't want "isPlaying" to be affected by buffering
        // So we only check if the player is paused or doesnt have an item to play.
        let paused = player.timeControlStatus == AVPlayer.TimeControlStatus.paused
        let waitingBecauseNoItemToPlay = player.timeControlStatus == AVPlayer.TimeControlStatus.waitingToPlayAtSpecifiedRate
            && player.reasonForWaitingToPlay == AVPlayer.WaitingReason.noItemToPlay
        return !paused && !waitingBecauseNoItemToPlay
    }
    
    @objc private func playerDidFinishPlaying(note: NSNotification) {
        let endedEvent = PlaybackEndedEvent.make(withPlayerId: id, mediaItem: getCurrentItem())
        playbackListener.onPlaybackEnded(endedEvent, completion: { _ in })
    }
}

class DebugResourceLoaderDelegate: NSObject, AVAssetResourceLoaderDelegate {
    func resourceLoader(_ resourceLoader: AVAssetResourceLoader, didCancel authenticationChallenge: URLAuthenticationChallenge) {
        print("bccm: authenticationChallenge")
    }

    func resourceLoader(_ resourceLoader: AVAssetResourceLoader, didCancel loadingRequest: AVAssetResourceLoadingRequest) {
        print("bccm: loadingRequest")
    }

    func resourceLoader(_ resourceLoader: AVAssetResourceLoader, shouldWaitForRenewalOfRequestedResource renewalRequest: AVAssetResourceRenewalRequest) -> Bool {
        print("bccm: renewalRequest")
        return false
    }

    func resourceLoader(_ resourceLoader: AVAssetResourceLoader, shouldWaitForResponseTo authenticationChallenge: URLAuthenticationChallenge) -> Bool {
        print("bccm: authenticationChallenge")
        return false
    }

    func resourceLoader(_ resourceLoader: AVAssetResourceLoader, shouldWaitForLoadingOfRequestedResource loadingRequest: AVAssetResourceLoadingRequest) -> Bool {
        print("bccm: shouldWaitForLoadingOfRequestedResource")
        return false
    }
}
