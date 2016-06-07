package info.ata4.minecraft.minema.client.modules.importers;

import info.ata4.minecraft.minema.Minema;
import info.ata4.minecraft.minema.client.config.MinemaConfig;
import info.ata4.minecraft.minema.client.event.FrameExportEvent;
import info.ata4.minecraft.minema.client.event.FrameImportEvent;
import info.ata4.minecraft.minema.client.event.FrameInitEvent;
import info.ata4.minecraft.minema.client.modules.CaptureModule;
import info.ata4.minecraft.minema.client.util.CaptureFrame;
import java.nio.ByteBuffer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.Util;

import static org.lwjgl.opengl.ARBBufferObject.*;
import static org.lwjgl.opengl.ARBPixelBufferObject.*;

public class FrameImporter extends CaptureModule {

    private static final int PBO_TARGET = GL_PIXEL_PACK_BUFFER_ARB;
    private static final int PBO_USAGE = GL_STREAM_READ_ARB;
    private static final int PBO_ACCESS = GL_READ_ONLY_ARB;

    private final boolean usePBO;
    private final boolean useFBO;

    // PBO state variables
    private int frontName;
    private int backName;
    private ByteBuffer bufferPBO;
    private boolean firstFrame;

    public FrameImporter(MinemaConfig cfg) {
        super(cfg);

        usePBO = GLContext.getCapabilities().GL_ARB_pixel_buffer_object && cfg.usePBO.get();
        useFBO = OpenGlHelper.isFramebufferEnabled();
    }

    @SubscribeEvent
    public void onFrameInit(FrameInitEvent e) {
        int bufferSize = e.frame.buffer.capacity();

        if (usePBO) {
            frontName = glGenBuffersARB();
            glBindBufferARB(PBO_TARGET, frontName);
            glBufferDataARB(PBO_TARGET, bufferSize, PBO_USAGE);

            backName = glGenBuffersARB();
            glBindBufferARB(PBO_TARGET, backName);
            glBufferDataARB(PBO_TARGET, bufferSize, PBO_USAGE);

            glBindBufferARB(PBO_TARGET, 0);

            firstFrame = true;
        }
    }

    @SubscribeEvent
    public void onFrameImport(FrameImportEvent e) {
        CaptureFrame frame = e.frame;

        // check if the dimensions are still the same
        frame.checkWindowSize();

        if (usePBO) {
            glBindBufferARB(PBO_TARGET, frontName);
        }

        // read pixels
        frame.readPixels(useFBO, usePBO);

        ByteBuffer buffer = e.frame.buffer;

        if (usePBO) {
            // copy back buffer
            glBindBufferARB(PBO_TARGET, backName);
            bufferPBO = glMapBufferARB(PBO_TARGET, PBO_ACCESS, buffer.capacity(), bufferPBO);
            bufferPBO.rewind();
            buffer.rewind();
            buffer.put(bufferPBO);
            glUnmapBufferARB(PBO_TARGET);
            glBindBufferARB(PBO_TARGET, 0);

            // If mapping threw an error -> crash immediately please
            Util.checkGLError();

            // swap PBOs
            int swapName = frontName;
            frontName = backName;
            backName = swapName;
        }

        buffer.rewind();
        
        // first frame is empty in PBO mode, don't export it
        if (usePBO && firstFrame) {
            firstFrame = false;
            return;
        }

        // send frame export event
        Minema.EVENT_BUS.post(new FrameExportEvent(e.frame, e.time, e.movieDir));
        e.time.nextFrame();
    }

    @Override
    protected void doEnable() throws Exception {
    }

    @Override
    protected void doDisable() throws Exception {
        if (usePBO) {
            glDeleteBuffersARB(frontName);
            glDeleteBuffersARB(backName);
        }
    }
}
