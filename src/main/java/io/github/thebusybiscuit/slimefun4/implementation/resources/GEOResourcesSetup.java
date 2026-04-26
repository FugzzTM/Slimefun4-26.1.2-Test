package io.github.thebusybiscuit.slimefun4.implementation.resources;

import java.util.logging.Level;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.api.geo.GEOResource;

/**
 * This static setup class is used to register all default instances of
 * {@link GEOResource} that Slimefun includes out of the box.
 * 
 * @author TheBusyBiscuit
 *
 */
public final class GEOResourcesSetup {

    private GEOResourcesSetup() {}

    public static void setup() {
        registerSafe(() -> new OilResource().register(), "oil");
        registerSafe(() -> new NetherIceResource().register(), "nether ice");
        registerSafe(() -> new UraniumResource().register(), "uranium");
        registerSafe(() -> new SaltResource().register(), "salt");
    }

    private static void registerSafe(Runnable task, String resourceName) {
        try {
            task.run();
        } catch (IllegalArgumentException e) {
            // Re-throw programming errors like double-registration
            throw e;
        } catch (LinkageError | Exception e) {
            Slimefun.logger().log(Level.WARNING, "Skipping GEO resource \"{0}\" because it could not be initialized.", resourceName);
            Slimefun.logger().log(Level.WARNING, e, () -> "Failed to register GEO resource \"" + resourceName + "\"");
        }
    }

}
