/*
 ** 2014 August 01
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.minema.client.modules;

import info.ata4.minecraft.minema.client.config.MinemaConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.opengl.Display;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class GameSettingsModifier extends CaptureModule {
    
    private static final Minecraft MC = Minecraft.getMinecraft();
    
    private int limitFramerate;
    private boolean pauseOnLostFocus;
    
    public GameSettingsModifier(MinemaConfig cfg) {
        super(cfg);
    }

    @Override
    protected void doEnable() throws Exception {
        GameSettings gs = MC.gameSettings;
        
        // disable build-in framerate limit
        limitFramerate = gs.limitFramerate;
        gs.limitFramerate = Integer.MAX_VALUE;
        
        // don't pause when losing focus
        pauseOnLostFocus = gs.pauseOnLostFocus;
        gs.pauseOnLostFocus = false;
        
    }

    @Override
    protected void doDisable() throws Exception {
        // restore game settings
        GameSettings gs = MC.gameSettings;
        gs.limitFramerate = limitFramerate;
        gs.pauseOnLostFocus = pauseOnLostFocus;
    }
    
}
