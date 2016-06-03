/*
 ** 2014 July 29
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.minema.client.event;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.lwjgl.util.Dimension;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Cancelable
public class FramePreCaptureEvent extends Event {

    public final int frameNum;
    public final Dimension frameDim;

    public FramePreCaptureEvent(int frameNum, Dimension frameDim) {
        super();
        
        this.frameNum = frameNum;
        this.frameDim = frameDim;
    }

}
