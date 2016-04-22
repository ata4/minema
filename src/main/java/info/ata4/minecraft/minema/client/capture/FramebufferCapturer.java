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
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGetTexImage;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glReadPixels;

import net.minecraft.client.shader.Framebuffer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FramebufferCapturer extends ACapturer {

	@Override
	protected void capture() {
		// set alignment flags
		glPixelStorei(GL_PACK_ALIGNMENT, 1);
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

		// read texture from framebuffer if enabled, otherwise use slower
		// glReadPixels
		if (isFramebufferEnabled) {
			final Framebuffer fb = MC.getFramebuffer();
			glBindTexture(GL_TEXTURE_2D, fb.framebufferTexture);
			glGetTexImage(GL_TEXTURE_2D, 0, colorFormat, TYPE, this.buffer);
		} else {
			glReadPixels(0, 0, start.getWidth(), start.getHeight(), colorFormat, TYPE, this.buffer);
		}
	}

	@Override
	public void close() {
	}
}
