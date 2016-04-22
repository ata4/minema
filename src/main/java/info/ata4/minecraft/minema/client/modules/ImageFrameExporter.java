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

	public ImageFrameExporter(final MinemaConfig cfg) {
		super(cfg);
	}

	@Override
	public void configureCapturer(final ACapturer fbc) {
		if (this.cfg.imageFormat.get().equals("tga")) {
			fbc.setFlipColors();
		} else {
			fbc.setFlipLines();
		}
	}

	@Override
	protected void doExportFrame(final FrameCaptureEvent evt) throws IOException {
		final String format = this.cfg.imageFormat.get();
		final String fileName = String.format("%06d.%s", evt.frameNum, format);
		final File file = new File(this.cfg.getMovieDir(), fileName);
		writeImage(file, evt.frameBuffer, evt.frameDim.getWidth(), evt.frameDim.getHeight(), format);
	}

	private void writeImage(final File file, final ByteBuffer bb, final int width, final int height,
			final String format) throws IOException {
		// use direct frame writer for Targa
		if (format.equals("tga")) {
			final byte[] tgaHeader = new byte[18];
			tgaHeader[2] = 2; // image type - uncompressed true-color image
			tgaHeader[12] = (byte) (width % 256);
			tgaHeader[13] = (byte) (width / 256);
			tgaHeader[14] = (byte) (height % 256);
			tgaHeader[15] = (byte) (height / 256);
			tgaHeader[16] = 24; // bits per pixel

			FileOutputStream os = null;

			try {
				os = new FileOutputStream(file);
				final FileChannel channel = os.getChannel();
				channel.write(ByteBuffer.wrap(tgaHeader));
				channel.write(bb);
			} finally {
				IOUtils.closeQuietly(os);
			}
		} else {
			final int[] imageData = new int[width * height];
			final byte[] pixel = new byte[3];
			final int pixels = bb.capacity() / pixel.length;

			// convert RGB byte array to integer array
			for (int i = 0; i < pixels; i++) {
				bb.get(pixel);
				imageData[i] = (pixel[0] << 16) + (pixel[1] << 8) + pixel[2];
			}

			final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			image.setRGB(0, 0, width, height, imageData, 0, width);

			if (!ImageIO.write(image, format, file)) {
				throw new RuntimeException("No appropriate image writer found for format " + format);
			}
		}
	}
}
