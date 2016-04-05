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

	public static void print(final String msg, final TextFormatting format, final Object... args) {
		if (MC.ingameGUI == null) {
			return;
		}

		final GuiNewChat chat = MC.ingameGUI.getChatGUI();
		final TextComponentTranslation ret = new TextComponentTranslation(msg, args);
		ret.getChatStyle().setColor(format);

		chat.printChatMessage(ret);
	}

}
