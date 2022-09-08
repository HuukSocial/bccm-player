// Autogenerated from Pigeon (v3.2.7), do not edit directly.
// See also: https://pub.dev/packages/pigeon

package media.bcc.player;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.flutter.plugin.common.BasicMessageChannel;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MessageCodec;
import io.flutter.plugin.common.StandardMessageCodec;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/** Generated class from Pigeon. */
@SuppressWarnings({"unused", "unchecked", "CodeBlock2Expr", "RedundantSuppression"})
public class PlaybackPlatformApi {

  public enum CastConnectionState {
    _(0),
    noDevicesAvailable(1),
    notConnected(2),
    connecting(3),
    connected(4);

    private int index;
    private CastConnectionState(final int index) {
      this.index = index;
    }
  }

  /** Generated class from Pigeon that represents data sent in messages. */
  public static class MediaItem {
    private @NonNull String url;
    public @NonNull String getUrl() { return url; }
    public void setUrl(@NonNull String setterArg) {
      if (setterArg == null) {
        throw new IllegalStateException("Nonnull field \"url\" is null.");
      }
      this.url = setterArg;
    }

    private @Nullable String mimeType;
    public @Nullable String getMimeType() { return mimeType; }
    public void setMimeType(@Nullable String setterArg) {
      this.mimeType = setterArg;
    }

    private @Nullable MediaMetadata metadata;
    public @Nullable MediaMetadata getMetadata() { return metadata; }
    public void setMetadata(@Nullable MediaMetadata setterArg) {
      this.metadata = setterArg;
    }

    private @Nullable Boolean isLive;
    public @Nullable Boolean getIsLive() { return isLive; }
    public void setIsLive(@Nullable Boolean setterArg) {
      this.isLive = setterArg;
    }

    private @Nullable Long playbackStartPositionMs;
    public @Nullable Long getPlaybackStartPositionMs() { return playbackStartPositionMs; }
    public void setPlaybackStartPositionMs(@Nullable Long setterArg) {
      this.playbackStartPositionMs = setterArg;
    }

    /** Constructor is private to enforce null safety; use Builder. */
    private MediaItem() {}
    public static final class Builder {
      private @Nullable String url;
      public @NonNull Builder setUrl(@NonNull String setterArg) {
        this.url = setterArg;
        return this;
      }
      private @Nullable String mimeType;
      public @NonNull Builder setMimeType(@Nullable String setterArg) {
        this.mimeType = setterArg;
        return this;
      }
      private @Nullable MediaMetadata metadata;
      public @NonNull Builder setMetadata(@Nullable MediaMetadata setterArg) {
        this.metadata = setterArg;
        return this;
      }
      private @Nullable Boolean isLive;
      public @NonNull Builder setIsLive(@Nullable Boolean setterArg) {
        this.isLive = setterArg;
        return this;
      }
      private @Nullable Long playbackStartPositionMs;
      public @NonNull Builder setPlaybackStartPositionMs(@Nullable Long setterArg) {
        this.playbackStartPositionMs = setterArg;
        return this;
      }
      public @NonNull MediaItem build() {
        MediaItem pigeonReturn = new MediaItem();
        pigeonReturn.setUrl(url);
        pigeonReturn.setMimeType(mimeType);
        pigeonReturn.setMetadata(metadata);
        pigeonReturn.setIsLive(isLive);
        pigeonReturn.setPlaybackStartPositionMs(playbackStartPositionMs);
        return pigeonReturn;
      }
    }
    @NonNull Map<String, Object> toMap() {
      Map<String, Object> toMapResult = new HashMap<>();
      toMapResult.put("url", url);
      toMapResult.put("mimeType", mimeType);
      toMapResult.put("metadata", (metadata == null) ? null : metadata.toMap());
      toMapResult.put("isLive", isLive);
      toMapResult.put("playbackStartPositionMs", playbackStartPositionMs);
      return toMapResult;
    }
    static @NonNull MediaItem fromMap(@NonNull Map<String, Object> map) {
      MediaItem pigeonResult = new MediaItem();
      Object url = map.get("url");
      pigeonResult.setUrl((String)url);
      Object mimeType = map.get("mimeType");
      pigeonResult.setMimeType((String)mimeType);
      Object metadata = map.get("metadata");
      pigeonResult.setMetadata((metadata == null) ? null : MediaMetadata.fromMap((Map)metadata));
      Object isLive = map.get("isLive");
      pigeonResult.setIsLive((Boolean)isLive);
      Object playbackStartPositionMs = map.get("playbackStartPositionMs");
      pigeonResult.setPlaybackStartPositionMs((playbackStartPositionMs == null) ? null : ((playbackStartPositionMs instanceof Integer) ? (Integer)playbackStartPositionMs : (Long)playbackStartPositionMs));
      return pigeonResult;
    }
  }

