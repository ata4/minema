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

import java.nio.ByteBuffer;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import org.lwjgl.util.Dimension;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Cancelable
public class FrameCaptureEvent extends FramePreCaptureEvent {
    
    public final ByteBuffer frameBuffer;

    public FrameCaptureEvent(int frameNum, Dimension frameDim, ByteBuffer frameBuffer) {
        super(frameNum, frameDim);
        this.frameBuffer = frameBuffer;
    }
}
