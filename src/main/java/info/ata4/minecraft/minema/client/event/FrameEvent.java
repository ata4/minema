/*
** 2016 June 03
**
** The author disclaims copyright to this source code. In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.minema.client.event;

import info.ata4.minecraft.minema.client.util.CaptureFrame;
import info.ata4.minecraft.minema.client.util.CaptureTime;
import java.nio.file.Path;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Cancelable
public abstract class FrameEvent extends Event  {

    public final CaptureFrame frame;
    public final CaptureTime time;
    public final Path movieDir;

    public FrameEvent(CaptureFrame frame, CaptureTime time, Path movieDir) {
        this.frame = frame;
        this.time = time;
        this.movieDir = movieDir;
    }
}
