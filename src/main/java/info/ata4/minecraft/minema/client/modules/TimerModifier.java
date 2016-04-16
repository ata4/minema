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
import info.ata4.minecraft.minema.client.engine.FixedTimer;
import info.ata4.minecraft.minema.util.reflection.PrivateFields;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TimerModifier extends ACaptureModule {

	private static final Logger L = LogManager.getLogger();
	private static final Minecraft MC = Minecraft.getMinecraft();

	private float defaultTps;

	public TimerModifier(final MinemaConfig cfg) {
		super(cfg);
	}

	@Override
	protected void doEnable() {
		final Timer defaultTimer = getTimer();

		// check if it's modified already
		if (defaultTimer instanceof FixedTimer) {
			L.warn("Timer is already modified!");
			return;
		}

		// get default ticks per second if possible
		if (defaultTimer != null) {
			this.defaultTps = getTicksPerSecond(defaultTimer);
		}

		final float fps = this.cfg.frameRate.get().floatValue();
		final float speed = this.cfg.engineSpeed.get().floatValue();

		// set fixed delay timer
		setTimer(new FixedTimer(this.defaultTps, fps, speed));
	}

	@Override
	protected void doDisable() {
		// check if it's still modified
		if (!(getTimer() instanceof FixedTimer)) {
			L.warn("Timer is already restored!");
			return;
		}

		// restore default timer
		setTimer(new Timer(this.defaultTps));
	}

	private Timer getTimer() {
		try {
			return ReflectionHelper.getPrivateValue(Minecraft.class, MC, PrivateFields.MINECRAFT_TIMER);
		} catch (final Exception ex) {
			throw new RuntimeException("Can't get timer", ex);
		}
	}

	private void setTimer(final Timer timer) {
		try {
			ReflectionHelper.setPrivateValue(Minecraft.class, MC, timer, PrivateFields.MINECRAFT_TIMER);
		} catch (final Exception ex) {
			throw new RuntimeException("Can't set timer", ex);
		}
	}

	private float getTicksPerSecond(final Timer timer) {
		try {
			return ReflectionHelper.getPrivateValue(Timer.class, timer, PrivateFields.TIMER_TICKSPERSECOND);
		} catch (final Exception ex) {
			L.warn("Can't get default ticks per second", ex);
			// hard-coded default
			return 20;
		}
	}
}
