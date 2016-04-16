package info.ata4.minecraft.minema.client.modules;

import org.lwjgl.opengl.Display;

import info.ata4.minecraft.minema.client.config.MinemaConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;

public class DisplaySizeModifier extends ACaptureModule {

	private static final Minecraft MC = Minecraft.getMinecraft();

	private int originalWidth;
	private int originalHeight;

	public DisplaySizeModifier(final MinemaConfig cfg) {
		super(cfg);
	}

	@Override
	protected void doEnable() {
		this.originalWidth = Display.getWidth();
		this.originalHeight = Display.getHeight();

		resize(this.cfg.getFrameWidth(), this.cfg.getFrameHeight());

		// render framebuffer texture in original size
		if (OpenGlHelper.isFramebufferEnabled()) {
			setFramebufferTextureSize(this.originalWidth, this.originalHeight);
		}
	}

	@Override
	protected void doDisable() {
		resize(this.originalWidth, this.originalHeight);
	}

	public void resize(final int width, final int height) {
		MC.resize(width, height);
	}

	public void setFramebufferTextureSize(final int width, final int height) {
		final Framebuffer fb = MC.getFramebuffer();
		fb.framebufferTextureWidth = width;
		fb.framebufferTextureHeight = height;
	}
}