  /** Generated class from Pigeon that represents data sent in messages. */
  public static class MediaMetadata {
    private @Nullable String artworkUri;
    public @Nullable String getArtworkUri() { return artworkUri; }
    public void setArtworkUri(@Nullable String setterArg) {
      this.artworkUri = setterArg;
    }

    private @Nullable String title;
    public @Nullable String getTitle() { return title; }
    public void setTitle(@Nullable String setterArg) {
      this.title = setterArg;
    }

    private @Nullable String artist;
    public @Nullable String getArtist() { return artist; }
    public void setArtist(@Nullable String setterArg) {
      this.artist = setterArg;
    }

    private @Nullable String episodeId;
    public @Nullable String getEpisodeId() { return episodeId; }
    public void setEpisodeId(@Nullable String setterArg) {
      this.episodeId = setterArg;
    }

    public static final class Builder {
      private @Nullable String artworkUri;
      public @NonNull Builder setArtworkUri(@Nullable String setterArg) {
        this.artworkUri = setterArg;
        return this;
      }
      private @Nullable String title;
      public @NonNull Builder setTitle(@Nullable String setterArg) {
        this.title = setterArg;
        return this;
      }
      private @Nullable String artist;
      public @NonNull Builder setArtist(@Nullable String setterArg) {
        this.artist = setterArg;
        return this;
      }
      private @Nullable String episodeId;
      public @NonNull Builder setEpisodeId(@Nullable String setterArg) {
        this.episodeId = setterArg;
        return this;
      }
      public @NonNull MediaMetadata build() {
        MediaMetadata pigeonReturn = new MediaMetadata();
        pigeonReturn.setArtworkUri(artworkUri);
        pigeonReturn.setTitle(title);
        pigeonReturn.setArtist(artist);
        pigeonReturn.setEpisodeId(episodeId);
        return pigeonReturn;
      }
    }
    @NonNull Map<String, Object> toMap() {
      Map<String, Object> toMapResult = new HashMap<>();
      toMapResult.put("artworkUri", artworkUri);
      toMapResult.put("title", title);
      toMapResult.put("artist", artist);
      toMapResult.put("episodeId", episodeId);
      return toMapResult;
    }
    static @NonNull MediaMetadata fromMap(@NonNull Map<String, Object> map) {
      MediaMetadata pigeonResult = new MediaMetadata();
      Object artworkUri = map.get("artworkUri");
      pigeonResult.setArtworkUri((String)artworkUri);
      Object title = map.get("title");
      pigeonResult.setTitle((String)title);
      Object artist = map.get("artist");
      pigeonResult.setArtist((String)artist);
      Object episodeId = map.get("episodeId");
      pigeonResult.setEpisodeId((String)episodeId);
      return pigeonResult;
    }
  }

  /** Generated class from Pigeon that represents data sent in messages. */
  public static class ChromecastState {
    private @NonNull CastConnectionState connectionState;
    public @NonNull CastConnectionState getConnectionState() { return connectionState; }
    public void setConnectionState(@NonNull CastConnectionState setterArg) {
      if (setterArg == null) {
        throw new IllegalStateException("Nonnull field \"connectionState\" is null.");
      }
      this.connectionState = setterArg;
    }

    /** Constructor is private to enforce null safety; use Builder. */
    private ChromecastState() {}
    public static final class Builder {
      private @Nullable CastConnectionState connectionState;
      public @NonNull Builder setConnectionState(@NonNull CastConnectionState setterArg) {
        this.connectionState = setterArg;
        return this;
      }
      public @NonNull ChromecastState build() {
        ChromecastState pigeonReturn = new ChromecastState();
        pigeonReturn.setConnectionState(connectionState);
        return pigeonReturn;
      }
    }
    @NonNull Map<String, Object> toMap() {
      Map<String, Object> toMapResult = new HashMap<>();
      toMapResult.put("connectionState", connectionState == null ? null : connectionState.index);
      return toMapResult;
    }
    static @NonNull ChromecastState fromMap(@NonNull Map<String, Object> map) {
      ChromecastState pigeonResult = new ChromecastState();
      Object connectionState = map.get("connectionState");
      pigeonResult.setConnectionState(connectionState == null ? null : CastConnectionState.values()[(int)connectionState]);
      return pigeonResult;
    }
  }

