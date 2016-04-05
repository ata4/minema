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

import java.io.File;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;

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

	public static void printFileLink(final String msg, final File file) {
		final TextComponentString text = new TextComponentString(file.getName());
		String path;

		try {
			path = file.getAbsoluteFile().getCanonicalPath();
		} catch (final IOException ex) {
			path = file.getAbsolutePath();
		}

		text.getChatStyle().setChatClickEvent(new ClickEvent(Action.OPEN_FILE, path));
		text.getChatStyle().setUnderlined(true);

		print(msg, TextFormatting.BLUE, text);
	}
}
