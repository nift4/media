/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.media3.exoplayer.audio;

import static androidx.media3.common.util.Util.castNonNull;

import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodec.CodecException;
import android.os.Handler;
import android.os.SystemClock;
import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.Player;
import androidx.media3.common.util.Assertions;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.decoder.DecoderException;
import androidx.media3.exoplayer.DecoderCounters;
import androidx.media3.exoplayer.DecoderReuseEvaluation;
import androidx.media3.exoplayer.Renderer;

/**
 * Listener of audio {@link Renderer} events. All methods have no-op default implementations to
 * allow selective overrides.
 */
@UnstableApi
public interface AudioRendererEventListener {

  /**
   * Called when the renderer is enabled.
   *
   * @param counters {@link DecoderCounters} that will be updated by the renderer for as long as it
   *     remains enabled.
   */
  default void onAudioEnabled(DecoderCounters counters) {}

  /**
   * Called when a decoder is created.
   *
   * @param decoderName The decoder that was created.
   * @param initializedTimestampMs {@link SystemClock#elapsedRealtime()} when initialization
   *     finished.
   * @param initializationDurationMs The time taken to initialize the decoder in milliseconds.
   */
  default void onAudioDecoderInitialized(
      String decoderName, long initializedTimestampMs, long initializationDurationMs) {}

  /**
   * Called when the format of the media being consumed by the renderer changes.
   *
   * @param format The new format.
   * @param decoderReuseEvaluation The result of the evaluation to determine whether an existing
   *     decoder instance can be reused for the new format, or {@code null} if the renderer did not
   *     have a decoder.
   */
  default void onAudioInputFormatChanged(
      Format format, @Nullable DecoderReuseEvaluation decoderReuseEvaluation) {}

  /**
   * Called when the audio position has increased for the first time since the last pause or
   * position reset.
   *
   * @param playoutStartSystemTimeMs The approximate derived {@link System#currentTimeMillis()} at
   *     which playout started.
   */
  default void onAudioPositionAdvancing(long playoutStartSystemTimeMs) {}

  /**
   * Called when an audio underrun occurs.
   *
   * @param bufferSize The size of the audio output buffer, in bytes.
   * @param bufferSizeMs The size of the audio output buffer, in milliseconds, if it contains PCM
   *     encoded audio. {@link C#TIME_UNSET} if the output buffer contains non-PCM encoded audio.
   * @param elapsedSinceLastFeedMs The time since audio was last written to the output buffer.
   */
  default void onAudioUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {}

  /**
   * Called when a decoder is released.
   *
   * @param decoderName The decoder that was released.
   */
  default void onAudioDecoderReleased(String decoderName) {}

  /**
   * Called when the renderer is disabled.
   *
   * @param counters {@link DecoderCounters} that were updated by the renderer.
   */
  default void onAudioDisabled(DecoderCounters counters) {}

  /**
   * Called when skipping silences is enabled or disabled in the audio stream.
   *
   * @param skipSilenceEnabled Whether skipping silences in the audio stream is enabled.
   */
  default void onSkipSilenceEnabledChanged(boolean skipSilenceEnabled) {}

  /**
   * Called when an audio decoder encounters an error.
   *
   * <p>This method being called does not indicate that playback has failed, or that it will fail.
   * The player may be able to recover from the error. Hence applications should <em>not</em>
   * implement this method to display a user visible error or initiate an application level retry.
   * {@link Player.Listener#onPlayerError} is the appropriate place to implement such behavior. This
   * method is called to provide the application with an opportunity to log the error if it wishes
   * to do so.
   *
   * @param audioCodecError The error. Typically a {@link CodecException} if the renderer uses
   *     {@link MediaCodec}, or a {@link DecoderException} if the renderer uses a software decoder.
   */
  default void onAudioCodecError(Exception audioCodecError) {}

  /**
   * Called when {@link AudioSink} has encountered an error.
   *
   * <p>If the sink writes to a platform {@link AudioTrack}, this will be called for all {@link
   * AudioTrack} errors.
   *
   * <p>This method being called does not indicate that playback has failed, or that it will fail.
   * The player may be able to recover from the error. Hence applications should <em>not</em>
   * implement this method to display a user visible error or initiate an application level retry.
   * {@link Player.Listener#onPlayerError} is the appropriate place to implement such behavior. This
   * method is called to provide the application with an opportunity to log the error if it wishes
   * to do so.
   *
   * @param audioSinkError The error that occurred. Typically an {@link
   *     AudioSink.InitializationException}, a {@link AudioSink.WriteException}, or an {@link
   *     AudioSink.UnexpectedDiscontinuityException}.
   */
  default void onAudioSinkError(Exception audioSinkError) {}

  /**
   * Called when an {@link AudioTrack} has been initialized.
   *
   * @param audioTrackConfig The {@link AudioSink.AudioTrackConfig} of the initialized {@link
   *     AudioTrack}.
   */
  default void onAudioTrackInitialized(AudioSink.AudioTrackConfig audioTrackConfig) {}

  /**
   * Called when an {@link AudioTrack} has been released.
   *
   * @param audioTrackConfig The {@link AudioSink.AudioTrackConfig} of the released {@link
   *     AudioTrack}.
   */
  default void onAudioTrackReleased(AudioSink.AudioTrackConfig audioTrackConfig) {}

