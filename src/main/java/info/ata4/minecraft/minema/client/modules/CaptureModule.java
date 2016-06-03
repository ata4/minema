/*
 ** 2014 July 28
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.minema.client.modules;

import info.ata4.minecraft.minema.client.config.MinemaConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class CaptureModule {

    private static final Logger L = LogManager.getLogger();

    protected MinemaConfig cfg;
    private boolean enabled;

    public CaptureModule(MinemaConfig cfg) {
        this.cfg = cfg;
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public synchronized void enable() {
        if (enabled) {
            return;
        }

        enabled = true;

        L.info("Enabling " + getName());

        try {
            doEnable();
        } catch (Exception ex) {
            L.error("Can't enable " + getName(), ex);
            handleError(ex);
            disable();
        }
    }

    public synchronized void disable() {
        if (!enabled) {
            return;
        }

        enabled = false;

        L.info("Disabling " + getName());

        try {
            doDisable();
        } catch (Exception ex) {
            L.error("Can't disable " + getName(), ex);
            handleError(ex);
        }
    }

    protected void handleError(Throwable t) {
        throw new RuntimeException(t);
    }

    protected abstract void doEnable() throws Exception;

    protected abstract void doDisable() throws Exception;
}
