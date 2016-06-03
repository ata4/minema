package info.ata4.minecraft.minema.client.modules.modifiers;

import info.ata4.minecraft.minema.client.config.MinemaConfig;
import info.ata4.minecraft.minema.client.modules.CaptureModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.Display;

public class DisplaySizeModifier extends CaptureModule {

    private static final Minecraft MC = Minecraft.getMinecraft();

    private int originalWidth;
    private int originalHeight;

    public DisplaySizeModifier(MinemaConfig cfg) {
        super(cfg);
    }

    @Override
    protected void doEnable() {
        originalWidth = Display.getWidth();
        originalHeight = Display.getHeight();

        resize(cfg.getFrameWidth(), cfg.getFrameHeight());

        // render framebuffer texture in original size
        if (OpenGlHelper.isFramebufferEnabled()) {
            setFramebufferTextureSize(originalWidth, originalHeight);
        }
    }

    @Override
    protected void doDisable() {
        resize(originalWidth, originalHeight);
    }

    public void resize(int width, int height) {
        MC.resize(width, height);
    }

    public void setFramebufferTextureSize(int width, int height) {
        Framebuffer fb = MC.getFramebuffer();
        fb.framebufferTextureWidth = width;
        fb.framebufferTextureHeight = height;
    }
}
