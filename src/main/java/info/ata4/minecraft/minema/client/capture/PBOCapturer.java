package info.ata4.minecraft.minema.client.capture;

import static org.lwjgl.opengl.ARBBufferObject.glBindBufferARB;
import static org.lwjgl.opengl.ARBBufferObject.glBufferDataARB;
import static org.lwjgl.opengl.ARBBufferObject.glDeleteBuffersARB;
import static org.lwjgl.opengl.ARBBufferObject.glGenBuffersARB;
import static org.lwjgl.opengl.ARBBufferObject.glMapBufferARB;
import static org.lwjgl.opengl.ARBBufferObject.glUnmapBufferARB;
import static org.lwjgl.opengl.GL11.GL_PACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glPixelStorei;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.ARBPixelBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

public class PBOCapturer extends ACapturer {

	public static final boolean isSupported = GLContext.getCapabilities().GL_ARB_pixel_buffer_object;

	private static final int pixelPackBuffer = ARBPixelBufferObject.GL_PIXEL_PACK_BUFFER_ARB;
	private final static int STREAM_READ = ARBPixelBufferObject.GL_STREAM_READ_ARB;

	private int frontAddress;
	private int backAddress;
	private ByteBuffer frontCache;
	private ByteBuffer backCache;

	public PBOCapturer() {
		this.frontAddress = glGenBuffersARB();
		glBindBufferARB(pixelPackBuffer, frontAddress);
		glBufferDataARB(pixelPackBuffer, bufferSize, STREAM_READ);

		this.backAddress = glGenBuffersARB();
		glBindBufferARB(pixelPackBuffer, backAddress);
		glBufferDataARB(pixelPackBuffer, bufferSize, STREAM_READ);

		glBindBufferARB(pixelPackBuffer, 0);
	}

	private void swapPBOs() {
		int swapAddress = this.frontAddress;
		frontAddress = backAddress;
		backAddress = swapAddress;
		ByteBuffer swapGlBuffer = frontCache;
		frontCache = backCache;
		backCache = swapGlBuffer;
	}

	public void capture() {
		// set alignment flags
		glPixelStorei(GL_PACK_ALIGNMENT, 1);
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

		glBindBufferARB(pixelPackBuffer, frontAddress);

		GL11.glReadPixels(0, 0, start.getWidth(), start.getHeight(), colorFormat, GL_UNSIGNED_BYTE, 0);

		glBindBufferARB(pixelPackBuffer, 0);

		swapPBOs();

		glBindBufferARB(pixelPackBuffer, frontAddress);

		frontCache = glMapBufferARB(pixelPackBuffer, STREAM_READ, bufferSize, frontCache);
		if (frontCache != null) // Is null for the first frame
			this.buffer.put(frontCache);
		glUnmapBufferARB(pixelPackBuffer);

		glBindBufferARB(pixelPackBuffer, 0);
	}

	@Override
	public void close() {
		glDeleteBuffersARB(frontAddress);
		glDeleteBuffersARB(backAddress);
	}

}
