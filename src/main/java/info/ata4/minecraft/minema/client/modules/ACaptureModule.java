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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import info.ata4.minecraft.minema.client.config.MinemaConfig;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class ACaptureModule {

	private static final Logger L = LogManager.getLogger();

	protected final MinemaConfig cfg;
	private boolean enabled;

	public ACaptureModule(final MinemaConfig cfg) {
		this.cfg = cfg;
	}

	public String getName() {
		return getClass().getSimpleName();
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public synchronized final void enable() {
		if (this.enabled) {
			return;
		}

		this.enabled = true;

		L.info("Enabling " + getName());

		try {
			doEnable();
		} catch (final Throwable t) {
			L.error("Can't enable " + getName(), t);
			handleError(t);
			disable();
		}
	}

	public synchronized final void disable() {
		if (!this.enabled) {
			return;
		}

		this.enabled = false;

		L.info("Disabling " + getName());

		try {
			doDisable();
		} catch (final Throwable t) {
			L.error("Can't disable " + getName(), t);
			handleError(t);
		}
	}

	protected void handleError(final Throwable t) {
		throw new RuntimeException(t);
	}

	protected abstract void doEnable() throws Exception;

	protected abstract void doDisable() throws Exception;
}
