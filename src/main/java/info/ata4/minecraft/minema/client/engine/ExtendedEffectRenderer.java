/*
 ** 2012 April 30
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.minema.client.engine;

import java.util.List;

import info.ata4.minecraft.minema.util.reflection.PrivateFields;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/**
 * EffectRenderer with configurable particle limit.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ExtendedEffectRenderer extends EffectRenderer {

	// TODO: This is pretty much a very old derivative from original MC code (works though)
	private List<EntityFX>[][] fxLayers;
	private int particleLimit = 4000;

	public ExtendedEffectRenderer(World world, TextureManager textureManager) {
		super(world, textureManager);

		try {
			fxLayers = ReflectionHelper.getPrivateValue(EffectRenderer.class, this,
					PrivateFields.EFFECTRENDERER_FXLAYERS);
		} catch (Exception ex) {
			throw new RuntimeException("Can't get FX layers array", ex);
		}
	}

	@Override
	public void addEffect(EntityFX fx) {
		// Forge: Prevent modders from being bad and adding nulls causing
		// untraceable NPEs.
		if (fx == null) {
			return;
		}

		if (particleLimit == 0) {
			return;
		}

		int i = fx.getFXLayer();
		int k = fx.func_187111_c() ? 0 : 1;

		List<EntityFX> fxLayer = fxLayers[i][k];

		if (particleLimit > 0 && fxLayer.size() >= particleLimit) {
			fxLayer.remove(0);
		}

		fxLayer.add(fx);
	}

	public int getParticleLimit() {
		return particleLimit;
	}

	public void setParticleLimit(int particleLimit) {
		this.particleLimit = particleLimit;
	}
}
