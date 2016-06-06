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

import info.ata4.minecraft.minema.Minema;
import info.ata4.minecraft.minema.client.config.MinemaConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class CaptureModule {

    private static final Logger L = LogManager.getLogger();

    protected final MinemaConfig cfg;
    private boolean enabled;

    public CaptureModule(MinemaConfig cfg) {
        this.cfg = cfg;
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    public synchronized final boolean isEnabled() {
        return enabled;
    }

    public synchronized final void enable() {
        if (enabled) {
            return;
        }

        enabled = true;

        L.info("Enabling " + getName());
        
        Minema.EVENT_BUS.register(this);

        try {
            doEnable();
        } catch (Exception ex) {
            handleError(ex, "Can't enable %s", getName());
            disable();
        }
    }

    public synchronized final void disable() {
        if (!enabled) {
            return;
        }

        enabled = false;

        L.info("Disabling " + getName());
        
        Minema.EVENT_BUS.unregister(this);

        try {
            doDisable();
        } catch (Exception ex) {
            handleError(ex, "Can't disable %s", getName());
        }
    }
    
    protected void handleWarning(Throwable t, String message, Object... args) {
        L.warn(String.format(message, args), t);
    }

    protected void handleError(Throwable t, String message, Object... args) {
        throw new RuntimeException(String.format(message, args), t);
    }

    protected abstract void doEnable() throws Exception;

    protected abstract void doDisable() throws Exception;
}
