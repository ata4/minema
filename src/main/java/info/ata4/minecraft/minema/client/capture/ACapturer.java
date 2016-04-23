package info.ata4.minecraft.minema.client.capture;

import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL12.GL_BGR;

import java.nio.ByteBuffer;

import org.lwjgl.util.Dimension;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;

public abstract class ACapturer {

	protected static final Minecraft MC = Minecraft.getMinecraft();
	protected static final boolean isFramebufferEnabled = OpenGlHelper.isFramebufferEnabled();
	protected static final int bytesPerPixel = 3;
	protected static final int TYPE = GL_UNSIGNED_BYTE;

	protected final Dimension start;
	protected final int bufferSize;
	protected final ByteBuffer buffer;

	private byte[] flipLine1 = null;
	private byte[] flipLine2 = null;
	// BGR is the internal OpenGL format -> try to use that as
	// frequently as possible
	protected int colorFormat = GL_BGR;

	public ACapturer() {
		this.start = new Dimension(MC.displayWidth, MC.displayHeight);
		this.bufferSize = this.start.getWidth() * this.start.getHeight() * bytesPerPixel;
		this.buffer = ByteBuffer.allocateDirect(this.bufferSize);
	}

	private void prepareByteBuffer() {
		if (flipLine1 == null || flipLine2 == null) {
			return;
		}

		final int currentWidth = start.getWidth();
		final int currentHeight = start.getHeight();

		// flip buffer vertically
		for (int i = 0; i < currentHeight / 2; i++) {
			final int ofs1 = i * currentWidth * bytesPerPixel;
			final int ofs2 = (currentHeight - i - 1) * currentWidth * bytesPerPixel;

			// read lines
			this.buffer.position(ofs1);
			this.buffer.get(this.flipLine1);
			this.buffer.position(ofs2);
			this.buffer.get(this.flipLine2);

			// write lines at swapped positions
			this.buffer.position(ofs2);
			this.buffer.put(this.flipLine1);
			this.buffer.position(ofs1);
			this.buffer.put(this.flipLine2);
		}
	}

	public final ByteBuffer getByteBuffer() {
		prepareByteBuffer();
		// Rewinding to pos 0 (after capture)
		this.buffer.rewind();
		// ByteBuffer was once duplicated right here -> resulting in heavy
		// memory allocation (eg. 1280*720*3 bytes, which is about 2.8 MB per
		// frame and about 1.7 GB for 10 seconds in 60fps)
		// I know that this is not good practice, but it does not matter and
		// optimizes a lot
		return buffer;
	}

	public final void doCapture() {
		final int currentWidth = MC.displayWidth;
		final int currentHeight = MC.displayHeight;
		// check if the dimensions are still the same
		if (currentWidth != start.getWidth() || currentHeight != start.getHeight()) {
			throw new IllegalStateException(
					String.format("Display size changed! %dx%d not equals the start dimension of %dx%d", currentWidth,
							currentHeight, start.getWidth(), start.getHeight()));
		}

		// Rewind after writting
		this.buffer.rewind();
		capture();
	}

	public final Dimension getCaptureDimension() {
		return this.start;
	}

	public final void setFlipLines() {
		flipLine1 = new byte[start.getWidth() * bytesPerPixel];
		flipLine2 = new byte[start.getWidth() * bytesPerPixel];
	}

	public final void setToRGBMode() {
		colorFormat = GL_RGB;
	}

	protected abstract void capture();

	public abstract void close();

}
