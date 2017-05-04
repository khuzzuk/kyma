package net.kyma.player;

import com.google.inject.AbstractModule;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ExecutorService.class).toInstance(Executors.newFixedThreadPool(1));
    }
}
