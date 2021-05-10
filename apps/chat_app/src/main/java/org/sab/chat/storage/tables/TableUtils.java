package org.sab.chat.storage.tables;

import com.datastax.driver.core.Row;

import java.util.List;

public class TableUtils {
    public static boolean isEmpty(List<Row> rows) {
        return rows == null || rows.size() == 0;
    }
}
