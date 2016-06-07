/*
** 2016 June 04
**
** The author disclaims copyright to this source code. In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.minema.client.modules;

import info.ata4.minecraft.minema.client.config.MinemaConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class CaptureNotification extends CaptureModule {
    
    private static final Minecraft MC = Minecraft.getMinecraft();

    public CaptureNotification(MinemaConfig cfg) {
        super(cfg);
    }

    @Override
    protected void doEnable() throws Exception {
        playChickenPlop(true);
    }

    @Override
    protected void doDisable() throws Exception {
        playChickenPlop(false);
    }

    private void playChickenPlop(boolean on) {
        try {
            float pitch = on ? 1 : 0.75f;
            MC.theWorld.playSound(MC.thePlayer, MC.thePlayer.getPosition(),
                SoundEvents.ENTITY_CHICKEN_EGG, SoundCategory.NEUTRAL, 1, pitch);
        } catch (Exception ex) {
            handleWarning(ex, "Can't play chicken plop");
        }
    }
}
