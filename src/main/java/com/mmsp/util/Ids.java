package com.mmsp.util;

import java.util.UUID;

public final class Ids {

    private Ids() {
    }

    public static String shortId(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
