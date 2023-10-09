/*
 * Copyright 2023 The Android Open Source Project
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
package com.google.android.exoplayer2.video;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.util.DebugViewProvider;
import com.google.android.exoplayer2.util.Effect;
import com.google.android.exoplayer2.util.VideoFrameProcessor;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.Executor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

/** Unit test for {@link CompositingVideoSinkProvider}. */
@RunWith(AndroidJUnit4.class)
public final class CompositingVideoSinkProviderTest {

  @Test
  public void initialize() throws VideoSink.VideoSinkException {
    CompositingVideoSinkProvider provider = createCompositingVideoSinkProvider();
    provider.setVideoEffects(ImmutableList.of());

    provider.initialize(new Format.Builder().build());

    assertThat(provider.isInitialized()).isTrue();
  }

  @Test
  public void initialize_withoutEffects_throws() {
    CompositingVideoSinkProvider provider = createCompositingVideoSinkProvider();

    assertThrows(
        IllegalStateException.class,
        () -> provider.initialize(new Format.Builder().setWidth(640).setHeight(480).build()));
  }

  @Test
  public void initialize_calledTwice_throws() throws VideoSink.VideoSinkException {
    CompositingVideoSinkProvider provider = createCompositingVideoSinkProvider();
    provider.setVideoEffects(ImmutableList.of());
    provider.initialize(new Format.Builder().build());

    assertThrows(
        IllegalStateException.class, () -> provider.initialize(new Format.Builder().build()));
  }

  @Test
  public void initialize_afterRelease_throws() throws VideoSink.VideoSinkException {
    CompositingVideoSinkProvider provider = createCompositingVideoSinkProvider();
    provider.setVideoEffects(ImmutableList.of());
    Format format = new Format.Builder().build();

    provider.initialize(format);
    provider.release();

    assertThrows(IllegalStateException.class, () -> provider.initialize(format));
  }

  @Test
  public void registerInputStream_withInputTypeBitmap_throws() throws VideoSink.VideoSinkException {
    CompositingVideoSinkProvider provider = createCompositingVideoSinkProvider();
    provider.setVideoEffects(ImmutableList.of());
    provider.initialize(new Format.Builder().build());
    VideoSink videoSink = provider.getSink();

    assertThrows(
        UnsupportedOperationException.class,
        () ->
            videoSink.registerInputStream(
                VideoSink.INPUT_TYPE_BITMAP, new Format.Builder().build()));
  }

  @Test
  public void setOutputStreamOffsetUs_frameReleaseTimesAreAdjusted()
      throws VideoSink.VideoSinkException {
    CompositingVideoSinkProvider provider = createCompositingVideoSinkProvider();
    provider.setVideoEffects(ImmutableList.of());
    provider.initialize(new Format.Builder().build());
    VideoSink videoSink = provider.getSink();
    videoSink.registerInputStream(
        VideoSink.INPUT_TYPE_SURFACE, new Format.Builder().setWidth(640).setHeight(480).build());

    assertThat(videoSink.registerInputFrame(/* framePresentationTimeUs= */ 0, false)).isEqualTo(0);
    provider.setStreamOffsetUs(1_000);
    assertThat(videoSink.registerInputFrame(/* framePresentationTimeUs= */ 0, false))
        .isEqualTo(1_000_000);
    provider.setStreamOffsetUs(2_000);
    assertThat(videoSink.registerInputFrame(/* framePresentationTimeUs= */ 0, false))
        .isEqualTo(2_000_000);
  }

  @Test
  public void setListener_calledTwiceWithDifferentExecutor_throws()
      throws VideoSink.VideoSinkException {
    CompositingVideoSinkProvider provider = createCompositingVideoSinkProvider();
    provider.setVideoEffects(ImmutableList.of());
    provider.initialize(new Format.Builder().build());
    VideoSink videoSink = provider.getSink();
    VideoSink.Listener listener = Mockito.mock(VideoSink.Listener.class);

    videoSink.setListener(listener, /* executor= */ command -> {});

    assertThrows(
        IllegalStateException.class,
        () -> videoSink.setListener(listener, /* executor= */ command -> {}));
  }

  private static CompositingVideoSinkProvider createCompositingVideoSinkProvider() {
    VideoSink.RenderControl renderControl = new TestRenderControl();
    return new CompositingVideoSinkProvider(
        ApplicationProvider.getApplicationContext(),
        new TestPreviewingVideoGraphFactory(),
        renderControl);
  }

  private static class TestPreviewingVideoGraphFactory implements PreviewingVideoGraph.Factory {
    // Using a mock but we don't assert mock interactions. If needed to assert interactions, we
    // should a fake instead.
    private final PreviewingVideoGraph previewingVideoGraph =
        Mockito.mock(PreviewingVideoGraph.class);
    private final VideoFrameProcessor videoFrameProcessor = Mockito.mock(VideoFrameProcessor.class);

    @Override
    public PreviewingVideoGraph create(
        Context context,
        ColorInfo inputColorInfo,
        ColorInfo outputColorInfo,
        DebugViewProvider debugViewProvider,
        VideoGraph.Listener listener,
        Executor listenerExecutor,
        List<Effect> compositionEffects,
        long initialTimestampOffsetUs) {
      when(previewingVideoGraph.getProcessor(anyInt())).thenReturn(videoFrameProcessor);
      when(videoFrameProcessor.registerInputFrame()).thenReturn(true);
      return previewingVideoGraph;
    }
  }

  private static class TestRenderControl implements VideoSink.RenderControl {

    @Override
    public long getFrameRenderTimeNs(
        long presentationTimeUs, long positionUs, long elapsedRealtimeUs, float playbackSpeed) {
      return presentationTimeUs;
    }

    @Override
    public void onNextFrame(long presentationTimeUs) {}

    @Override
    public void onFrameRendered() {}

    @Override
    public void onFrameDropped() {}
  }
}
