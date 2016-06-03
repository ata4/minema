/*
 ** 2014 July 29
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.minema.client.modules.exporters;

import info.ata4.minecraft.minema.client.capture.Capturer;
import info.ata4.minecraft.minema.client.config.MinemaConfig;
import info.ata4.minecraft.minema.client.event.FrameCaptureEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class PipeFrameExporter extends FrameExporter {

    private static final Logger L = LogManager.getLogger();

    private Process proc;
    private WritableByteChannel pipe;

    public PipeFrameExporter(MinemaConfig cfg) {
        super(cfg);
    }

    @Override
    protected void doEnable() throws Exception {
        super.doEnable();

        String params = cfg.videoEncoderParams.get();
        params = params.replace("%WIDTH%", String.valueOf(cfg.getFrameWidth()));
        params = params.replace("%HEIGHT%", String.valueOf(cfg.getFrameHeight()));
        params = params.replace("%FPS%", String.valueOf(cfg.frameRate.get()));
        
        List<String> cmds = new ArrayList<>();
        cmds.add(cfg.videoEncoderPath.get());
        cmds.addAll(Arrays.asList(StringUtils.split(params, ' ')));

        // build encoder process and redirect output
        ProcessBuilder pb = new ProcessBuilder(cmds);
        pb.directory(cfg.getMovieDir());
        pb.redirectErrorStream(true);
        pb.redirectOutput(new File(cfg.getMovieDir(), "encoder.log"));
        proc = pb.start();

        // Java wraps the process output stream into a BufferedOutputStream,
        // but its little buffer is just slowing everything down with the huge
        // amount of data we're dealing here, so unwrap it with this little hack.
        OutputStream os = proc.getOutputStream();
        if (os instanceof BufferedOutputStream) {
            Field outField = FilterOutputStream.class.getDeclaredField("out");
            outField.setAccessible(true);
            os = (OutputStream) outField.get(os);
        }
        
        pipe = Channels.newChannel(os);
    }

    @Override
    protected void doDisable() throws Exception {
        super.doDisable();

        try {
            if (pipe.isOpen()) {
                pipe.close();
            }
        } catch (IOException ex) {
            L.warn("Pipe not closed properly", ex);
        }

        try {
            if (proc != null) {
                proc.waitFor(1, TimeUnit.MINUTES);
                proc.destroy();
            }
        } catch (InterruptedException ex) {
            L.warn("Pipe program termination interrupted", ex);
        }
    }

    @Override
    public void configureCapturer(Capturer fbc) {
    }

    @Override
    protected void doExportFrame(FrameCaptureEvent evt) throws Exception {
        if (pipe.isOpen()) {
            pipe.write(evt.frameBuffer);
        }
    }
}
