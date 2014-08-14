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

import info.ata4.minecraft.minema.client.config.MinemaConfig;
import info.ata4.minecraft.minema.client.capture.FramebufferCapturer;
import info.ata4.minecraft.minema.client.event.FrameCaptureEvent;
import info.ata4.minecraft.minema.io.StreamPipe;
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

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class PipeFrameExporter extends FrameExporter {
    
    private static final Logger L = LogManager.getLogger();
    
    private Process proc;
    private WritableByteChannel pipe;

    private OutputStream log;

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
        
        List<String> cmds = new ArrayList<String>();
        cmds.add(cfg.videoEncoderPath.get());
        cmds.addAll(Arrays.asList(StringUtils.split(params, ' ')));
        
        // build encoder process
        ProcessBuilder pb = new ProcessBuilder(cmds);
        pb.directory(cfg.getMovieDir());
        proc = pb.start();
                
        // Java 1.6 doesn't know redirectOutput/redirectError and these
        // streams need to be emptied to avoid blocking
        log = FileUtils.openOutputStream(new File(cfg.getMovieDir(), "encoder.log"));
        new StreamPipe(proc.getInputStream(), log).start();
        new StreamPipe(proc.getErrorStream(), log).start();
        
        // create channel from output stream
        pipe = Channels.newChannel(proc.getOutputStream());
    }

    @Override
    protected void doDisable() throws Exception {
        super.doDisable();
        
        IOUtils.closeQuietly(pipe);
        IOUtils.closeQuietly(log);
        
        if (proc != null) {
            try {
                proc.waitFor();
            } catch (InterruptedException ex) {
                L.warn("Pipe program termination interrupted", ex);
            }

            proc.destroy();
        }
    }

    @Override
    public void configureCapturer(FramebufferCapturer fbc) {
        fbc.setFlipColors(false);
        fbc.setFlipLines(true);
    }

    @Override
    protected void doExportFrame(FrameCaptureEvent evt) throws Exception {
        if (pipe.isOpen()) {
            pipe.write(evt.frameBuffer);
        }
    }
}
