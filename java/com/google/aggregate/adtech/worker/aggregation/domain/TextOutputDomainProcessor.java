/*
 * Copyright 2022 Google LLC
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

package com.google.aggregate.adtech.worker.aggregation.domain;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.aggregate.adtech.worker.Annotations.BlockingThreadPool;
import com.google.aggregate.adtech.worker.Annotations.NonBlockingThreadPool;
import com.google.aggregate.adtech.worker.exceptions.DomainReadException;
import com.google.aggregate.adtech.worker.util.NumericConversions;
import com.google.aggregate.perf.StopwatchRegistry;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.scp.operator.cpio.blobstorageclient.BlobStorageClient;
import com.google.scp.operator.cpio.blobstorageclient.BlobStorageClient.BlobStorageClientException;
import com.google.scp.operator.cpio.blobstorageclient.model.DataLocation;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.UUID;
import java.util.stream.Stream;
import javax.inject.Inject;

/** Reads output domain from a text file with each aggregation key on a separate line. */
public final class TextOutputDomainProcessor extends OutputDomainProcessor {

  private final BlobStorageClient blobStorageClient;
  private final StopwatchRegistry stopwatches;

  @Inject
  public TextOutputDomainProcessor(
      @BlockingThreadPool ListeningExecutorService blockingThreadPool,
      @NonBlockingThreadPool ListeningExecutorService nonBlockingThreadPool,
      BlobStorageClient blobStorageClient,
      StopwatchRegistry stopwatches) {
    super(
        /* blockingThreadPool= */ blockingThreadPool,
        /* nonBlockingThreadPool= */ nonBlockingThreadPool,
        /* blobStorageClient= */ blobStorageClient,
        /* stopwatches= */ stopwatches);
    this.blobStorageClient = blobStorageClient;
    this.stopwatches = stopwatches;
  }

  public ImmutableList<BigInteger> readShard(DataLocation outputDomainLocation) {
    Stopwatch stopwatch =
        stopwatches.createStopwatch(String.format("domain-shard-read-%s", UUID.randomUUID()));
    stopwatch.start();
    try (InputStream domainStream = blobStorageClient.getBlob(outputDomainLocation)) {
      byte[] bytes = ByteStreams.toByteArray(domainStream);
      try (Stream<String> fileLines = NumericConversions.createStringFromByteArray(bytes).lines()) {
        ImmutableList<BigInteger> shard =
            fileLines.map(NumericConversions::createBucketFromString).collect(toImmutableList());
        return shard;
      }
    } catch (IOException | BlobStorageClientException | IllegalArgumentException e) {
      throw new DomainReadException(e);
    } finally {
      stopwatch.stop();
    }
  }
}
