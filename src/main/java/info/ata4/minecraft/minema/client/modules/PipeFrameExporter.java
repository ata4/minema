/*
 ** 2014 July 29
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.minema.client.modules;

import java.io.File;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import info.ata4.minecraft.minema.client.capture.FramebufferCapturer;
import info.ata4.minecraft.minema.client.config.MinemaConfig;
import info.ata4.minecraft.minema.client.event.FrameCaptureEvent;
import info.ata4.minecraft.minema.io.StreamPipe;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class PipeFrameExporter extends FrameExporter {

	private static final Logger L = LogManager.getLogger();

	private Process proc;
	private WritableByteChannel pipe;

	private OutputStream log;

	public PipeFrameExporter(final MinemaConfig cfg) {
		super(cfg);
	}

	@Override
	protected void doEnable() throws Exception {
		super.doEnable();

		String params = this.cfg.videoEncoderParams.get();
		params = params.replace("%WIDTH%", String.valueOf(this.cfg.getFrameWidth()));
		params = params.replace("%HEIGHT%", String.valueOf(this.cfg.getFrameHeight()));
		params = params.replace("%FPS%", String.valueOf(this.cfg.frameRate.get()));

		final List<String> cmds = new ArrayList<String>();
		cmds.add(this.cfg.videoEncoderPath.get());
		cmds.addAll(Arrays.asList(StringUtils.split(params, ' ')));

		// build encoder process
		final ProcessBuilder pb = new ProcessBuilder(cmds);
		pb.directory(this.cfg.getMovieDir());
		this.proc = pb.start();

		// Java 1.6 doesn't know redirectOutput/redirectError and these
		// streams need to be emptied to avoid blocking
		this.log = FileUtils.openOutputStream(new File(this.cfg.getMovieDir(), "encoder.log"));
		new StreamPipe(this.proc.getInputStream(), this.log).start();
		new StreamPipe(this.proc.getErrorStream(), this.log).start();

		// create channel from output stream
		this.pipe = Channels.newChannel(this.proc.getOutputStream());
	}

	@Override
	protected void doDisable() throws Exception {
		super.doDisable();

		IOUtils.closeQuietly(this.pipe);
		IOUtils.closeQuietly(this.log);

		if (this.proc != null) {
			try {
				this.proc.waitFor();
			} catch (final InterruptedException ex) {
				L.warn("Pipe program termination interrupted", ex);
			}

			this.proc.destroy();
		}
	}

	@Override
	public void configureCapturer(final FramebufferCapturer fbc) {
		fbc.setFlipColors(false);
		fbc.setFlipLines(true);
	}

	@Override
	protected void doExportFrame(final FrameCaptureEvent evt) throws Exception {
		if (this.pipe.isOpen()) {
			this.pipe.write(evt.frameBuffer);
		}
	}
}
