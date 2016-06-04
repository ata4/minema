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
import info.ata4.minecraft.minema.util.config.ConfigContainer;
import info.ata4.minecraft.minema.util.config.ConfigDouble;
import info.ata4.minecraft.minema.util.config.ConfigEnum;
import info.ata4.minecraft.minema.util.config.ConfigInteger;
import info.ata4.minecraft.minema.util.config.ConfigString;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Configuration;
import org.lwjgl.opengl.Display;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class MinemaConfig extends ConfigContainer {

    private static final int MAX_TEXTURE_SIZE = Minecraft.getGLMaximumTextureSize();

    public static final String CATEGORY_ENCODING = "encoding";
    public static final String CATEGORY_CAPTURING = "capturing";
    public static final String CATEGORY_ENGINE = "engine";

    public final ConfigBoolean useVideoEncoder = new ConfigBoolean(false);
    public final ConfigString videoEncoderPath = new ConfigString("");
    public final ConfigString videoEncoderParams = new ConfigString("");
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

    private Path movieDir;

    public MinemaConfig(Configuration config) {
        super(config);

        setLangKeyPrefix("minema.config");

        register(useVideoEncoder, "useVideoEncoder", CATEGORY_ENCODING);
        register(videoEncoderPath, "videoEncoderPath", CATEGORY_ENCODING);
        register(videoEncoderParams, "videoEncoderParams", CATEGORY_ENCODING);
        register(snapResolution, "snapResolution", CATEGORY_ENCODING);

        register(frameWidth, "frameWidth", CATEGORY_CAPTURING);
        register(frameHeight, "frameHeight", CATEGORY_CAPTURING);
        register(frameRate, "frameRate", CATEGORY_CAPTURING);
        register(frameLimit, "frameLimit", CATEGORY_CAPTURING);
        register(capturePath, "capturePath", CATEGORY_CAPTURING);
        register(showOverlay, "showOverlay", CATEGORY_CAPTURING);
        register(usePBO, "usePBO", CATEGORY_CAPTURING);

        register(engineSpeed, "engineSpeed", CATEGORY_ENGINE);
        // register(particleLimit, "particleLimit", CATEGORY_ENGINE);
        register(syncEngine, "syncEngine", CATEGORY_ENGINE);

        update(true);
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

    public Path getMovieDir() {
        return movieDir;
    }

    public void setMovieDir(Path movieDir) {
        this.movieDir = movieDir;
    }
}