  /** Generated class from Pigeon that represents data sent in messages. */
  public static class PositionUpdateEvent {
    private @NonNull String playerId;
    public @NonNull String getPlayerId() { return playerId; }
    public void setPlayerId(@NonNull String setterArg) {
      if (setterArg == null) {
        throw new IllegalStateException("Nonnull field \"playerId\" is null.");
      }
      this.playerId = setterArg;
    }

    private @Nullable Long playbackPositionMs;
    public @Nullable Long getPlaybackPositionMs() { return playbackPositionMs; }
    public void setPlaybackPositionMs(@Nullable Long setterArg) {
      this.playbackPositionMs = setterArg;
    }

    /** Constructor is private to enforce null safety; use Builder. */
    private PositionUpdateEvent() {}
    public static final class Builder {
      private @Nullable String playerId;
      public @NonNull Builder setPlayerId(@NonNull String setterArg) {
        this.playerId = setterArg;
        return this;
      }
      private @Nullable Long playbackPositionMs;
      public @NonNull Builder setPlaybackPositionMs(@Nullable Long setterArg) {
        this.playbackPositionMs = setterArg;
        return this;
      }
      public @NonNull PositionUpdateEvent build() {
        PositionUpdateEvent pigeonReturn = new PositionUpdateEvent();
        pigeonReturn.setPlayerId(playerId);
        pigeonReturn.setPlaybackPositionMs(playbackPositionMs);
        return pigeonReturn;
      }
    }
    @NonNull Map<String, Object> toMap() {
      Map<String, Object> toMapResult = new HashMap<>();
      toMapResult.put("playerId", playerId);
      toMapResult.put("playbackPositionMs", playbackPositionMs);
      return toMapResult;
    }
    static @NonNull PositionUpdateEvent fromMap(@NonNull Map<String, Object> map) {
      PositionUpdateEvent pigeonResult = new PositionUpdateEvent();
      Object playerId = map.get("playerId");
      pigeonResult.setPlayerId((String)playerId);
      Object playbackPositionMs = map.get("playbackPositionMs");
      pigeonResult.setPlaybackPositionMs((playbackPositionMs == null) ? null : ((playbackPositionMs instanceof Integer) ? (Integer)playbackPositionMs : (Long)playbackPositionMs));
      return pigeonResult;
    }
  }

  /** Generated class from Pigeon that represents data sent in messages. */
  public static class IsPlayingChangedEvent {
    private @NonNull String playerId;
    public @NonNull String getPlayerId() { return playerId; }
    public void setPlayerId(@NonNull String setterArg) {
      if (setterArg == null) {
        throw new IllegalStateException("Nonnull field \"playerId\" is null.");
      }
      this.playerId = setterArg;
    }

    private @NonNull Boolean isPlaying;
    public @NonNull Boolean getIsPlaying() { return isPlaying; }
    public void setIsPlaying(@NonNull Boolean setterArg) {
      if (setterArg == null) {
        throw new IllegalStateException("Nonnull field \"isPlaying\" is null.");
      }
      this.isPlaying = setterArg;
    }

    /** Constructor is private to enforce null safety; use Builder. */
    private IsPlayingChangedEvent() {}
    public static final class Builder {
      private @Nullable String playerId;
      public @NonNull Builder setPlayerId(@NonNull String setterArg) {
        this.playerId = setterArg;
        return this;
      }
      private @Nullable Boolean isPlaying;
      public @NonNull Builder setIsPlaying(@NonNull Boolean setterArg) {
        this.isPlaying = setterArg;
        return this;
      }
      public @NonNull IsPlayingChangedEvent build() {
        IsPlayingChangedEvent pigeonReturn = new IsPlayingChangedEvent();
        pigeonReturn.setPlayerId(playerId);
        pigeonReturn.setIsPlaying(isPlaying);
        return pigeonReturn;
      }
    }
    @NonNull Map<String, Object> toMap() {
      Map<String, Object> toMapResult = new HashMap<>();
      toMapResult.put("playerId", playerId);
      toMapResult.put("isPlaying", isPlaying);
      return toMapResult;
    }
    static @NonNull IsPlayingChangedEvent fromMap(@NonNull Map<String, Object> map) {
      IsPlayingChangedEvent pigeonResult = new IsPlayingChangedEvent();
      Object playerId = map.get("playerId");
      pigeonResult.setPlayerId((String)playerId);
      Object isPlaying = map.get("isPlaying");
      pigeonResult.setIsPlaying((Boolean)isPlaying);
      return pigeonResult;
    }
  }

