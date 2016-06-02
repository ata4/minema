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

    public FixedTimer(float tps, float fps, float speed) {
        super(tps);
        ticksPerSecond = tps;
        framesPerSecond = fps;
        timerSpeed = speed;
    }

    @Override
    public void updateTimer() {
        elapsedPartialTicks += timerSpeed * (ticksPerSecond / framesPerSecond);
        elapsedTicks = (int) elapsedPartialTicks;
        elapsedPartialTicks -= elapsedTicks;
        renderPartialTicks = elapsedPartialTicks;
    }
}
