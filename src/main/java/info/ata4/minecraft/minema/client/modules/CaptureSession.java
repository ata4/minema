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

import info.ata4.minecraft.minema.client.modules.modifiers.DisplaySizeModifier;
import info.ata4.minecraft.minema.client.modules.modifiers.GameSettingsModifier;
import info.ata4.minecraft.minema.client.modules.exporters.FrameExporter;
import info.ata4.minecraft.minema.client.modules.exporters.ImageFrameExporter;
import info.ata4.minecraft.minema.client.modules.exporters.PipeFrameExporter;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import info.ata4.minecraft.minema.client.capture.Capturer;
import info.ata4.minecraft.minema.client.config.MinemaConfig;
import info.ata4.minecraft.minema.client.event.FrameCaptureEvent;
import info.ata4.minecraft.minema.client.event.FramePreCaptureEvent;
import info.ata4.minecraft.minema.client.util.CaptureTime;
import info.ata4.minecraft.minema.client.util.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class CaptureSession extends CaptureModule {

    public static Logger L = LogManager.getLogger();
    public static Minecraft MC = Minecraft.getMinecraft();

    private final ArrayList<CaptureModule> modules = new ArrayList<>();
    private final EventBus eventBus = new EventBus();

    private CaptureTime time;
    private Capturer capturer;

    private File movieDir;

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

        FrameExporter exporter;
        if (cfg.useVideoEncoder.get()) {
            exporter = new PipeFrameExporter(cfg);
        } else {
            exporter = new ImageFrameExporter(cfg);
        }
        modules.add(exporter);

        if (cfg.showOverlay.get()) {
            modules.add(new CaptureOverlay(cfg, this));
        }

        // enable and register modules
        modules.forEach(module -> {
            eventBus.register(module);
            module.enable();
        });
        MinecraftForge.EVENT_BUS.register(this);

        // reset capturing stats
        time = new CaptureTime(cfg);

        // configure framebuffer capturer
        capturer = new Capturer();
        exporter.configureCapturer(capturer);

        playChickenPlop();
    }

    @Override
    protected void doDisable() {
        capturer.close();

        // disable and unregister modules
        modules.forEach(module -> {
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
        });

        MinecraftForge.EVENT_BUS.unregister(this);

        modules.clear();

        // delete empty movie dir
        if (movieDir != null && movieDir.exists() && movieDir.list().length == 0) {
            movieDir.delete();
        }
        cfg.setMovieDir(null);
    }

    @Override
    protected void handleError(Throwable throwable) {
        ChatUtils.print("minema.error.label", TextFormatting.RED);

        // get list of throwables and their causes
        List<Throwable> throwables = new ArrayList<>();
        do {
            throwables.add(throwable);
            throwable = throwable.getCause();
        } while (throwable != null);
        
        throwables.stream().filter(t -> {
            String message = t.getMessage();

            // skip wrapped exceptions
            if (message == null) {
                return false;
            }
            
            // skip wrapped exceptions with generated messages
            Throwable cause = t.getCause();
            return cause == null || !message.equals(cause.toString());
        }).forEach(t -> ChatUtils.print(t.getMessage(), TextFormatting.RED));
    }

    @SubscribeEvent
    public void captureFrame(RenderTickEvent e) {
        if (!isEnabled()) {
            return;
        }
        if (e.phase == Phase.START) {
            // Only record at the end of the frame (fixes recording two images
            // per frame)
            return;
        }

        // skip frames if the capturing framerate is not synchronized with the
        // rendering framerate
        if (!cfg.isSyncEngine() && !time.isNextFrame()) {
            // Game renders faster than necessary for recording?
            return;
        }

        try {
            if (eventBus.post(new FramePreCaptureEvent(time.getNumFrames(),
                    capturer.getCaptureDimension()))) {
                throw new RuntimeException("Frame capturing cancelled at frame " + time.getNumFrames());
            }

            if (eventBus.post(new FrameCaptureEvent(time.getNumFrames(),
                    capturer.getCaptureDimension(), capturer.capture()))) {
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

    private void playChickenPlop() {
        try {
            MC.theWorld.playSound(MC.thePlayer, MC.thePlayer.playerLocation, SoundEvents.entity_chicken_egg,
                    SoundCategory.NEUTRAL, 1, 1);
        } catch (Exception e) {
            L.error("cannot play chicken plop", e);
        }
    }

    public CaptureTime getCaptureTime() {
        return time;
    }
}
