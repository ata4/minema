/*
 ** 2014 July 30
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.minema.client.modules;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;

import info.ata4.minecraft.minema.client.capture.ACapturer;
import info.ata4.minecraft.minema.client.config.MinemaConfig;
import info.ata4.minecraft.minema.client.event.FrameCaptureEvent;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ImageFrameExporter extends FrameExporter {

    public ImageFrameExporter(MinemaConfig cfg) {
        super(cfg);
    }

    @Override
    public void configureCapturer(ACapturer fbc) {
        if (cfg.imageFormat.get().equals("tga")) {
            return;
        }
        fbc.setFlipLines();
        fbc.setToRGBMode();
    }

    @Override
    protected void doExportFrame(FrameCaptureEvent evt) throws IOException {
        String format = cfg.imageFormat.get();
        String fileName = String.format("%06d.%s", evt.frameNum, format);
        File file = new File(cfg.getMovieDir(), fileName);
        writeImage(file, evt.frameBuffer, evt.frameDim.getWidth(), evt.frameDim.getHeight(), format);
    }

    private void writeImage(File file, ByteBuffer bb, int width, int height,
            String format) throws IOException {
        // use direct frame writer for Targa
        if (format.equals("tga")) {
            byte[] tgaHeader = new byte[18];
            tgaHeader[2] = 2; // image type - uncompressed true-color image
            tgaHeader[12] = (byte) (width % 256);
            tgaHeader[13] = (byte) (width / 256);
            tgaHeader[14] = (byte) (height % 256);
            tgaHeader[15] = (byte) (height / 256);
            tgaHeader[16] = 24; // bits per pixel

            FileOutputStream os = null;

            try {
                os = new FileOutputStream(file);
                FileChannel channel = os.getChannel();
                channel.write(ByteBuffer.wrap(tgaHeader));
                channel.write(bb);
            } finally {
                IOUtils.closeQuietly(os);
            }
        } else {
            int[] imageData = new int[width * height];
            byte[] pixel = new byte[3];
            int pixels = bb.capacity() / pixel.length;

            // convert RGB byte array to integer array
            for (int i = 0; i < pixels; i++) {
                bb.get(pixel);
                imageData[i] = (pixel[0] << 16) + (pixel[1] << 8) + pixel[2];
            }

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            image.setRGB(0, 0, width, height, imageData, 0, width);

            if (!ImageIO.write(image, format, file)) {
                throw new RuntimeException("No appropriate image writer found for format " + format);
            }
        }
    }
}
