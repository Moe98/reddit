package org.sab.chat.storage.tables;

import com.datastax.driver.core.Row;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

public class TableUtils {
    public static boolean isEmpty(List<Row> rows) {
        return rows == null || rows.size() == 0;
    }

    private static final long NUM_HUNDRED_NANOS_IN_A_SECOND = 10_000_000L;

    private static final long NUM_HUNDRED_NANOS_FROM_UUID_EPOCH_TO_UNIX_EPOCH = 122_192_928_000_000_000L;

    public static Instant getInstantFromUUID(final UUID uuid) {
        final long hundredNanosSinceUnixEpoch = uuid.timestamp() - NUM_HUNDRED_NANOS_FROM_UUID_EPOCH_TO_UNIX_EPOCH;
        final long secondsSinceUnixEpoch = hundredNanosSinceUnixEpoch / NUM_HUNDRED_NANOS_IN_A_SECOND;
        final long nanoAdjustment = ((hundredNanosSinceUnixEpoch % NUM_HUNDRED_NANOS_IN_A_SECOND) * 100);
        return Instant.ofEpochSecond(secondsSinceUnixEpoch, nanoAdjustment);
    }
}
