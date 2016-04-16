/*
 ** 2014 July 28
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */

package info.ata4.minecraft.minema.client.cmd;

import info.ata4.minecraft.minema.Minema;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class CommandMinema extends CommandBase {

	private final Minema minema;

	public CommandMinema(final Minema minema) {
		this.minema = minema;
	}

	@Override
	public String getCommandName() {
		return "minema";
	}

	@Override
	public String getCommandUsage(final ICommandSender sender) {
		return "commands.minema.usage";
	}

	@Override
	public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args)
			throws CommandException {
		if (args.length == 0) {
			throw new WrongUsageException(getCommandUsage(sender));
		}

		final String cmd = args[0];

		if (cmd.equals("enable")) {
			this.minema.enable();
		} else if (cmd.equals("disable")) {
			this.minema.disable();
		} else {
			throw new WrongUsageException(getCommandUsage(sender));
		}
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

}
