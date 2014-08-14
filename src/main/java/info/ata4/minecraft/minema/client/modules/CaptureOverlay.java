/*
 ** 2012 March 31
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.minema.client.modules;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import info.ata4.minecraft.minema.client.config.MinemaConfig;
import info.ata4.minecraft.minema.client.util.CaptureTime;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;

/**
 * Minema information screen overlay.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class CaptureOverlay extends CaptureModule {
    
    private static final Minecraft MC = Minecraft.getMinecraft();

    private final CaptureSession session;

    public CaptureOverlay(MinemaConfig cfg, CaptureSession session) {
        super(cfg);
        this.session = session;
    }
    
    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Text evt) {
        List<String> left = evt.left;
        
        if (MC.gameSettings.showDebugInfo) {
            left.add(null);
        }

        CaptureTime time = session.getCaptureTime();
        
        String frame = String.valueOf(time.getNumFrames());
        left.add("Frame: " + frame);
        
        String fps = MC.debug.isEmpty() ? MC.debug : MC.debug.substring(0, MC.debug.indexOf(','));
        left.add("Rate: " + fps);
        
        String avg = (int) time.getAverageFPS() + " fps";
        left.add("Avg.: " + avg);
        
        String delay = CaptureTime.getTimeUnit(time.getPreviousCaptureTime());
        left.add("Delay: " + delay);
        
        left.add("Time R: " + time.getRealTimeString());
        left.add("Time V: " + time.getVideoTimeString());
    }
    
    @Override
    protected void doEnable() throws Exception {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void doDisable() throws Exception {
        MinecraftForge.EVENT_BUS.unregister(this);
    }
}
