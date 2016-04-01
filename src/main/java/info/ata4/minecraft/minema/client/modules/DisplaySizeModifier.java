/*
 ** 2014 July 28
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.minema.client.modules;

import java.lang.reflect.Method;

import org.lwjgl.opengl.Display;

import info.ata4.minecraft.minema.client.config.MinemaConfig;
import info.ata4.minecraft.minema.util.reflection.PrivateFields;
import info.ata4.minecraft.minema.util.reflection.PrivateMethods;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
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
        try {
            Method resize = ReflectionHelper.findMethod(Minecraft.class, MC, PrivateMethods.MINECRAFT_RESIZE, Integer.TYPE, Integer.TYPE);
            resize.invoke(MC, width, height);
        } catch (Exception ex) {
            throw new RuntimeException("Can't resize display", ex);
        }
    }
    
    public void setFramebufferTextureSize(int width, int height) {
        try {
            Framebuffer fb = MC.getFramebuffer();
            ReflectionHelper.setPrivateValue(Framebuffer.class, fb, width, PrivateFields.FRAMEBUFFER_FRAMEBUFFERTEXTUREWIDTH);
            ReflectionHelper.setPrivateValue(Framebuffer.class, fb, height, PrivateFields.FRAMEBUFFER_FRAMEBUFFERTEXTUREHEIGHT);
        } catch (Exception ex) {
            throw new RuntimeException("Can't set framebuffer texture size", ex);
        }
    }
}
