package info.ata4.minecraft.minema.client.capture;

import java.nio.ByteBuffer;
import net.minecraft.client.Minecraft;
import org.lwjgl.util.Dimension;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

public abstract class Capturer {

    protected static final Minecraft MC = Minecraft.getMinecraft();
    protected static final int BPP = 3;
    protected static final int TYPE = GL_UNSIGNED_BYTE;
    protected static final int FORMAT = GL_BGR;

    protected Dimension start;
    protected int bufferSize;
    protected ByteBuffer buffer;

    public Capturer() {
        start = new Dimension(MC.displayWidth, MC.displayHeight);
        bufferSize = start.getWidth() * start.getHeight() * BPP;
        buffer = ByteBuffer.allocateDirect(bufferSize);
    }

    public ByteBuffer getByteBuffer() {
        return buffer.duplicate();
    }

    public void doCapture() {
        int currentWidth = MC.displayWidth;
        int currentHeight = MC.displayHeight;
        // check if the dimensions are still the same
        if (currentWidth != start.getWidth() || currentHeight != start.getHeight()) {
            throw new IllegalStateException(
                    String.format("Display size changed! %dx%d not equals the start dimension of %dx%d", currentWidth,
                            currentHeight, start.getWidth(), start.getHeight()));
        }

        // Rewind after writting
        buffer.rewind();
        capture();
    }

    public Dimension getCaptureDimension() {
        return start;
    }

    protected abstract void capture();

    public abstract void close();

}
