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

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.Display;

import info.ata4.minecraft.minema.util.config.ConfigBoolean;
import info.ata4.minecraft.minema.util.config.ConfigContainer;
import info.ata4.minecraft.minema.util.config.ConfigDouble;
import info.ata4.minecraft.minema.util.config.ConfigEnum;
import info.ata4.minecraft.minema.util.config.ConfigInteger;
import info.ata4.minecraft.minema.util.config.ConfigString;
import info.ata4.minecraft.minema.util.config.ConfigStringEnum;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Configuration;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class MinemaConfig extends ConfigContainer {

	private static final int MAX_TEXTURE_SIZE = Minecraft.getGLMaximumTextureSize();

	public static final String CATEGORY_ENCODING = "encoding";
	public static final String CATEGORY_CAPTURING = "capturing";
	public static final String CATEGORY_ENGINE = "engine";

	private static Set<String> getImageFormats() {
		final Set<String> formats = new HashSet<String>();

		// add all writer format names and convert them to lower case
		for (final String formatName : ImageIO.getWriterFormatNames()) {
			formats.add(formatName.toLowerCase());
		}

		// "tga" is always supported
		formats.add("tga");

		// "jpeg" is added together with "jpg" for some reason
		formats.remove("jpeg");

		return formats;
	}

	public final ConfigStringEnum imageFormat = new ConfigStringEnum("tga", getImageFormats());
	public final ConfigBoolean useVideoEncoder = new ConfigBoolean(false);
	public final ConfigString videoEncoderPath = new ConfigString("");
	public final ConfigString videoEncoderParams = new ConfigString("");
	public final ConfigEnum<SnapResolution> snapResolution = new ConfigEnum<SnapResolution>(SnapResolution.MOD2);

	public final ConfigInteger frameWidth = new ConfigInteger(0, 0, MAX_TEXTURE_SIZE);
	public final ConfigInteger frameHeight = new ConfigInteger(0, 0, MAX_TEXTURE_SIZE);
	public final ConfigDouble frameRate = new ConfigDouble(30.0, 0.01, 1000.0);
	public final ConfigInteger frameLimit = new ConfigInteger(-1, -1);
	public final ConfigString capturePath = new ConfigString("movies");
	public final ConfigBoolean showOverlay = new ConfigBoolean(false);

	public final ConfigDouble engineSpeed = new ConfigDouble(1.0, 0.01);
	// public final ConfigInteger particleLimit = new ConfigInteger(64000, -1);
	public final ConfigBoolean syncEngine = new ConfigBoolean(true);
	// public final ConfigBoolean preloadChunks = new ConfigBoolean(false);

	private File movieDir;

	public MinemaConfig(final Configuration config) {
		super(config);

		setLangKeyPrefix("minema.config");

		register(this.imageFormat, "imageFormat", CATEGORY_ENCODING);
		register(this.useVideoEncoder, "useVideoEncoder", CATEGORY_ENCODING);
		register(this.videoEncoderPath, "videoEncoderPath", CATEGORY_ENCODING);
		register(this.videoEncoderParams, "videoEncoderParams", CATEGORY_ENCODING);
		register(this.snapResolution, "snapResolution", CATEGORY_ENCODING);

		register(this.frameWidth, "frameWidth", CATEGORY_CAPTURING);
		register(this.frameHeight, "frameHeight", CATEGORY_CAPTURING);
		register(this.frameRate, "frameRate", CATEGORY_CAPTURING);
		register(this.frameLimit, "frameLimit", CATEGORY_CAPTURING);
		register(this.capturePath, "capturePath", CATEGORY_CAPTURING);
		register(this.showOverlay, "showOverlay", CATEGORY_CAPTURING);

		register(this.engineSpeed, "engineSpeed", CATEGORY_ENGINE);
		// register(particleLimit, "particleLimit", CATEGORY_ENGINE);
		register(this.syncEngine, "syncEngine", CATEGORY_ENGINE);

		update(true);
	}

	public int getFrameWidth() {
		int width;

		if (this.frameWidth.get() == 0) {
			width = Display.getWidth();
		} else {
			width = this.frameWidth.get();
		}

		if (this.useVideoEncoder.get()) {
			width = this.snapResolution.get().snap(width);
		}

		return width;
	}

	public int getFrameHeight() {
		int height;

		if (this.frameHeight.get() == 0) {
			height = Display.getHeight();
		} else {
			height = this.frameHeight.get();
		}

		if (this.useVideoEncoder.get()) {
			height = this.snapResolution.get().snap(height);
		}

		return height;
	}

	public boolean useFrameSize() {
		return getFrameWidth() != Display.getWidth() || getFrameHeight() != Display.getHeight();
	}

	public boolean isSyncEngine() {
		return Minecraft.getMinecraft().isSingleplayer() && this.syncEngine.get();
	}

	public File getMovieDir() {
		return this.movieDir;
	}

	public void setMovieDir(final File movieDir) {
		this.movieDir = movieDir;
	}
}
