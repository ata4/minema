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
import net.minecraftforge.fml.client.config.GuiConfig;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class MinemaConfigGui extends GuiConfig {
    
    public MinemaConfigGui(GuiScreen parentScreen) {
        super(parentScreen, Minema.instance.getConfig().getConfigElements(),
            Minema.ID, false, false, GuiConfig.getAbridgedConfigPath(
                Minema.instance.getConfig().getConfiguration().toString()
            ));
    }

}
