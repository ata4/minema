/*
 ** 2014 July 28
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.minema.client.modules;

import net.minecraft.client.stream.NullStream;

/**
 * Minema interface for IStream.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class CaptureStream extends NullStream {
    
    private final CaptureSession session;
    
    public CaptureStream(CaptureSession session) {
        super(null);
        this.session = session;
    }
    
    // submitFrame
    @Override
    public void func_152922_k() {
        session.captureFrame();
    }

    // getIsBroadcasting
    @Override
    public boolean func_152934_n() {
        return session.isEnabled();
    }

    // getIsPaused
    @Override
    public boolean func_152919_o() {
        return session.isPaused();
    }

    // pauseBroadcasting
    @Override
    public void func_152916_q() {
        session.setPaused(true);
    }

    // resumeBroadcasting
    @Override
    public void func_152933_r() {
        session.setPaused(false);
    }

    // isMuted
    @Override
    public boolean func_152929_G() {
        // Minema doesn't record audio
        return true;
    }
}
