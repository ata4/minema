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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import info.ata4.minecraft.minema.client.capture.FramebufferCapturer;
import info.ata4.minecraft.minema.client.config.MinemaConfig;
import info.ata4.minecraft.minema.client.event.CapturePausedEvent;
import info.ata4.minecraft.minema.client.event.CaptureResumedEvent;
import info.ata4.minecraft.minema.client.event.FrameCaptureEvent;
import info.ata4.minecraft.minema.client.event.FramePreCaptureEvent;
import info.ata4.minecraft.minema.client.util.CaptureTime;
import info.ata4.minecraft.minema.client.util.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class CaptureSession extends CaptureModule {

	private static final Minecraft MC = Minecraft.getMinecraft();
	private static final Logger L = LogManager.getLogger();

	private final List<CaptureModule> modules = new ArrayList<CaptureModule>();
	private final EventBus eventBus = new EventBus();

	private CaptureTime time;
	private FramebufferCapturer fbc;

	private File movieDir;
	private boolean paused;

	public CaptureSession(MinemaConfig cfg) {
		super(cfg);
	}

	@Override
	protected void doEnable() {
		// create and set movie dir
		File captureDir = new File(cfg.capturePath.get());
		String movieName = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
		movieDir = new File(captureDir, movieName);
		if (!movieDir.exists()) {
			movieDir.mkdirs();
		}
		cfg.setMovieDir(movieDir);

		// init modules
		modules.add(new GameSettingsModifier(cfg));

		if (cfg.isSyncEngine()) {
			modules.add(new TimerModifier(cfg));
			modules.add(new TickSynchronizer(cfg));
		}

		if (cfg.useFrameSize()) {
			modules.add(new DisplaySizeModifier(cfg));
		}

		modules.add(new EffectRendererModifier(cfg));

		FrameExporter exporter;
		if (cfg.useVideoEncoder.get()) {
			exporter = new PipeFrameExporter(cfg);
		} else {
			exporter = new ImageFrameExporter(cfg);
		}
		modules.add(exporter);

		if (cfg.preloadChunks.get()) {
			modules.add(new ChunkPreloader(cfg));
		}

		if (cfg.showOverlay.get()) {
			modules.add(new CaptureOverlay(cfg, this));
		}

		// enable and register modules
		for (CaptureModule module : modules) {
			module.enable();
			eventBus.register(module);
		}

		// reset capturing stats
		time = new CaptureTime(cfg);

		// configure framebuffer capturer
		fbc = new FramebufferCapturer();
		exporter.configureCapturer(fbc);

		// play a sound
		playSound();
	}

	@Override
	protected void doDisable() {
		// disable and unregister modules
		for (CaptureModule module : modules) {
			try {
				if (module.isEnabled()) {
					module.disable();
				}
			} catch (Throwable t) {
				L.error("Can't disable module {}", module.getName(), t);
			}

			try {
				eventBus.unregister(module);
			} catch (NullPointerException ex) {
				// module doesn't have any event methods or wasn't registered.
				// unfortunately, the unregister method isn't smart enough to
				// notice that and throws NPEs...
			}
		}

		modules.clear();

		// delete empty movie dir
		if (movieDir != null && movieDir.exists() && movieDir.list().length == 0) {
			movieDir.delete();
		}
		cfg.setMovieDir(null);

	}

	@Override
	protected void handleError(Throwable t) {
		ChatUtils.print("minema.error.label", TextFormatting.RED);

		// get list of throwables and their causes
		List<Throwable> throwables = new ArrayList<Throwable>();
		do {
			throwables.add(t);
			t = t.getCause();
		} while (t != null);

		for (Throwable throwable : throwables) {
			String message = throwable.getMessage();

			// skip wrapped exceptions
			if (message == null) {
				continue;
			}

			// skip wrapped exceptions with generated messages
			Throwable cause = throwable.getCause();
			if (cause != null && message.equals(cause.toString())) {
				continue;
			}

			ChatUtils.print(message, TextFormatting.RED);
		}
	}

	public void captureFrame() {
		if (!isEnabled() || isPaused()) {
			return;
		}

		// skip frames if the capturing framerate is not synchronized with the
		// rendering framerate
		if (!cfg.isSyncEngine() && !time.isNextFrame()) {
			// not yet!
			return;
		}

		try {
			if (eventBus.post(new FramePreCaptureEvent(time.getNumFrames(), fbc.getCaptureDimension()))) {
				throw new RuntimeException("Frame capturing cancelled at frame " + time.getNumFrames());
			}

			fbc.capture();

			if (eventBus
					.post(new FrameCaptureEvent(time.getNumFrames(), fbc.getCaptureDimension(), fbc.getByteBuffer()))) {
				throw new RuntimeException("Frame capturing cancelled at frame " + time.getNumFrames());
			}

			time.nextFrame();

			if (time.isAtFrameLimit()) {
				disable();
			}
		} catch (Throwable t) {
			L.error("Frame capturing error", t);
			handleError(t);
			disable();
		}
	}

	public boolean isPaused() {
		return paused;
	}

	public void setPaused(boolean paused) {
		// prepare event if the paused status changed
		Event evt = null;
		if (paused && !this.paused) {
			evt = new CapturePausedEvent();
		} else if (!paused && this.paused) {
			evt = new CaptureResumedEvent();
		}

		this.paused = paused;

		// send event to notify modules
		if (evt != null) {
			eventBus.post(evt);
		}
	}

	public CaptureTime getCaptureTime() {
		return time;
	}

	private void playSound() {
		try {
			if (MC.theWorld != null && MC.thePlayer != null) {
				MC.theWorld.playSound(MC.thePlayer.posX, MC.thePlayer.posY, MC.thePlayer.posZ,
						new SoundEvent(new ResourceLocation("mob.chicken.plop")), SoundCategory.NEUTRAL, 1, 1, false);
			}
		} catch (Exception ex) {
			L.warn("Can't play capture sound", ex);
		}
	}
}
