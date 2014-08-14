/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package info.ata4.minecraft.minema.client;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import info.ata4.minecraft.minema.Minema;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class KeyHandler {
    
    public static final String KEY_CATEGORY = "key.categories.minema";
    public static final KeyBinding KEY_CAPTURE = new KeyBinding("key.minema.capture", Keyboard.KEY_F4, KEY_CATEGORY);
    
    private final Minema minema;

    public KeyHandler(Minema minema) {
        this.minema = minema;
        
        ClientRegistry.registerKeyBinding(KEY_CAPTURE);
    }
    
    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (KEY_CAPTURE.isPressed()) {
            if (minema.isEnabled()) {
                minema.disable();
            } else {
                minema.enable();
            }
        }
    }
}