  /**
   * Called when the audio session id changed.
   *
   * @param audioSessionId The new audio session ID.
   */
  default void onAudioSessionIdChanged(int audioSessionId) {}

  /** Dispatches events to an {@link AudioRendererEventListener}. */
  final class EventDispatcher {

    @Nullable private final Handler handler;
    @Nullable private final AudioRendererEventListener listener;

    /**
     * @param handler A handler for dispatching events, or null if events should not be dispatched.
     * @param listener The listener to which events should be dispatched, or null if events should
     *     not be dispatched.
     */
    public EventDispatcher(
        @Nullable Handler handler, @Nullable AudioRendererEventListener listener) {
      this.handler = listener != null ? Assertions.checkNotNull(handler) : null;
      this.listener = listener;
    }

    /** Invokes {@link AudioRendererEventListener#onAudioEnabled(DecoderCounters)}. */
    public void enabled(DecoderCounters decoderCounters) {
      if (handler != null) {
        handler.post(() -> castNonNull(listener).onAudioEnabled(decoderCounters));
      }
    }

    /** Invokes {@link AudioRendererEventListener#onAudioDecoderInitialized(String, long, long)}. */
    public void decoderInitialized(
        String decoderName, long initializedTimestampMs, long initializationDurationMs) {
      if (handler != null) {
        handler.post(
            () ->
                castNonNull(listener)
                    .onAudioDecoderInitialized(
                        decoderName, initializedTimestampMs, initializationDurationMs));
      }
    }

    /**
     * Invokes {@link AudioRendererEventListener#onAudioInputFormatChanged(Format,
     * DecoderReuseEvaluation)}.
     */
    public void inputFormatChanged(
        Format format, @Nullable DecoderReuseEvaluation decoderReuseEvaluation) {
      if (handler != null) {
        handler.post(
            () -> castNonNull(listener).onAudioInputFormatChanged(format, decoderReuseEvaluation));
      }
    }

    /** Invokes {@link AudioRendererEventListener#onAudioPositionAdvancing(long)}. */
    public void positionAdvancing(long playoutStartSystemTimeMs) {
      if (handler != null) {
        handler.post(
            () -> castNonNull(listener).onAudioPositionAdvancing(playoutStartSystemTimeMs));
      }
    }

    /** Invokes {@link AudioRendererEventListener#onAudioUnderrun(int, long, long)}. */
    public void underrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {
      if (handler != null) {
        handler.post(
            () ->
                castNonNull(listener)
                    .onAudioUnderrun(bufferSize, bufferSizeMs, elapsedSinceLastFeedMs));
      }
    }

    /** Invokes {@link AudioRendererEventListener#onAudioDecoderReleased(String)}. */
    public void decoderReleased(String decoderName) {
      if (handler != null) {
        handler.post(() -> castNonNull(listener).onAudioDecoderReleased(decoderName));
      }
    }

    /** Invokes {@link AudioRendererEventListener#onAudioDisabled(DecoderCounters)}. */
    public void disabled(DecoderCounters counters) {
      counters.ensureUpdated();
      if (handler != null) {
        handler.post(
            () -> {
              counters.ensureUpdated();
              castNonNull(listener).onAudioDisabled(counters);
            });
      }
    }

    /** Invokes {@link AudioRendererEventListener#onSkipSilenceEnabledChanged(boolean)}. */
    public void skipSilenceEnabledChanged(boolean skipSilenceEnabled) {
      if (handler != null) {
        handler.post(() -> castNonNull(listener).onSkipSilenceEnabledChanged(skipSilenceEnabled));
      }
    }

    /** Invokes {@link AudioRendererEventListener#onAudioSinkError(Exception)}. */
    public void audioSinkError(Exception audioSinkError) {
      if (handler != null) {
        handler.post(() -> castNonNull(listener).onAudioSinkError(audioSinkError));
      }
    }

    /** Invokes {@link AudioRendererEventListener#onAudioCodecError(Exception)}. */
    public void audioCodecError(Exception audioCodecError) {
      if (handler != null) {
        handler.post(() -> castNonNull(listener).onAudioCodecError(audioCodecError));
      }
    }

    /** Invokes {@link AudioRendererEventListener#onAudioTrackInitialized}. */
    public void audioTrackInitialized(AudioSink.AudioTrackConfig audioTrackConfig) {
      if (handler != null) {
        handler.post(() -> castNonNull(listener).onAudioTrackInitialized(audioTrackConfig));
      }
    }

    /** Invokes {@link AudioRendererEventListener#onAudioTrackReleased}. */
    public void audioTrackReleased(AudioSink.AudioTrackConfig audioTrackConfig) {
      if (handler != null) {
        handler.post(() -> castNonNull(listener).onAudioTrackReleased(audioTrackConfig));
      }
    }

    /** Invokes {@link AudioRendererEventListener#onAudioSessionIdChanged}. */
    public void audioSessionIdChanged(int audioSessionId) {
      if (handler != null) {
        handler.post(() -> castNonNull(listener).onAudioSessionIdChanged(audioSessionId));
      }
    }
  }
}
