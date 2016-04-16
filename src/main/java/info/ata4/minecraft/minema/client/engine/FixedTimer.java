/*
 ** 2012 January 3
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */

package info.ata4.minecraft.minema.client.engine;

import net.minecraft.util.Timer;

/**
 * Extension of Minecraft's default timer for fixed framerate rendering.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FixedTimer extends Timer {

	private final float ticksPerSecond;
	private final float framesPerSecond;

	public FixedTimer(final float tps, final float fps, final float speed) {
		super(tps);
		this.ticksPerSecond = tps;
		this.framesPerSecond = fps;
		this.timerSpeed = speed;
	}

	@Override
	public void updateTimer() {
		this.elapsedPartialTicks += this.timerSpeed * (this.ticksPerSecond / this.framesPerSecond);
		this.elapsedTicks = (int) this.elapsedPartialTicks;
		this.elapsedPartialTicks -= this.elapsedTicks;
		this.renderPartialTicks = this.elapsedPartialTicks;
	}
}
