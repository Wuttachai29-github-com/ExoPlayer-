/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.exoplayer2.source;

import android.net.Uri;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ParserException;
import com.google.android.exoplayer2.extractor.Extractor;
import com.google.android.exoplayer2.extractor.SniffFailure;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.InlineMe;
import java.util.List;

/**
 * Thrown if the input format was not recognized.
 *
 * @deprecated com.google.android.exoplayer2 is deprecated. Please migrate to androidx.media3 (which
 *     contains the same ExoPlayer code). See <a
 *     href="https://developer.android.com/guide/topics/media/media3/getting-started/migration-guide">the
 *     migration guide</a> for more details, including a script to help with the migration.
 */
@Deprecated
public class UnrecognizedInputFormatException extends ParserException {

  /** The {@link Uri} from which the unrecognized data was read. */
  public final Uri uri;

  /**
   * Sniff failures from {@link Extractor#getSniffFailureDetails()} from any extractors that were
   * checked while trying to recognize the input data.
   *
   * <p>May be empty if no extractors provided additional sniffing failure details.
   */
  public final ImmutableList<SniffFailure> sniffFailures;

  /**
   * @deprecated Use {@link #UnrecognizedInputFormatException(String, Uri, List)} instead.
   */
  @InlineMe(
      replacement = "this(message, uri, ImmutableList.of())",
      imports = "com.google.common.collect.ImmutableList")
  @Deprecated
  public UnrecognizedInputFormatException(String message, Uri uri) {
    this(message, uri, ImmutableList.of());
  }

  /**
   * Constructs a new instance.
   *
   * @param message The detail message for the exception.
   * @param uri The {@link Uri} from which the unrecognized data was read.
   * @param sniffFailures Sniff failures from any extractors that were used to sniff the data while
   *     trying to recognize it.
   */
  public UnrecognizedInputFormatException(
      String message, Uri uri, List<? extends SniffFailure> sniffFailures) {
    super(message, /* cause= */ null, /* contentIsMalformed= */ false, C.DATA_TYPE_MEDIA);
    this.uri = uri;
    this.sniffFailures = ImmutableList.copyOf(sniffFailures);
  }
}
