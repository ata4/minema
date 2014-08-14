/*
 ** 2014 August 03
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.minema.client.modules;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.RenderTickEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import info.ata4.minecraft.minema.client.config.MinemaConfig;
import info.ata4.minecraft.minema.util.PrivateFields;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.WorldRenderer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ChunkPreloader extends CaptureModule {
    
    private static final Minecraft MC = Minecraft.getMinecraft();
    
    private WorldRenderer[] worldRenderers;

    public ChunkPreloader(MinemaConfig cfg) {
        super(cfg);
    }
    
    @SubscribeEvent
    public void onTick(RenderTickEvent evt) {
        if (evt.phase != Phase.START) {
            return;
        }
        
        if (worldRenderers == null) {
            return;
        }
        
        for (WorldRenderer worldRenderer : worldRenderers) {
            if (worldRenderer.isInFrustum && worldRenderer.needsUpdate) {
                worldRenderer.updateRenderer(MC.renderViewEntity);
            }
        }
    }

    @Override
    protected void doEnable() throws Exception {
        try {
            worldRenderers = ReflectionHelper.getPrivateValue(RenderGlobal.class, MC.renderGlobal, PrivateFields.RENDERGLOBAL_WORLDRENDERERS);
        } catch (Exception ex) {
            throw new RuntimeException("Can't get worldRenderers field", ex);
        }
        
        FMLCommonHandler.instance().bus().register(this);
    }

    @Override
    protected void doDisable() throws Exception {
        FMLCommonHandler.instance().bus().unregister(this);
    }
    
}
