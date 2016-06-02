package info.ata4.minecraft.minema.client.capture;

import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL12.GL_BGR;

import java.nio.ByteBuffer;

import org.lwjgl.util.Dimension;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;

public abstract class Capturer {

    protected static Minecraft MC = Minecraft.getMinecraft();
    protected static boolean isFramebufferEnabled = OpenGlHelper.isFramebufferEnabled();
    protected static int bytesPerPixel = 3;
    protected static int TYPE = GL_UNSIGNED_BYTE;

    protected Dimension start;
    protected int bufferSize;
    protected ByteBuffer buffer;

    private byte[] flipLine1 = null;
    private byte[] flipLine2 = null;
    // BGR is the internal OpenGL format -> try to use that as
    // frequently as possible
    protected int colorFormat = GL_BGR;

    public Capturer() {
        start = new Dimension(MC.displayWidth, MC.displayHeight);
        bufferSize = start.getWidth() * start.getHeight() * bytesPerPixel;
        buffer = ByteBuffer.allocateDirect(bufferSize);
    }

    private void prepareByteBuffer() {
        if (flipLine1 == null || flipLine2 == null) {
            return;
        }

        int currentWidth = start.getWidth();
        int currentHeight = start.getHeight();

        // flip buffer vertically
        for (int i = 0; i < currentHeight / 2; i++) {
            int ofs1 = i * currentWidth * bytesPerPixel;
            int ofs2 = (currentHeight - i - 1) * currentWidth * bytesPerPixel;

            // read lines
            buffer.position(ofs1);
            buffer.get(flipLine1);
            buffer.position(ofs2);
            buffer.get(flipLine2);

            // write lines at swapped positions
            buffer.position(ofs2);
            buffer.put(flipLine1);
            buffer.position(ofs1);
            buffer.put(flipLine2);
        }
    }

    public ByteBuffer getByteBuffer() {
        prepareByteBuffer();
        // Rewinding to pos 0 (after capture)
        buffer.rewind();
        // ByteBuffer was once duplicated right here -> resulting in heavy
        // memory allocation (eg. 1280*720*3 bytes, which is about 2.8 MB per
        // frame and about 1.7 GB for 10 seconds in 60fps)
        // I know that this is not good practice, but it does not matter and
        // optimizes a lot
        return buffer;
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

    public void setFlipLines() {
        flipLine1 = new byte[start.getWidth() * bytesPerPixel];
        flipLine2 = new byte[start.getWidth() * bytesPerPixel];
    }

    public void setToRGBMode() {
        colorFormat = GL_RGB;
    }

    protected abstract void capture();

    public abstract void close();

}
