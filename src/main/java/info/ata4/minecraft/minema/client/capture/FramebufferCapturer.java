/*
 ** 2014 July 28
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.minema.client.capture;

import static org.lwjgl.opengl.GL11.GL_PACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGetTexImage;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glReadPixels;
import static org.lwjgl.opengl.GL12.GL_BGR;

import java.nio.ByteBuffer;

import org.lwjgl.util.Dimension;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FramebufferCapturer {

	private static final int bytesPerPixel = 3;
	private static final int TYPE = GL_UNSIGNED_BYTE;
	private static final Minecraft MC = Minecraft.getMinecraft();

	private final ByteBuffer bb;
	private final Dimension start;

	private byte[] flipLine1 = null;
	private byte[] flipLine2 = null;
	private int colorFormat = GL_RGB;

	public FramebufferCapturer() {
		this.start = new Dimension(MC.displayWidth, MC.displayHeight);
		this.bb = ByteBuffer.allocateDirect(this.start.getWidth() * this.start.getHeight() * bytesPerPixel);
	}

	public void setFlipColors() {
		colorFormat = GL_BGR;
	}

	public void setFlipLines() {
		flipLine1 = new byte[start.getWidth() * bytesPerPixel];
		flipLine2 = new byte[start.getWidth() * bytesPerPixel];
	}

	public ByteBuffer getByteBuffer() {
		// Rewinding to pos 0 (after capture)
		this.bb.rewind();
		// ByteBuffer was once duplicated right here -> resulting in heavy
		// memory allocation (eg. 1280*720*3 bytes, which is about 2.8 MB per
		// frame and about 1.7 GB for 10 seconds in 60fps)
		// I know that this is not good practice, but it does not matter and
		// optimizes a lot
		return this.bb;
	}

	public Dimension getCaptureDimension() {
		return this.start;
	}

	public void capture() {
		// check if the dimensions are still the same
		if (MC.displayWidth != start.getWidth() || MC.displayHeight != start.getHeight()) {
			throw new IllegalStateException(
					String.format("Display size changed! %dx%d not equals the start dimension of %dx%d",
							MC.displayWidth, MC.displayHeight, start.getWidth(), start.getHeight()));
		}

		// set alignment flags
		glPixelStorei(GL_PACK_ALIGNMENT, 1);
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

		// Rewinding to pos 0 (after writting bytebuffer via FrameExporter)
		this.bb.rewind();

		// read texture from framebuffer if enabled, otherwise use slower
		// glReadPixels
		if (OpenGlHelper.isFramebufferEnabled()) {
			final Framebuffer fb = MC.getFramebuffer();
			glBindTexture(GL_TEXTURE_2D, fb.framebufferTexture);
			glGetTexImage(GL_TEXTURE_2D, 0, colorFormat, TYPE, this.bb);
		} else {
			glReadPixels(0, 0, MC.displayWidth, MC.displayHeight, colorFormat, TYPE, this.bb);
		}

		if (flipLine1 == null || flipLine2 == null) {
			return;
		}

		// flip buffer vertically
		for (int i = 0; i < MC.displayHeight / 2; i++) {
			final int ofs1 = i * MC.displayWidth * bytesPerPixel;
			final int ofs2 = (MC.displayHeight - i - 1) * MC.displayWidth * bytesPerPixel;

			// read lines
			this.bb.position(ofs1);
			this.bb.get(this.flipLine1);
			this.bb.position(ofs2);
			this.bb.get(this.flipLine2);

			// write lines at swapped positions
			this.bb.position(ofs2);
			this.bb.put(this.flipLine1);
			this.bb.position(ofs1);
			this.bb.put(this.flipLine2);
		}
	}
}
