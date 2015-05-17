/*
 ** 2014 August 05
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.minema.client.config;

import info.ata4.minecraft.minema.Minema;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class MinemaConfigGui extends GuiConfig {
    
    private static String getTitle() {
        Configuration cfg = Minema.instance.getConfig().getConfiguration();
        return GuiConfig.getAbridgedConfigPath(cfg.toString());
    }

    public MinemaConfigGui(GuiScreen parentScreen) {
        // telescoping into space while static methods prevent worse.
        // thanks for nothing, IModGui"Factory"...
        super(parentScreen, Minema.instance.getConfig().getConfigElements(),
                Minema.ID, false, false, getTitle());
    }
    
}
