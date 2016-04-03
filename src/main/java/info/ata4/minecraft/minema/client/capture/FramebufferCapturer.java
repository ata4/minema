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

	private static final int BPP = 3; // bytes per pixel
	private static final int TYPE = GL_UNSIGNED_BYTE;
	private static final Minecraft MC = Minecraft.getMinecraft();

	private final ByteBuffer bb;
	private final Dimension dim;
	private final byte[] line1;
	private final byte[] line2;
	private boolean flipColors = false;
	private boolean flipLines = false;

	public FramebufferCapturer() {
		this.dim = getCurrentDimension();
		this.bb = ByteBuffer.allocateDirect(this.dim.getWidth() * this.dim.getHeight() * BPP);
		this.line1 = new byte[MC.displayWidth * BPP];
		this.line2 = new byte[MC.displayWidth * BPP];
	}

	public void setFlipColors(final boolean flipColors) {
		this.flipColors = flipColors;
	}

	public boolean isFlipColors() {
		return this.flipColors;
	}

	public void setFlipLines(final boolean flipLines) {
		this.flipLines = flipLines;
	}

	public boolean isFlipLines() {
		return this.flipLines;
	}

	public int getBytesPerPixel() {
		return BPP;
	}

	public ByteBuffer getByteBuffer() {
		this.bb.rewind();
		return this.bb.duplicate();
	}

	public Dimension getCaptureDimension() {
		return this.dim;
	}

	private Dimension getCurrentDimension() {
		return new Dimension(MC.displayWidth, MC.displayHeight);
	}

	public void capture() {
		// check if the dimensions are still the same
		final Dimension dim1 = getCurrentDimension();
		final Dimension dim2 = getCaptureDimension();
		if (!dim1.equals(dim2)) {
			throw new IllegalStateException(String.format("Display size changed! %dx%d != %dx%d", dim1.getWidth(),
					dim1.getHeight(), dim2.getWidth(), dim2.getHeight()));
		}

		// set alignment flags
		glPixelStorei(GL_PACK_ALIGNMENT, 1);
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

		final int format = this.flipColors ? GL_BGR : GL_RGB;

		// read texture from framebuffer if enabled, otherwise use slower
		// glReadPixels
		if (OpenGlHelper.isFramebufferEnabled()) {
			final Framebuffer fb = MC.getFramebuffer();
			glBindTexture(GL_TEXTURE_2D, fb.framebufferTexture);
			glGetTexImage(GL_TEXTURE_2D, 0, format, TYPE, this.bb);
		} else {
			glReadPixels(0, 0, MC.displayWidth, MC.displayHeight, format, TYPE, this.bb);
		}

		if (!this.flipLines) {
			return;
		}

		// flip buffer vertically
		for (int i = 0; i < MC.displayHeight / 2; i++) {
			final int ofs1 = i * MC.displayWidth * BPP;
			final int ofs2 = (MC.displayHeight - i - 1) * MC.displayWidth * BPP;

			// read lines
			this.bb.position(ofs1);
			this.bb.get(this.line1);
			this.bb.position(ofs2);
			this.bb.get(this.line2);

			// write lines at swapped positions
			this.bb.position(ofs2);
			this.bb.put(this.line1);
			this.bb.position(ofs1);
			this.bb.put(this.line2);
		}
	}
}
