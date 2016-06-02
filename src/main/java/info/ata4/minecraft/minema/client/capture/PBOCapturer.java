package info.ata4.minecraft.minema.client.capture;

import java.nio.ByteBuffer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.Util;

import static org.lwjgl.opengl.ARBBufferObject.*;
import static org.lwjgl.opengl.ARBPixelBufferObject.*;
import static org.lwjgl.opengl.GL11.*;

public class PBOCapturer extends Capturer {

    public static final boolean isSupported = GLContext.getCapabilities().GL_ARB_pixel_buffer_object;

    private static final int PACK_MODE = GL_PIXEL_PACK_BUFFER_ARB;
    private static final int STREAM_READ = GL_STREAM_READ_ARB;
    private static final int READ_ONLY_ACCESS = GL_READ_ONLY_ARB;

    private int frontAddress;
    private int backAddress;
    private ByteBuffer frontCache;
    private ByteBuffer backCache;

    public PBOCapturer() {
        frontAddress = glGenBuffersARB();
        glBindBufferARB(PACK_MODE, frontAddress);
        glBufferDataARB(PACK_MODE, bufferSize, STREAM_READ);

        backAddress = glGenBuffersARB();
        glBindBufferARB(PACK_MODE, backAddress);
        glBufferDataARB(PACK_MODE, bufferSize, STREAM_READ);

        glBindBufferARB(PACK_MODE, 0);
    }

    private void swapPBOs() {
        int swapAddress = frontAddress;
        frontAddress = backAddress;
        backAddress = swapAddress;
        ByteBuffer swapGlBuffer = frontCache;
        frontCache = backCache;
        backCache = swapGlBuffer;
    }

    @Override
    public void capture() {
        glBindBufferARB(PACK_MODE, frontAddress);

        // Calling into event queue
        // set alignment flags (has to be inside event queue)
        glPixelStorei(GL_PACK_ALIGNMENT, 1);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        // use faster framebuffer access if enabled
        if (OpenGlHelper.isFramebufferEnabled()) {
            Framebuffer fb = MC.getFramebuffer();
            fb.bindFramebufferTexture();
            glGetTexImage(GL_TEXTURE_2D, 0, FORMAT, GL_UNSIGNED_BYTE, 0);
            fb.unbindFramebufferTexture();
        } else {
            glReadPixels(0, 0, start.getWidth(), start.getHeight(), FORMAT, GL_UNSIGNED_BYTE, 0);
        }

        // Not calling into event queue
        glBindBufferARB(PACK_MODE, 0);

        swapPBOs();

        glBindBufferARB(PACK_MODE, frontAddress);

        frontCache = glMapBufferARB(PACK_MODE, READ_ONLY_ACCESS, bufferSize, frontCache);
        // If mapping threw an error -> crash immediately please
        Util.checkGLError();
        buffer.put(frontCache);
        // Recycling native buffers also needs rewinding! Not doing so would
        // result in fast line flipping of the first frame (or not if you do not
        // use PipeExporter) -> a symptom of not writing due to not rewinding
        frontCache.rewind();
        glUnmapBufferARB(PACK_MODE);

        glBindBufferARB(PACK_MODE, 0);
    }

    @Override
    public void close() {
        glDeleteBuffersARB(frontAddress);
        glDeleteBuffersARB(backAddress);
    }

}
