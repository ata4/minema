/*
 ** 2013 May 13
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.minema.client.util;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import info.ata4.minecraft.minema.client.config.MinemaConfig;

/**
 * Utility class to keep track of various time-related information during a
 * capture.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class CaptureTime {

	private static final Map<TimeUnit, String> TU_SUFFIX;

	static {
		final Map<TimeUnit, String> tuSuffix = new EnumMap<TimeUnit, String>(TimeUnit.class);
		tuSuffix.put(TimeUnit.DAYS, "d");
		tuSuffix.put(TimeUnit.HOURS, "h");
		tuSuffix.put(TimeUnit.MINUTES, "m");
		tuSuffix.put(TimeUnit.SECONDS, "s");
		tuSuffix.put(TimeUnit.MILLISECONDS, "ms");
		tuSuffix.put(TimeUnit.MICROSECONDS, "µs");
		tuSuffix.put(TimeUnit.NANOSECONDS, "ns");
		TU_SUFFIX = Collections.unmodifiableMap(tuSuffix);
	}

	public static String getTimeUnit(final long nanos) {
		TimeUnit tu = null;
		final TimeUnit[] tus = TimeUnit.values();
		long time = nanos;

		for (int i = tus.length - 1; i >= 0; i--) {
			tu = tus[i];
			time = tu.convert(nanos, TimeUnit.NANOSECONDS);
			if (time > 1) {
				break;
			}
		}

		return time + TU_SUFFIX.get(tu);
	}

	public static String getTimeStringFull(final long nanos) {
		final long hours = TimeUnit.NANOSECONDS.toHours(nanos);
		final long minutes = TimeUnit.NANOSECONDS.toMinutes(nanos) - TimeUnit.HOURS.toMinutes(hours);
		final long seconds = TimeUnit.NANOSECONDS.toSeconds(nanos) - TimeUnit.MINUTES.toSeconds(minutes)
				- TimeUnit.HOURS.toSeconds(hours);
		final long milis = TimeUnit.NANOSECONDS.toMillis(nanos) - TimeUnit.SECONDS.toMillis(seconds)
				- TimeUnit.MINUTES.toMillis(minutes) - TimeUnit.HOURS.toMillis(hours);

		return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, milis);
	}

	public static String getTimeStringSimple(final long nanos) {
		final long hours = TimeUnit.NANOSECONDS.toHours(nanos);
		final long minutes = TimeUnit.NANOSECONDS.toMinutes(nanos) - TimeUnit.HOURS.toMinutes(hours);
		final long seconds = TimeUnit.NANOSECONDS.toSeconds(nanos) - TimeUnit.MINUTES.toSeconds(minutes)
				- TimeUnit.HOURS.toSeconds(hours);

		return String.format("%dh %dm %ds", hours, minutes, seconds);
	}

	private final MinemaConfig cfg;
	private final long startTime;
	private long currentFrameTime;
	private long prevFrameTime;
	private final long nanosPerFrame;
	private int frames;

	public CaptureTime(final MinemaConfig cfg) {
		this.cfg = cfg;
		this.nanosPerFrame = (long) (TimeUnit.SECONDS.toNanos(1) / cfg.frameRate.get());
		this.startTime = this.currentFrameTime = this.prevFrameTime = System.nanoTime();
	}

	public void nextFrame() {
		this.prevFrameTime = this.currentFrameTime;
		this.currentFrameTime = System.nanoTime();
		this.frames++;
	}

	public int getNumFrames() {
		return this.frames;
	}

	public boolean isAtFrameLimit() {
		final int frameLimit = this.cfg.frameLimit.get();
		return frameLimit > 0 && this.frames > frameLimit;
	}

	public boolean isNextFrame() {
		return getTimeSincePreviousFrame() >= this.nanosPerFrame;
	}

	public long getPreviousCaptureTime() {
		return this.currentFrameTime - this.prevFrameTime;
	}

	public long getTimeSincePreviousFrame() {
		return System.nanoTime() - this.prevFrameTime;
	}

	public long getStartTime() {
		return this.startTime;
	}

	public long getRealTime() {
		return System.nanoTime() - this.startTime;
	}

	public String getRealTimeString() {
		return getTimeStringFull(getRealTime());
	}

	public long getVideoTime() {
		return this.frames * this.nanosPerFrame;
	}

	public String getVideoTimeString() {
		return getTimeStringFull(getVideoTime());
	}

	public double getAverageFPS() {
		return TimeUnit.SECONDS.toNanos(1) / ((double) getRealTime() / (double) getNumFrames());
	}
}
