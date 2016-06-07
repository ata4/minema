/*
 ** 2014 July 28
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.minema.client.config;

import info.ata4.minecraft.minema.util.config.ConfigBoolean;
import info.ata4.minecraft.minema.util.config.ConfigDouble;
import info.ata4.minecraft.minema.util.config.ConfigEnum;
import info.ata4.minecraft.minema.util.config.ConfigInteger;
import info.ata4.minecraft.minema.util.config.ConfigString;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Configuration;
import org.lwjgl.opengl.Display;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class MinemaConfig {

    private static final int MAX_TEXTURE_SIZE = Minecraft.getGLMaximumTextureSize();

    public static final String LANG_KEY = "minema.config";

    public final ConfigBoolean useVideoEncoder = new ConfigBoolean(true);
    public final ConfigString videoEncoderPath = new ConfigString("ffmpeg");
    public final ConfigString videoEncoderParams = new ConfigString(
        "-f rawvideo -pix_fmt bgr24 -s %WIDTH%x%HEIGHT% -r %FPS% -i - -vf vflip " +
        "-c:v libx264 -preset ultrafast -tune zerolatency -qp 20 video.mp4");
    public final ConfigEnum<SnapResolution> snapResolution = new ConfigEnum<>(SnapResolution.MOD2);

    public final ConfigInteger frameWidth = new ConfigInteger(0, 0, MAX_TEXTURE_SIZE);
    public final ConfigInteger frameHeight = new ConfigInteger(0, 0, MAX_TEXTURE_SIZE);
    public final ConfigDouble frameRate = new ConfigDouble(30.0, 0.01, 1000.0);
    public final ConfigInteger frameLimit = new ConfigInteger(-1, -1);
    public final ConfigString capturePath = new ConfigString("movies");
    public final ConfigBoolean showOverlay = new ConfigBoolean(false);
    public final ConfigBoolean usePBO = new ConfigBoolean(true);

    public final ConfigDouble engineSpeed = new ConfigDouble(1.0, 0.01);
    // public final ConfigInteger particleLimit = new ConfigInteger(64000, -1);
    public final ConfigBoolean syncEngine = new ConfigBoolean(true);
    // public final ConfigBoolean preloadChunks = new ConfigBoolean(false);

    public MinemaConfig(Configuration cfg) {
        useVideoEncoder.link(cfg, "encoding.useVideoEncoder", LANG_KEY);
        videoEncoderPath.link(cfg, "encoding.videoEncoderPath", LANG_KEY);
        videoEncoderParams.link(cfg, "encoding.videoEncoderParams", LANG_KEY);
        snapResolution.link(cfg, "encoding.snapResolution", LANG_KEY);

        frameWidth.link(cfg, "capturing.frameWidth", LANG_KEY);
        frameHeight.link(cfg, "capturing.frameHeight", LANG_KEY);
        frameRate.link(cfg, "capturing.frameRate", LANG_KEY);
        frameLimit.link(cfg, "capturing.frameLimit", LANG_KEY);
        capturePath.link(cfg, "capturing.capturePath", LANG_KEY);
        showOverlay.link(cfg, "capturing.showOverlay", LANG_KEY);
        usePBO.link(cfg, "capturing.usePBO", LANG_KEY);

        engineSpeed.link(cfg, "engine.engineSpeed", LANG_KEY);
        // particleLimit.link(config, "engine.particleLimit", LANG_KEY);
        syncEngine.link(cfg, "engine.syncEngine", LANG_KEY);
    }

    public int getFrameWidth() {
        int width = frameWidth.get();
        
        // use display width if not set
        if (width == 0) {
            width = Display.getWidth();
        }

        // snap to nearest
        if (useVideoEncoder.get()) {
            width = snapResolution.get().snap(width);
        }

        return width;
    }

    public int getFrameHeight() {
        int height = frameHeight.get();
        
        // use display height if not set
        if (height == 0) {
            height = Display.getHeight();
        }

        // snap to nearest
        if (useVideoEncoder.get()) {
            height = snapResolution.get().snap(height);
        }

        return height;
    }

    public boolean useFrameSize() {
        return getFrameWidth() != Display.getWidth() || getFrameHeight() != Display.getHeight();
    }

    public boolean isSyncEngine() {
        return Minecraft.getMinecraft().isSingleplayer() && syncEngine.get();
    }
}
