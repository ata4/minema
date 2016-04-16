/*
 ** 2014 August 10
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.minema.util.config;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class ConfigNumber<T extends Number & Comparable<T>> extends ConfigValue<T> {

	private final T min;
	private final T max;

	public ConfigNumber(final T value, final T min, final T max) {
		super(value);
		this.min = min;
		this.max = max;
	}

	public ConfigNumber(final T value, final T min) {
		this(value, min, null);
	}

	@Override
	public void set(final T value) {
		if (this.min != null && this.min.compareTo(value) > 0) {
			super.set(this.min);
		} else if (this.max != null && this.max.compareTo(value) < 0) {
			super.set(this.max);
		} else {
			super.set(value);
		}
	}

	public T getMin() {
		return this.min;
	}

	public T getMax() {
		return this.max;
	}
}