  /** Generated class from Pigeon that represents data sent in messages. */
  public static class MediaItemTransitionEvent {
    private @NonNull String playerId;
    public @NonNull String getPlayerId() { return playerId; }
    public void setPlayerId(@NonNull String setterArg) {
      if (setterArg == null) {
        throw new IllegalStateException("Nonnull field \"playerId\" is null.");
      }
      this.playerId = setterArg;
    }

    private @Nullable MediaItem mediaItem;
    public @Nullable MediaItem getMediaItem() { return mediaItem; }
    public void setMediaItem(@Nullable MediaItem setterArg) {
      this.mediaItem = setterArg;
    }

    /** Constructor is private to enforce null safety; use Builder. */
    private MediaItemTransitionEvent() {}
    public static final class Builder {
      private @Nullable String playerId;
      public @NonNull Builder setPlayerId(@NonNull String setterArg) {
        this.playerId = setterArg;
        return this;
      }
      private @Nullable MediaItem mediaItem;
      public @NonNull Builder setMediaItem(@Nullable MediaItem setterArg) {
        this.mediaItem = setterArg;
        return this;
      }
      public @NonNull MediaItemTransitionEvent build() {
        MediaItemTransitionEvent pigeonReturn = new MediaItemTransitionEvent();
        pigeonReturn.setPlayerId(playerId);
        pigeonReturn.setMediaItem(mediaItem);
        return pigeonReturn;
      }
    }
    @NonNull Map<String, Object> toMap() {
      Map<String, Object> toMapResult = new HashMap<>();
      toMapResult.put("playerId", playerId);
      toMapResult.put("mediaItem", (mediaItem == null) ? null : mediaItem.toMap());
      return toMapResult;
    }
    static @NonNull MediaItemTransitionEvent fromMap(@NonNull Map<String, Object> map) {
      MediaItemTransitionEvent pigeonResult = new MediaItemTransitionEvent();
      Object playerId = map.get("playerId");
      pigeonResult.setPlayerId((String)playerId);
      Object mediaItem = map.get("mediaItem");
      pigeonResult.setMediaItem((mediaItem == null) ? null : MediaItem.fromMap((Map)mediaItem));
      return pigeonResult;
    }
  }

  public interface Result<T> {
    void success(T result);
    void error(Throwable error);
  }
  private static class PlaybackPlatformPigeonCodec extends StandardMessageCodec {
    public static final PlaybackPlatformPigeonCodec INSTANCE = new PlaybackPlatformPigeonCodec();
    private PlaybackPlatformPigeonCodec() {}
    @Override
    protected Object readValueOfType(byte type, ByteBuffer buffer) {
      switch (type) {
        case (byte)128:         
          return ChromecastState.fromMap((Map<String, Object>) readValue(buffer));
        
        case (byte)129:         
          return MediaItem.fromMap((Map<String, Object>) readValue(buffer));
        
        case (byte)130:         
          return MediaMetadata.fromMap((Map<String, Object>) readValue(buffer));
        
        default:        
          return super.readValueOfType(type, buffer);
        
      }
    }
    @Override
    protected void writeValue(ByteArrayOutputStream stream, Object value)     {
      if (value instanceof ChromecastState) {
        stream.write(128);
        writeValue(stream, ((ChromecastState) value).toMap());
      } else 
      if (value instanceof MediaItem) {
        stream.write(129);
        writeValue(stream, ((MediaItem) value).toMap());
      } else 
      if (value instanceof MediaMetadata) {
        stream.write(130);
        writeValue(stream, ((MediaMetadata) value).toMap());
      } else 
{
        super.writeValue(stream, value);
      }
    }
  }

