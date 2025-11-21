package com.onlineshop.task;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class OnlineShopApplication implements QuarkusApplication {
    public static void main(String[] args) {
        Quarkus.run(OnlineShopApplication.class, args);
    }

    /**
     * Keep the Quarkus runtime running until it is shut down externally.
     */
    @Override
    public int run(String... args) throws Exception {
        Quarkus.waitForExit();
        return 0;
    }
}