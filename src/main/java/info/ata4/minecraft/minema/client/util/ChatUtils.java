/*
 ** 2014 July 29
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.minema.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ChatUtils {

    private static final Minecraft MC = Minecraft.getMinecraft();

    public static void print(String msg, TextFormatting format, Object... args) {
        if (MC.ingameGUI == null) {
            return;
        }

        GuiNewChat chat = MC.ingameGUI.getChatGUI();
        TextComponentTranslation ret = new TextComponentTranslation(msg, args);
        ret.getStyle().setColor(format);

        chat.printChatMessage(ret);
    }

}
