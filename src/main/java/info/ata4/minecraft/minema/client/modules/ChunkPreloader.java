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

import java.util.Iterator;
import java.util.Set;

import info.ata4.minecraft.minema.client.config.MinemaConfig;
import info.ata4.minecraft.minema.util.reflection.PrivateFields;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ChunkPreloader extends CaptureModule {
    
    private static final Minecraft MC = Minecraft.getMinecraft();
    
    private Set<RenderChunk> chunksToUpdate;
    private ChunkRenderDispatcher renderDispatcher;

    public ChunkPreloader(MinemaConfig cfg) {
        super(cfg);
    }
    
    @SubscribeEvent
    public void onTick(RenderTickEvent evt) {
        if (evt.phase != Phase.START) {
            return;
        }
        
        if (chunksToUpdate == null || renderDispatcher == null) {
            return;
        }
        
        // TODO: this only loads local client chunks and doesn't load missing
        // chunks from the server
        Iterator<RenderChunk> iterator = chunksToUpdate.iterator();
        while (iterator.hasNext()) {
            RenderChunk renderChunk = iterator.next();
            renderDispatcher.updateChunkNow(renderChunk);
            renderChunk.setNeedsUpdate(false);
            iterator.remove();
        }
    }

    @Override
    protected void doEnable() throws Exception {
        try {
            chunksToUpdate = ReflectionHelper.getPrivateValue(RenderGlobal.class, MC.renderGlobal, PrivateFields.RENDERGLOBAL_CHUNKSTOUPDATE);
        } catch (Exception ex) {
            throw new RuntimeException("Can't get chunksToUpdate field", ex);
        }
        
        try {
            renderDispatcher = ReflectionHelper.getPrivateValue(RenderGlobal.class, MC.renderGlobal, PrivateFields.RENDERGLOBAL_RENDERDISPATCHER);
        } catch (Exception ex) {
            throw new RuntimeException("Can't get renderDispatcher field", ex);
        }
        
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void doDisable() throws Exception {
    	MinecraftForge.EVENT_BUS.unregister(this);
    }
    
}
