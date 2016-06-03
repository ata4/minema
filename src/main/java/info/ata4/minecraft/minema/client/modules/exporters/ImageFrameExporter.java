/*
 ** 2014 July 30
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.minema.client.modules.exporters;

import info.ata4.minecraft.minema.client.capture.Capturer;
import info.ata4.minecraft.minema.client.config.MinemaConfig;
import info.ata4.minecraft.minema.client.event.FrameCaptureEvent;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.*;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ImageFrameExporter extends FrameExporter {

    public ImageFrameExporter(MinemaConfig cfg) {
        super(cfg);
    }

    @Override
    public void configureCapturer(Capturer fbc) {
    }

    @Override
    protected void doExportFrame(FrameCaptureEvent evt) throws IOException {
        String fileName = String.format("%06d.tga", evt.frameNum);
        Path path = cfg.getMovieDir().resolve(fileName);
        writeImage(path, evt.frameBuffer, evt.frameDim.getWidth(), evt.frameDim.getHeight());
    }

    private void writeImage(Path path, ByteBuffer bb, int width, int height) throws IOException {
        ByteBuffer tgah = ByteBuffer.allocate(18);
        
        // image type - uncompressed true-color image
        tgah.position(2);
        tgah.put((byte) 2);
        
        // width and height
        tgah.position(12);
        tgah.putShort((short) (width & 0xffff));
        tgah.putShort((short) (height & 0xffff));
        
        // bits per pixel
        tgah.position(16);
        tgah.put((byte) 24);
        
        tgah.rewind();

        try (FileChannel fc = FileChannel.open(path, CREATE, WRITE)) {
            fc.write(tgah);
            fc.write(bb);
        }
    }
}
