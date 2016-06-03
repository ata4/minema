package info.ata4.minecraft.minema.client.capture;

import java.nio.ByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.Util;
import org.lwjgl.util.Dimension;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.ARBBufferObject.*;
import static org.lwjgl.opengl.ARBPixelBufferObject.*;

public class Capturer {

    private static final Minecraft MC = Minecraft.getMinecraft();
    private static final int BPP = 3;
    private static final int TYPE = GL_UNSIGNED_BYTE;
    private static final int FORMAT = GL_BGR;

    private static final int PBO_TARGET = GL_PIXEL_PACK_BUFFER_ARB;
    private static final int PBO_USAGE = GL_STREAM_READ_ARB;
    private static final int PBO_ACCESS = GL_READ_ONLY_ARB;

    private final Dimension start;
    private final ByteBuffer buffer;

    private final boolean usePBO;
    private final boolean useFBO;

    // PBO state variables
    private int frontName;
    private int backName;
    private ByteBuffer bufferPBO;

    public Capturer() {
        this(true, true);
    }

    public Capturer(boolean pbo, boolean fbo) {
        start = new Dimension(MC.displayWidth, MC.displayHeight);
        
        usePBO = pbo && GLContext.getCapabilities().GL_ARB_pixel_buffer_object;
        useFBO = fbo && OpenGlHelper.isFramebufferEnabled();
        
        int bufferSize = start.getWidth() * start.getHeight() * BPP;
        buffer = ByteBuffer.allocateDirect(bufferSize);

        if (usePBO) {
            frontName = glGenBuffersARB();
            glBindBufferARB(PBO_TARGET, frontName);
            glBufferDataARB(PBO_TARGET, bufferSize, PBO_USAGE);

            backName = glGenBuffersARB();
            glBindBufferARB(PBO_TARGET, backName);
            glBufferDataARB(PBO_TARGET, bufferSize, PBO_USAGE);

            glBindBufferARB(PBO_TARGET, 0);
        }
    }

    public Dimension getCaptureDimension() {
        return start;
    }

    public ByteBuffer capture() {
        // check if the dimensions are still the same
        int currentWidth = MC.displayWidth;
        int currentHeight = MC.displayHeight;
        if (currentWidth != start.getWidth() || currentHeight != start.getHeight()) {
            throw new IllegalStateException(
                    String.format("Display size changed! %dx%d not equals the start dimension of %dx%d", currentWidth,
                            currentHeight, start.getWidth(), start.getHeight()));
        }

        if (usePBO) {
            glBindBufferARB(PBO_TARGET, frontName);
        }
        
        // read pixels
        buffer.rewind();
        readPixels();

        if (usePBO) {
            // copy back buffer
            glBindBufferARB(PBO_TARGET, backName);
            bufferPBO = glMapBufferARB(PBO_TARGET, PBO_ACCESS, buffer.capacity(), bufferPBO);
            bufferPBO.rewind();
            buffer.rewind();
            buffer.put(bufferPBO);
            glUnmapBufferARB(PBO_TARGET);
            glBindBufferARB(PBO_TARGET, 0);
            
            // swap PBOs
            int swapName = frontName;
            frontName = backName;
            backName = swapName;

            // If mapping threw an error -> crash immediately please
            Util.checkGLError();
        }
        
        buffer.rewind();
        return buffer;
    }

    public void close() {
        if (usePBO) {
            glDeleteBuffersARB(frontName);
            glDeleteBuffersARB(backName);
        }
    }

    protected void readPixels() {
        // set alignment flags
        glPixelStorei(GL_PACK_ALIGNMENT, 1);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        // read texture from framebuffer if enabled, otherwise use slower
        // glReadPixels
        if (useFBO) {
            Framebuffer fb = MC.getFramebuffer();
            fb.bindFramebufferTexture();
            if (usePBO) {
                glGetTexImage(GL_TEXTURE_2D, 0, FORMAT, TYPE, 0);
            } else {
                glGetTexImage(GL_TEXTURE_2D, 0, FORMAT, TYPE, buffer);
            }
            fb.unbindFramebufferTexture();
        } else {
            if (usePBO) {
                glReadPixels(0, 0, start.getWidth(), start.getHeight(), FORMAT, TYPE, 0);
            } else {
                glReadPixels(0, 0, start.getWidth(), start.getHeight(), FORMAT, TYPE, buffer);
            }
        }
    }
}
