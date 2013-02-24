/**
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.informant.local.store;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import io.informant.config.ConfigService;
import io.informant.config.GeneralConfig;
import io.informant.core.Trace;
import io.informant.util.Singleton;

/**
 * @author Trask Stalnaker
 * @since 0.5
 */
@Singleton
public class TraceSnapshotService {

    private final ConfigService configService;

    TraceSnapshotService(ConfigService configService) {
        this.configService = configService;
    }

    public boolean shouldStore(Trace trace) {
        if (trace.isStuck() || trace.isError()) {
            return true;
        }
        long duration = trace.getDuration();
        // check if should store for user tracing
        String userId = trace.getUserId();
        if (userId != null && userId.equals(configService.getUserConfig().getUserId())
                && duration >= MILLISECONDS.toNanos(configService.getUserConfig()
                        .getStoreThresholdMillis())) {
            return true;
        }
        // check if should store for fine profiling
        if (trace.isFine()) {
            int fineStoreThresholdMillis = configService.getFineProfilingConfig()
                    .getStoreThresholdMillis();
            if (fineStoreThresholdMillis != GeneralConfig.STORE_THRESHOLD_DISABLED) {
                return trace.getDuration() >= MILLISECONDS.toNanos(fineStoreThresholdMillis);
            }
        }
        // fall back to core store threshold
        return shouldStoreBasedOnCoreStoreThreshold(trace);
    }

    private boolean shouldStoreBasedOnCoreStoreThreshold(Trace trace) {
        int storeThresholdMillis = configService.getGeneralConfig().getStoreThresholdMillis();
        return storeThresholdMillis != GeneralConfig.STORE_THRESHOLD_DISABLED
                && trace.getDuration() >= MILLISECONDS.toNanos(storeThresholdMillis);
    }
}
