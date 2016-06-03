/*
** 2016 June 03
**
** The author disclaims copyright to this source code. In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.minema.client.util;

import java.nio.ByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class CaptureFrame {

    private static final Minecraft MC = Minecraft.getMinecraft();
    private static final int BPP = 3;
    private static final int TYPE = GL_UNSIGNED_BYTE;
    private static final int FORMAT = GL_BGR;

    public final int width;
    public final int height;
    public final ByteBuffer buffer;

    public CaptureFrame() {
        this.width = MC.displayWidth;
        this.height = MC.displayHeight;
        this.buffer = ByteBuffer.allocateDirect(width * height * BPP);
    }

    public void checkWindowSize() {
        int displayWidth = MC.displayWidth;
        int displayHeight = MC.displayHeight;
        if (displayWidth != width || displayHeight != height) {
            throw new IllegalStateException(String.format(
                "Display size changed! %dx%d not equals the start dimension of %dx%d",
                displayWidth, displayHeight, width, height));
        }
    }

    public void readPixels(boolean fbo, boolean pbo) {
        buffer.rewind();

        // set alignment flags
        glPixelStorei(GL_PACK_ALIGNMENT, 1);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        // read texture from framebuffer if enabled, otherwise use slower
        // glReadPixels
        if (fbo) {
            Framebuffer fb = MC.getFramebuffer();
            fb.bindFramebufferTexture();
            if (pbo) {
                glGetTexImage(GL_TEXTURE_2D, 0, FORMAT, TYPE, 0);
            } else {
                glGetTexImage(GL_TEXTURE_2D, 0, FORMAT, TYPE, buffer);
            }
            fb.unbindFramebufferTexture();
        } else {
            if (pbo) {
                glReadPixels(0, 0, width, height, FORMAT, TYPE, 0);
            } else {
                glReadPixels(0, 0, width, height, FORMAT, TYPE, buffer);
            }
        }
    }
}
