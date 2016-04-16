/*
 ** 2014 August 01
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.minema.client.modules;

import info.ata4.minecraft.minema.client.config.MinemaConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class GameSettingsModifier extends ACaptureModule {

	private static final Minecraft MC = Minecraft.getMinecraft();

	private int framerateLimit;
	private boolean vSync;
	private boolean pauseOnLostFocus;

	public GameSettingsModifier(final MinemaConfig cfg) {
		super(cfg);
	}

	@Override
	protected void doEnable() throws Exception {
		final GameSettings gs = MC.gameSettings;

		// disable build-in framerate limit
		this.framerateLimit = gs.limitFramerate;
		gs.limitFramerate = Integer.MAX_VALUE;

		// disable vSync
		this.vSync = gs.enableVsync;
		gs.enableVsync = false;

		// don't pause when losing focus
		this.pauseOnLostFocus = gs.pauseOnLostFocus;
		gs.pauseOnLostFocus = false;

	}

	@Override
	protected void doDisable() throws Exception {
		// restore everything
		final GameSettings gs = MC.gameSettings;
		gs.limitFramerate = this.framerateLimit;
		gs.pauseOnLostFocus = this.pauseOnLostFocus;
		gs.enableVsync = this.vSync;
	}

}
