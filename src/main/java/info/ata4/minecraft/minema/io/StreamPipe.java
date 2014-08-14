/*
 ** 2014 July 29
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.minema.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class StreamPipe extends Thread {
    
    private final InputStream is;
    private final OutputStream os;
    
    public StreamPipe(InputStream is, OutputStream os) {
        super("StreamPipe");
        this.is = is;
        this.os = os;
    }

    @Override
    public void run() {
        try {
            IOUtils.copy(is, os);
        } catch (IOException ex) {
            // probably one of the streams was closed, ignore
        }
    }
}
