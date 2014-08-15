/*
 ** 2014 July 28
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */

package info.ata4.minecraft.minema.util.reflection;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public interface PrivateFields {
    
    public static final String[] TIMER_TICKSPERSECOND = new String[] {"ticksPerSecond", "field_74282_a"};
    public static final String[] MINECRAFT_TIMER = new String[] {"timer", "field_71428_T"};
    public static final String[] MINECRAFT_STREAM = new String[] {"field_152353_at"};
    public static final String[] MINECRAFT_EFFECTRENDERER = new String[] {"effectRenderer", "field_71452_i"};
    public static final String[] FRAMEBUFFER_FRAMEBUFFERTEXTUREWIDTH = new String[] {"framebufferTextureWidth", "field_147622_a"};
    public static final String[] FRAMEBUFFER_FRAMEBUFFERTEXTUREHEIGHT = new String[] {"framebufferTextureHeight", "field_147620_b"};
    public static final String[] EFFECTRENDERER_FXLAYERS = new String[] {"fxLayers", "field_78876_b"};
    public static final String[] RENDERGLOBAL_WORLDRENDERERS = new String[] {"worldRenderers", "field_72765_l"};
}
