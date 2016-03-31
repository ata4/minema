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

import info.ata4.minecraft.minema.client.KeyHandler;
import info.ata4.minecraft.minema.client.cmd.CommandMinema;
import info.ata4.minecraft.minema.client.config.MinemaConfig;
import info.ata4.minecraft.minema.client.modules.CaptureSession;
import java.io.File;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Main control class for Forge.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Mod(
    modid = Minema.ID,
    name = Minema.NAME,
    version = Minema.VERSION,
    useMetadata = true,
    guiFactory = "info.ata4.minecraft.minema.client.config.MinemaConfigGuiFactory"
)
public class Minema {
    
    public static final String NAME = "Minema";
    public static final String ID = NAME;
    public static final String AID = NAME.toLowerCase();
    public static final String VERSION = "1.9";
    
    @Instance(ID)
    public static Minema instance;
    
    private ModMetadata metadata;
    private MinemaConfig config;
    private CaptureSession session;
    
    public MinemaConfig getConfig() {
        return config;
    }

    public ModMetadata getMetadata() {
        return metadata;
    }
    
    public void enable() {
        config.load();
        
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
    
    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
        if (eventArgs.modID.equals(ID)) {
            config.update(false);
        }
    }

    @EventHandler
    public void onPreInit(FMLPreInitializationEvent evt) {
        File file = evt.getSuggestedConfigurationFile();
        config = new MinemaConfig(new Configuration(file));
        metadata = evt.getModMetadata();
    }
    
    @EventHandler
    public void onInit(FMLInitializationEvent evt) {
        ClientCommandHandler.instance.registerCommand(new CommandMinema(this));
        FMLCommonHandler.instance().bus().register(new KeyHandler(this));
        FMLCommonHandler.instance().bus().register(this);
    }
}
