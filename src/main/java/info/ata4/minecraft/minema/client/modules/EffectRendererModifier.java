/*
 ** 2014 July 30
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.minema.client.modules;

import cpw.mods.fml.relauncher.ReflectionHelper;
import info.ata4.minecraft.minema.client.config.MinemaConfig;
import info.ata4.minecraft.minema.client.engine.ExtendedEffectRenderer;
import info.ata4.minecraft.minema.util.PrivateFields;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EffectRendererModifier extends CaptureModule {
    
    private static final Logger L = LogManager.getLogger();
    private static final Minecraft MC = Minecraft.getMinecraft();
    
    public EffectRendererModifier(MinemaConfig cfg) {
        super(cfg);
    }

    @Override
    protected void doEnable() throws Exception {
        EffectRenderer effectRenderer = getEffectRenderer();
        
        if (effectRenderer == null) {
            return;
        }
        
        if (effectRenderer instanceof ExtendedEffectRenderer) {
            L.warn("Effect renderer is already modified!");
            return;
        }
        
        // clear particles
        effectRenderer.clearEffects(MC.theWorld);
        
        // set new effect renderer
        ExtendedEffectRenderer extEffectRenderer = new ExtendedEffectRenderer(MC.theWorld, MC.renderEngine);
        extEffectRenderer.setParticleLimit(cfg.particleLimit.get());
        setEffectRenderer(extEffectRenderer);
    }

    @Override
    protected void doDisable() throws Exception {
        EffectRenderer effectRenderer = getEffectRenderer();
        
        if (effectRenderer == null) {
            return;
        }
        
        if (!(effectRenderer instanceof ExtendedEffectRenderer)) {
            L.warn("Effect renderer is already restored!");
            return;
        }
        
        // clear particles
        effectRenderer.clearEffects(MC.theWorld);
        
        // set original effect renderer
        EffectRenderer extEffectRenderer = new EffectRenderer(MC.theWorld, MC.renderEngine);
        setEffectRenderer(extEffectRenderer);
    }

    private EffectRenderer getEffectRenderer() {
        try {
            return ReflectionHelper.getPrivateValue(Minecraft.class, MC, PrivateFields.MINECRAFT_EFFECTRENDERER);
        } catch (Exception ex) {
            throw new RuntimeException("Can't get effect renderer", ex);
        }
    }

    private void setEffectRenderer(EffectRenderer effectRenderer) {
        try {
            ReflectionHelper.setPrivateValue(Minecraft.class, MC, effectRenderer, PrivateFields.MINECRAFT_EFFECTRENDERER);
        } catch (Exception ex) {
            throw new RuntimeException("Can't set effect renderer", ex);
        }
    }
    
}
