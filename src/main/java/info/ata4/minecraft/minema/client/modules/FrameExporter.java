/*
 ** 2014 August 04
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.minema.client.modules;

import info.ata4.minecraft.minema.client.capture.FramebufferCapturer;
import info.ata4.minecraft.minema.client.config.MinemaConfig;
import info.ata4.minecraft.minema.client.event.FrameCaptureEvent;
import info.ata4.minecraft.minema.client.event.FramePreCaptureEvent;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class FrameExporter extends CaptureModule {
    
    private static final Logger L = LogManager.getLogger();
    
    protected ExecutorService exportService;
    protected Future exportFuture;
    
    private boolean concurrent = true;

    public FrameExporter(MinemaConfig cfg) {
        super(cfg);
    }
    
    public void setConcurrent(boolean threaded) {
        if (isEnabled()) {
            throw new IllegalStateException("Can't change concurrent setting while module is active!");
        }
        this.concurrent = threaded;
    }

    public boolean isConcurrent() {
        return concurrent;
    }
    
    @Override
    protected void doEnable() throws Exception {
        if (isConcurrent()) {
            exportService = Executors.newSingleThreadExecutor();
        }
    }

    @Override
    protected void doDisable() throws Exception {
        if (isConcurrent()) {
            exportService.shutdown();

            try {
                if (!exportService.awaitTermination(3, TimeUnit.SECONDS)) {
                    L.warn("Frame export service termination timeout");
                    exportService.shutdownNow();
                }
            } catch (InterruptedException ex) {
                L.warn("Frame export service termination interrupted", ex);
            }
        }
    }

    @SubscribeEvent
    public void onFramePreCapture(FramePreCaptureEvent evt) throws ExecutionException {
        if (!isEnabled()) {
            return;
        }
        
        if (isConcurrent() && exportFuture != null) {
            // wait for the previous task to complete before sending the next one
            try {
                exportFuture.get();
            } catch (InterruptedException ex) {
                // catch uncritical interruption exception
                L.warn("Frame export task interrupted", ex);
            }
        }
    }
    
    @SubscribeEvent
    public void onFrameCapture(final FrameCaptureEvent evt) {
        if (!isEnabled()) {
            return;
        }
        
        if (isConcurrent()) {
            // export frame in the background so that the next frame can be rendered
            // in the meantime
            exportFuture = exportService.submit(new Runnable() {
                @Override
                public void run() {
                    exportFrame(evt);
                }
            });
        } else {
            // export frame in same thread
            exportFrame(evt);
        }
    }
    
    private void exportFrame(FrameCaptureEvent evt) {
        try {
            doExportFrame(evt);
        } catch (Exception ex) {
            throw new RuntimeException("Can't export frame " + evt.frameNum, ex);
        }
    }

    protected abstract void doExportFrame(FrameCaptureEvent evt) throws Exception;
    
    public abstract void configureCapturer(FramebufferCapturer fbc);
}