  /** Generated interface from Pigeon that represents a handler of messages from Flutter.*/
  public interface PlaybackPlatformPigeon {
    void newPlayer(@Nullable String url, Result<String> result);
    void queueMediaItem(@NonNull String playerId, @NonNull MediaItem mediaItem, Result<Void> result);
    void replaceCurrentMediaItem(@NonNull String playerId, @NonNull MediaItem mediaItem, @Nullable Boolean playbackPositionFromPrimary, Result<Void> result);
    void setPrimary(@NonNull String id, Result<Void> result);
    void play(@NonNull String playerId);
    void pause(@NonNull String playerId);
    void stop(@NonNull String playerId, @NonNull Boolean reset);
    void getChromecastState(Result<ChromecastState> result);

    /** The codec used by PlaybackPlatformPigeon. */
    static MessageCodec<Object> getCodec() {
      return PlaybackPlatformPigeonCodec.INSTANCE;
    }

    /** Sets up an instance of `PlaybackPlatformPigeon` to handle messages through the `binaryMessenger`. */
    static void setup(BinaryMessenger binaryMessenger, PlaybackPlatformPigeon api) {
      {
        BasicMessageChannel<Object> channel =
            new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.PlaybackPlatformPigeon.newPlayer", getCodec());
        if (api != null) {
          channel.setMessageHandler((message, reply) -> {
            Map<String, Object> wrapped = new HashMap<>();
            try {
              ArrayList<Object> args = (ArrayList<Object>)message;
              String urlArg = (String)args.get(0);
              Result<String> resultCallback = new Result<String>() {
                public void success(String result) {
                  wrapped.put("result", result);
                  reply.reply(wrapped);
                }
                public void error(Throwable error) {
                  wrapped.put("error", wrapError(error));
                  reply.reply(wrapped);
                }
              };

              api.newPlayer(urlArg, resultCallback);
            }
            catch (Error | RuntimeException exception) {
              wrapped.put("error", wrapError(exception));
              reply.reply(wrapped);
            }
          });
        } else {
          channel.setMessageHandler(null);
        }
      }
      {
        BasicMessageChannel<Object> channel =
            new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.PlaybackPlatformPigeon.queueMediaItem", getCodec());
        if (api != null) {
          channel.setMessageHandler((message, reply) -> {
            Map<String, Object> wrapped = new HashMap<>();
            try {
              ArrayList<Object> args = (ArrayList<Object>)message;
              String playerIdArg = (String)args.get(0);
              if (playerIdArg == null) {
                throw new NullPointerException("playerIdArg unexpectedly null.");
              }
              MediaItem mediaItemArg = (MediaItem)args.get(1);
              if (mediaItemArg == null) {
                throw new NullPointerException("mediaItemArg unexpectedly null.");
              }
              Result<Void> resultCallback = new Result<Void>() {
                public void success(Void result) {
                  wrapped.put("result", null);
                  reply.reply(wrapped);
                }
                public void error(Throwable error) {
                  wrapped.put("error", wrapError(error));
                  reply.reply(wrapped);
                }
              };

              api.queueMediaItem(playerIdArg, mediaItemArg, resultCallback);
            }
            catch (Error | RuntimeException exception) {
              wrapped.put("error", wrapError(exception));
              reply.reply(wrapped);
            }
          });
        } else {
          channel.setMessageHandler(null);
        }
      }
      {
        BasicMessageChannel<Object> channel =
            new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.PlaybackPlatformPigeon.replaceCurrentMediaItem", getCodec());
        if (api != null) {
          channel.setMessageHandler((message, reply) -> {
            Map<String, Object> wrapped = new HashMap<>();
            try {
              ArrayList<Object> args = (ArrayList<Object>)message;
              String playerIdArg = (String)args.get(0);
              if (playerIdArg == null) {
                throw new NullPointerException("playerIdArg unexpectedly null.");
              }
              MediaItem mediaItemArg = (MediaItem)args.get(1);
              if (mediaItemArg == null) {
                throw new NullPointerException("mediaItemArg unexpectedly null.");
              }
              Boolean playbackPositionFromPrimaryArg = (Boolean)args.get(2);
              Result<Void> resultCallback = new Result<Void>() {
                public void success(Void result) {
                  wrapped.put("result", null);
                  reply.reply(wrapped);
                }
                public void error(Throwable error) {
                  wrapped.put("error", wrapError(error));
                  reply.reply(wrapped);
                }
              };

              api.replaceCurrentMediaItem(playerIdArg, mediaItemArg, playbackPositionFromPrimaryArg, resultCallback);
            }
            catch (Error | RuntimeException exception) {
              wrapped.put("error", wrapError(exception));
              reply.reply(wrapped);
            }
          });
        } else {
          channel.setMessageHandler(null);
        }
      }
      {
        BasicMessageChannel<Object> channel =
            new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.PlaybackPlatformPigeon.setPrimary", getCodec());
        if (api != null) {
          channel.setMessageHandler((message, reply) -> {
            Map<String, Object> wrapped = new HashMap<>();
            try {
              ArrayList<Object> args = (ArrayList<Object>)message;
              String idArg = (String)args.get(0);
              if (idArg == null) {
                throw new NullPointerException("idArg unexpectedly null.");
              }
              Result<Void> resultCallback = new Result<Void>() {
                public void success(Void result) {
                  wrapped.put("result", null);
                  reply.reply(wrapped);
                }
                public void error(Throwable error) {
                  wrapped.put("error", wrapError(error));
                  reply.reply(wrapped);
                }
              };

              api.setPrimary(idArg, resultCallback);
            }
            catch (Error | RuntimeException exception) {
              wrapped.put("error", wrapError(exception));
              reply.reply(wrapped);
            }
          });
        } else {
          channel.setMessageHandler(null);
        }
      }
      {
        BasicMessageChannel<Object> channel =
            new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.PlaybackPlatformPigeon.play", getCodec());
        if (api != null) {
          channel.setMessageHandler((message, reply) -> {
            Map<String, Object> wrapped = new HashMap<>();
            try {
              ArrayList<Object> args = (ArrayList<Object>)message;
              String playerIdArg = (String)args.get(0);
              if (playerIdArg == null) {
                throw new NullPointerException("playerIdArg unexpectedly null.");
              }
              api.play(playerIdArg);
              wrapped.put("result", null);
            }
            catch (Error | RuntimeException exception) {
              wrapped.put("error", wrapError(exception));
            }
            reply.reply(wrapped);
          });
        } else {
          channel.setMessageHandler(null);
        }
      }
      {
        BasicMessageChannel<Object> channel =
            new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.PlaybackPlatformPigeon.pause", getCodec());
        if (api != null) {
          channel.setMessageHandler((message, reply) -> {
            Map<String, Object> wrapped = new HashMap<>();
            try {
              ArrayList<Object> args = (ArrayList<Object>)message;
              String playerIdArg = (String)args.get(0);
              if (playerIdArg == null) {
                throw new NullPointerException("playerIdArg unexpectedly null.");
              }
              api.pause(playerIdArg);
              wrapped.put("result", null);
            }
            catch (Error | RuntimeException exception) {
              wrapped.put("error", wrapError(exception));
            }
            reply.reply(wrapped);
          });
        } else {
          channel.setMessageHandler(null);
        }
      }
      {
        BasicMessageChannel<Object> channel =
            new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.PlaybackPlatformPigeon.stop", getCodec());
        if (api != null) {
          channel.setMessageHandler((message, reply) -> {
            Map<String, Object> wrapped = new HashMap<>();
            try {
              ArrayList<Object> args = (ArrayList<Object>)message;
              String playerIdArg = (String)args.get(0);
              if (playerIdArg == null) {
                throw new NullPointerException("playerIdArg unexpectedly null.");
              }
              Boolean resetArg = (Boolean)args.get(1);
              if (resetArg == null) {
                throw new NullPointerException("resetArg unexpectedly null.");
              }
              api.stop(playerIdArg, resetArg);
              wrapped.put("result", null);
            }
            catch (Error | RuntimeException exception) {
              wrapped.put("error", wrapError(exception));
            }
            reply.reply(wrapped);
          });
        } else {
          channel.setMessageHandler(null);
        }
      }
      {
        BasicMessageChannel<Object> channel =
            new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.PlaybackPlatformPigeon.getChromecastState", getCodec());
        if (api != null) {
          channel.setMessageHandler((message, reply) -> {
            Map<String, Object> wrapped = new HashMap<>();
            try {
              Result<ChromecastState> resultCallback = new Result<ChromecastState>() {
                public void success(ChromecastState result) {
                  wrapped.put("result", result);
                  reply.reply(wrapped);
                }
                public void error(Throwable error) {
                  wrapped.put("error", wrapError(error));
                  reply.reply(wrapped);
                }
              };

              api.getChromecastState(resultCallback);
            }
            catch (Error | RuntimeException exception) {
              wrapped.put("error", wrapError(exception));
              reply.reply(wrapped);
            }
          });
        } else {
          channel.setMessageHandler(null);
        }
      }
    }
  }
  private static class PlaybackListenerPigeonCodec extends StandardMessageCodec {
    public static final PlaybackListenerPigeonCodec INSTANCE = new PlaybackListenerPigeonCodec();
    private PlaybackListenerPigeonCodec() {}
    @Override
    protected Object readValueOfType(byte type, ByteBuffer buffer) {
      switch (type) {
        case (byte)128:         
          return IsPlayingChangedEvent.fromMap((Map<String, Object>) readValue(buffer));
        
        case (byte)129:         
          return MediaItem.fromMap((Map<String, Object>) readValue(buffer));
        
        case (byte)130:         
          return MediaItemTransitionEvent.fromMap((Map<String, Object>) readValue(buffer));
        
        case (byte)131:         
          return MediaMetadata.fromMap((Map<String, Object>) readValue(buffer));
        
        case (byte)132:         
          return PositionUpdateEvent.fromMap((Map<String, Object>) readValue(buffer));
        
        default:        
          return super.readValueOfType(type, buffer);
        
      }
    }
    @Override
    protected void writeValue(ByteArrayOutputStream stream, Object value)     {
      if (value instanceof IsPlayingChangedEvent) {
        stream.write(128);
        writeValue(stream, ((IsPlayingChangedEvent) value).toMap());
      } else 
      if (value instanceof MediaItem) {
        stream.write(129);
        writeValue(stream, ((MediaItem) value).toMap());
      } else 
      if (value instanceof MediaItemTransitionEvent) {
        stream.write(130);
        writeValue(stream, ((MediaItemTransitionEvent) value).toMap());
      } else 
      if (value instanceof MediaMetadata) {
        stream.write(131);
        writeValue(stream, ((MediaMetadata) value).toMap());
      } else 
      if (value instanceof PositionUpdateEvent) {
        stream.write(132);
        writeValue(stream, ((PositionUpdateEvent) value).toMap());
      } else 
{
        super.writeValue(stream, value);
      }
    }
  }

