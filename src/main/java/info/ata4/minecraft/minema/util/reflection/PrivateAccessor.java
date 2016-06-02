/*
 ** 2014 July 28
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.minema.util.reflection;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public interface PrivateAccessor {

    static final String[] TIMER_TICKSPERSECOND = new String[] { "ticksPerSecond", "field_74282_a" };
    static final String[] MINECRAFT_TIMER = new String[] { "timer", "field_71428_T" };

    default Timer minecraftGetTimer(Minecraft mc) {
        try {
            return ReflectionHelper.getPrivateValue(Minecraft.class, mc, PrivateAccessor.MINECRAFT_TIMER);
        } catch (Exception ex) {
            throw new RuntimeException("Can't get timer", ex);
        }
    }

    default void minecraftSetTimer(Minecraft mc, Timer timer) {
        try {
            ReflectionHelper.setPrivateValue(Minecraft.class, mc, timer, PrivateAccessor.MINECRAFT_TIMER);
        } catch (Exception ex) {
            throw new RuntimeException("Can't set timer", ex);
        }
    }

    default float timerGetTicksPerSecond(Timer timer) {
        try {
            return ReflectionHelper.getPrivateValue(Timer.class, timer, PrivateAccessor.TIMER_TICKSPERSECOND);
        } catch (Exception ex) {
            // hard-coded default
            return 20;
        }
    }    
}
