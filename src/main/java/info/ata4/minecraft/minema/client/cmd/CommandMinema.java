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
import info.ata4.minecraft.minema.client.modules.DisplaySizeModifier;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class CommandMinema extends CommandBase {
    
    private final Minema minema;

    public CommandMinema(Minema minema) {
        this.minema = minema;
    }
    
    @Override
    public String getCommandName() {
        return "minema";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "commands.minema.usage";
    }
    
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            throw new WrongUsageException(getCommandUsage(sender));
        }
        
        String cmd = args[0];
        
        if (cmd.equals("enable")) {
            minema.enable();
        } else if (cmd.equals("disable")) {
            minema.disable();
        } else if (cmd.equals("resize")) {
            DisplaySizeModifier modifier = new DisplaySizeModifier(minema.getConfig());
            
            int width = Integer.parseInt(args[1]);
            int height = Integer.parseInt(args[2]);
            int mode = args.length == 4 ? Integer.parseInt(args[3]) : 0;
            
            modifier.resize(width, height);
            
            switch (mode) {
                case 0:
                    try {
                        Display.setDisplayMode(new DisplayMode(width, height));
                    } catch (LWJGLException ex) {
                        throw new RuntimeException("Can't resize LWJGL display", ex);
                    }
                    break;
                
                case 1:
                    modifier.setFramebufferTextureSize(
                            Math.max(width, Display.getWidth()),
                            Math.max(height, Display.getHeight()));
                    break;
            }
        } else {
            throw new WrongUsageException(getCommandUsage(sender));
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
    
}
