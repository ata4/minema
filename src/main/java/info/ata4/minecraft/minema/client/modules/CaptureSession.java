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

import info.ata4.minecraft.minema.client.capture.ACapturer;
import info.ata4.minecraft.minema.client.capture.FramebufferCapturer;
import info.ata4.minecraft.minema.client.capture.PBOCapturer;
import info.ata4.minecraft.minema.client.config.MinemaConfig;
import info.ata4.minecraft.minema.client.event.FrameCaptureEvent;
import info.ata4.minecraft.minema.client.event.FramePreCaptureEvent;
import info.ata4.minecraft.minema.client.util.CaptureTime;
import info.ata4.minecraft.minema.client.util.ChatUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class CaptureSession extends ACaptureModule {

	private static final Logger L = LogManager.getLogger();

	private final ArrayList<ACaptureModule> modules = new ArrayList<ACaptureModule>();
	private final EventBus eventBus = new EventBus();

	private CaptureTime time;
	private ACapturer capturer;

	private File movieDir;

	public CaptureSession(final MinemaConfig cfg) {
		super(cfg);
	}

	@Override
	protected void doEnable() {
		// create and set movie dir
		final File captureDir = new File(this.cfg.capturePath.get());
		final String movieName = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
		this.movieDir = new File(captureDir, movieName);
		if (!this.movieDir.exists()) {
			this.movieDir.mkdirs();
		}
		this.cfg.setMovieDir(this.movieDir);

		// init modules
		this.modules.add(new GameSettingsModifier(this.cfg));

		if (this.cfg.isSyncEngine()) {
			this.modules.add(new TimerModifier(this.cfg));
			this.modules.add(new TickSynchronizer(this.cfg));
		}

		if (this.cfg.useFrameSize()) {
			this.modules.add(new DisplaySizeModifier(this.cfg));
		}

		FrameExporter exporter;
		if (this.cfg.useVideoEncoder.get()) {
			exporter = new PipeFrameExporter(this.cfg);
		} else {
			exporter = new ImageFrameExporter(this.cfg);
		}
		this.modules.add(exporter);

		if (this.cfg.showOverlay.get()) {
			this.modules.add(new CaptureOverlay(this.cfg, this));
		}

		// enable and register modules
		for (final ACaptureModule module : this.modules) {
			module.enable();
			this.eventBus.register(module);
		}
		MinecraftForge.EVENT_BUS.register(this);

		// reset capturing stats
		this.time = new CaptureTime(this.cfg);

		// configure framebuffer capturer
		if (PBOCapturer.isSupported) {
			capturer = new PBOCapturer();
			System.out.println("Using PBO: true");
		} else {
			capturer = new FramebufferCapturer();
			System.out.println("Using PBO: false");
		}
		exporter.configureCapturer(this.capturer);
	}

	@Override
	protected void doDisable() {
		capturer.close();
		// disable and unregister modules
		for (final ACaptureModule module : this.modules) {
			try {
				if (module.isEnabled()) {
					module.disable();
				}
			} catch (final Throwable t) {
				L.error("Can't disable module {}", module.getName(), t);
			}

			try {
				this.eventBus.unregister(module);
			} catch (final NullPointerException ex) {
				// module doesn't have any event methods or wasn't registered.
				// unfortunately, the unregister method isn't smart enough to
				// notice that and throws NPEs...
			}
		}
		MinecraftForge.EVENT_BUS.unregister(this);

		this.modules.clear();

		// delete empty movie dir
		if (this.movieDir != null && this.movieDir.exists() && this.movieDir.list().length == 0) {
			this.movieDir.delete();
		}
		this.cfg.setMovieDir(null);

	}

	@Override
	protected void handleError(Throwable t) {
		ChatUtils.print("minema.error.label", TextFormatting.RED);

		// get list of throwables and their causes
		final List<Throwable> throwables = new ArrayList<Throwable>();
		do {
			throwables.add(t);
			t = t.getCause();
		} while (t != null);

		for (final Throwable throwable : throwables) {
			final String message = throwable.getMessage();

			// skip wrapped exceptions
			if (message == null) {
				continue;
			}

			// skip wrapped exceptions with generated messages
			final Throwable cause = throwable.getCause();
			if (cause != null && message.equals(cause.toString())) {
				continue;
			}

			ChatUtils.print(message, TextFormatting.RED);
		}
	}

	@SubscribeEvent
	public void captureFrame(final RenderTickEvent e) {
		if (!isEnabled()) {
			return;
		}

		// skip frames if the capturing framerate is not synchronized with the
		// rendering framerate
		if (!this.cfg.isSyncEngine() && !this.time.isNextFrame()) {
			// Game renders faster than necessary for recording?
			return;
		}

		try {
			if (this.eventBus
					.post(new FramePreCaptureEvent(this.time.getNumFrames(), this.capturer.getCaptureDimension()))) {
				throw new RuntimeException("Frame capturing cancelled at frame " + this.time.getNumFrames());
			}

			this.capturer.doCapture();

			if (this.eventBus.post(new FrameCaptureEvent(this.time.getNumFrames(), this.capturer.getCaptureDimension(),
					this.capturer.getByteBuffer()))) {
				throw new RuntimeException("Frame capturing cancelled at frame " + this.time.getNumFrames());
			}

			this.time.nextFrame();

			if (this.time.isAtFrameLimit()) {
				disable();
			}
		} catch (final Throwable t) {
			L.error("Frame capturing error", t);
			handleError(t);
			disable();
		}
	}

	public CaptureTime getCaptureTime() {
		return this.time;
	}
}
