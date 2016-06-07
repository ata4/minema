/*
 ** 2013 April 09
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.minema;

import info.ata4.minecraft.minema.client.cmd.CommandMinema;
import info.ata4.minecraft.minema.client.config.MinemaConfig;
import info.ata4.minecraft.minema.client.modules.CaptureSession;
import java.io.File;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventBus;

/**
 * Main control class for Forge.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Mod(
    modid = Minema.ID,
    name = Minema.NAME,
    version = Minema.VERSION,
    guiFactory = "info.ata4.minecraft.minema.client.config.MinemaConfigGuiFactory"
)
public class Minema {

    public static final String NAME = "Minema";
    public static final String ID = NAME;
    public static final String VERSION = "@VERSION@";

    @Instance(ID)
    public static Minema instance;
    public static final EventBus EVENT_BUS = new EventBus();

    private ModMetadata metadata;
    private Configuration configForge;
    private MinemaConfig config;
    private CaptureSession session;

    @EventHandler
    public void onPreInit(FMLPreInitializationEvent evt) {
        File file = evt.getSuggestedConfigurationFile();
        configForge = new Configuration(file);
        config = new MinemaConfig(configForge);
        metadata = evt.getModMetadata();
    }

    @EventHandler
    public void onInit(FMLInitializationEvent evt) {
        ClientCommandHandler.instance.registerCommand(new CommandMinema(this));
        MinecraftForge.EVENT_BUS.register(new KeyHandler(this));
    }
    
    public Configuration getConfigForge() {
        return configForge;
    }

    public MinemaConfig getConfig() {
        return config;
    }

    public ModMetadata getMetadata() {
        return metadata;
    }

    public void enable() {
        session = new CaptureSession(config);
        session.enable();
    }

    public void disable() {
        if (isEnabled()) {
            session.disable();
        }
        session = null;
    }

    public boolean isEnabled() {
        return session != null && session.isEnabled();
    }
}
