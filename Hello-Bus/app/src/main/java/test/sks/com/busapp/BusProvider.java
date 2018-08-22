package test.sks.com.busapp;

import com.squareup.otto.Bus;

/**
 * Created by acornjello on 2017-09-05.
 */

public final class BusProvider {
    private static final Bus BUS = new Bus();

    public static Bus getInstance() {
        return BUS;
    }

    private BusProvider() {
        // No instances.
    }
}