  /** Generated class from Pigeon that represents Flutter messages that can be called from Java.*/
  public static class PlaybackListenerPigeon {
    private final BinaryMessenger binaryMessenger;
    public PlaybackListenerPigeon(BinaryMessenger argBinaryMessenger){
      this.binaryMessenger = argBinaryMessenger;
    }
    public interface Reply<T> {
      void reply(T reply);
    }
    static MessageCodec<Object> getCodec() {
      return PlaybackListenerPigeonCodec.INSTANCE;
    }

    public void onPositionUpdate(@NonNull PositionUpdateEvent eventArg, Reply<Void> callback) {
      BasicMessageChannel<Object> channel =
          new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.PlaybackListenerPigeon.onPositionUpdate", getCodec());
      channel.send(new ArrayList<Object>(Arrays.asList(eventArg)), channelReply -> {
        callback.reply(null);
      });
    }
    public void onIsPlayingChanged(@NonNull IsPlayingChangedEvent eventArg, Reply<Void> callback) {
      BasicMessageChannel<Object> channel =
          new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.PlaybackListenerPigeon.onIsPlayingChanged", getCodec());
      channel.send(new ArrayList<Object>(Arrays.asList(eventArg)), channelReply -> {
        callback.reply(null);
      });
    }
    public void onMediaItemTransition(@NonNull MediaItemTransitionEvent eventArg, Reply<Void> callback) {
      BasicMessageChannel<Object> channel =
          new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.PlaybackListenerPigeon.onMediaItemTransition", getCodec());
      channel.send(new ArrayList<Object>(Arrays.asList(eventArg)), channelReply -> {
        callback.reply(null);
      });
    }
  }
  private static Map<String, Object> wrapError(Throwable exception) {
    Map<String, Object> errorMap = new HashMap<>();
    errorMap.put("message", exception.toString());
    errorMap.put("code", exception.getClass().getSimpleName());
    errorMap.put("details", "Cause: " + exception.getCause() + ", Stacktrace: " + Log.getStackTraceString(exception));
    return errorMap;
  }
}
