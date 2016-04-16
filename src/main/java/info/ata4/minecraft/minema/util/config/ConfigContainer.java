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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.IConfigElement;

/**
 * Bridge class between Forge's weird config interface and simplified generic
 * config values.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ConfigContainer {

	private static final Logger L = LogManager.getLogger();

	private final Configuration config;
	private final Map<Pair<String, String>, Pair<ConfigValue, Property>> propMap = new LinkedHashMap<Pair<String, String>, Pair<ConfigValue, Property>>();
	private String langKeyPrefix = "";

	public ConfigContainer(final Configuration config) {
		this.config = config;
	}

	public Configuration getConfiguration() {
		return this.config;
	}

	public String getLangKeyPrefix() {
		return this.langKeyPrefix;
	}

	public void setLangKeyPrefix(final String langKeyPrefix) {
		Validate.notNull(langKeyPrefix);
		this.langKeyPrefix = langKeyPrefix;
	}

	public List<IConfigElement> getConfigElements() {
		final List<IConfigElement> list = new ArrayList<IConfigElement>();
		final Set<String> names = this.config.getCategoryNames();
		for (final String catName : names) {
			if (catName.equals(Configuration.CATEGORY_GENERAL)) {
				continue;
			}
			list.add(new ConfigElement(this.config.getCategory(catName)));
		}

		// add props in category CATEGORY_GENERAL directly to the root of the
		// list
		if (this.config.hasCategory(Configuration.CATEGORY_GENERAL)) {
			final ConfigCategory catGeneral = this.config.getCategory(Configuration.CATEGORY_GENERAL);
			final List<Property> props = catGeneral.getOrderedValues();
			for (final Property prop : props) {
				list.add(new ConfigElement(prop));
			}
		}

		return list;
	}

	public void load() {
		L.debug("Loading config");
		try {
			this.config.load();
		} catch (final Exception ex) {
			L.warn("Can't load config", ex);
		}

		// properties lose their mins/maxs/defaults/langkeys and whatsoever
		// after
		// using Configuration.load(), so re-register all ConfigValues to fix
		// that
		final Map<Pair<String, String>, Pair<ConfigValue, Property>> propMapCopy = new LinkedHashMap<Pair<String, String>, Pair<ConfigValue, Property>>(
				this.propMap);
		this.propMap.clear();

		for (final Map.Entry<Pair<String, String>, Pair<ConfigValue, Property>> propEntry : propMapCopy.entrySet()) {
			final String catName = propEntry.getKey().getLeft();
			final String propName = propEntry.getKey().getRight();
			final ConfigValue configValue = propEntry.getValue().getLeft();

			register(configValue, propName, catName);
		}
	}

	public void save() {
		L.debug("Saving config");
		try {
			if (this.config.hasChanged()) {
				this.config.save();
			}
		} catch (final Exception ex) {
			L.warn("Can't save config", ex);
		}
	}

	public void update(final boolean export) {
		L.debug("Syncing config");
		for (final Pair<ConfigValue, Property> propEntry : this.propMap.values()) {
			final ConfigValue configValue = propEntry.getLeft();
			final Property prop = propEntry.getRight();

			if (export) {
				configValue.exportProp(prop);
			} else {
				configValue.importProp(prop);
			}
		}

		save();
	}

	protected void register(final ConfigValue configValue, final String propName, final String catName) {
		// set category language key and description
		final String catLangKey = this.langKeyPrefix + "." + catName;
		final String catDesc = WordUtils.wrap(I18n.format(catLangKey + ".tooltip"), 128);

		// configure category and add property
		final ConfigCategory cat = this.config.getCategory(catName);
		cat.setLanguageKey(catLangKey);
		cat.setComment(catDesc);

		// set property language key and description
		final String propLangKey = this.langKeyPrefix + "." + propName;
		final String propDesc = WordUtils.wrap(I18n.format(propLangKey + ".tooltip"), 128);

		// get or create prop and configure it
		final String propDefault = String.valueOf(configValue.getDefault());
		final Property.Type propType = configValue.getPropType();
		final Property prop = this.config.get(catName, propName, propDefault, propDesc, propType);
		prop.setLanguageKey(propLangKey);

		// import and export prop to sync settings and values
		configValue.importProp(prop);
		configValue.exportProp(prop);

		// add to internal category map
		final Pair<String, String> mapKey = new ImmutablePair<String, String>(catName, propName);
		final Pair<ConfigValue, Property> mapValue = new ImmutablePair<ConfigValue, Property>(configValue, prop);
		this.propMap.put(mapKey, mapValue);

		// using insertion order for properties
		final List<String> propertyOrder = new ArrayList<String>();
		for (final Pair<String, String> propEntry : this.propMap.keySet()) {
			if (propEntry.getLeft().equals(catName)) {
				propertyOrder.add(propEntry.getRight());
			}
		}
		cat.setPropertyOrder(propertyOrder);

		L.debug("Registered prop {}.{} of type {}", catName, propName, propType);
	}
}
