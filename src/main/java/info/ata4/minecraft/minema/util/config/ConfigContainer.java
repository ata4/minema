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

import cpw.mods.fml.client.config.IConfigElement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    
    public ConfigContainer(Configuration config) {
        this.config = config;
    }

    public Configuration getConfiguration() {
        return config;
    }
    
    public String getLangKeyPrefix() {
        return langKeyPrefix;
    }

    public void setLangKeyPrefix(String langKeyPrefix) {
        Validate.notNull(langKeyPrefix);
        this.langKeyPrefix = langKeyPrefix;
    }
    
    public List<IConfigElement> getConfigElements() {
        List<IConfigElement> list = new ArrayList<IConfigElement>();
        Set<String> names = config.getCategoryNames();
        for (String catName : names) {
            list.add(new ConfigElement(config.getCategory(catName)));
        }
        return list;
    }
    
    public void load() {
        L.debug("Loading config");
        try {
            config.load();
        } catch (Exception ex) {
            L.warn("Can't load config", ex);
        }
        
        // properties lose their mins/maxs/defaults/langkeys and whatsoever after
        // using Configuration.load(), so re-register all ConfigValues to fix that
        Map<Pair<String, String>, Pair<ConfigValue, Property>> propMapCopy = new LinkedHashMap<Pair<String, String>, Pair<ConfigValue, Property>>(propMap);
        propMap.clear();
        
        for (Map.Entry<Pair<String, String>, Pair<ConfigValue, Property>> propEntry : propMapCopy.entrySet()) {
            String catName = propEntry.getKey().getLeft();
            String propName = propEntry.getKey().getRight();
            ConfigValue configValue = propEntry.getValue().getLeft();
            
            register(configValue, propName, catName);
        }
    }
    
    public void save() {
        L.debug("Saving config");
        try {
            if (config.hasChanged()) {
                config.save();
            }
        } catch (Exception ex) {
            L.warn("Can't save config", ex);
        }
    }
    
    public void sync() {
        L.debug("Syncing config");
        for (Map.Entry<Pair<String, String>, Pair<ConfigValue, Property>> propEntry : propMap.entrySet()) {
            String catName = propEntry.getKey().getLeft();
            String propName = propEntry.getKey().getRight();
            
            ConfigValue configValue = propEntry.getValue().getLeft();
            Property prop = propEntry.getValue().getRight();
            
            // TODO
            if (prop.isList()) {
                L.warn("Prop {}.{} is a list, which isn't supported yet", catName, propName);
                continue;
            }
            
            // set value by property type
            switch (prop.getType()) {
                case INTEGER:
                    configValue.set(prop.getInt());
                    break;
                    
                case DOUBLE:
                    configValue.set(prop.getDouble());
                    break;
                    
                case BOOLEAN:
                    configValue.set(prop.getBoolean());
                    break;
                    
                case STRING:
                    configValue.set(prop.getString());
                    break;
                    
                default:
                    L.warn("Unsupported prop type {} for {}.{}", prop.getType(), catName, propName);
            }
        }
        
        save();
    }
    
    protected void register(ConfigValue configValue, String propName, String catName) {
        Object value = configValue.get();
        
        // TODO
        if (value instanceof List) {
            L.warn("Prop {}.{} is a list, which isn't supported yet", catName, propName);
            return;
        }
        
        // get property type from object type
        Property.Type type = getPropertyType(value);
        if (type == null) {
            L.warn("Unsupported prop type {} for {}.{}", value.getClass().getSimpleName(), catName, propName);
            return;
        }
        
        // set category language key and description
        String catLangKey = langKeyPrefix + "." + catName;
        String catDesc = WordUtils.wrap(I18n.format(catLangKey + ".tooltip"), 128);

        // configure category and add property
        ConfigCategory cat = config.getCategory(catName);
        cat.setLanguageKey(catLangKey);
        cat.setComment(catDesc);
        
        // set property language key and description
        String propLangKey = langKeyPrefix + "." + propName;
        String propDesc = WordUtils.wrap(I18n.format(propLangKey + ".tooltip"), 128);
        
        // get or create prop and configure it
        Property prop = config.get(catName, propName, String.valueOf(value), propDesc, type);
        prop.setLanguageKey(propLangKey);
        
        if (configValue instanceof ConfigNumber) {
            // set min/max number values
            ConfigNumber configNum = (ConfigNumber) configValue;
            Number min = configNum.getMin();
            Number max = configNum.getMax();
            
            // only integer and double are supported, although short/byte/float
            // could theoretically work, too
            if (value instanceof Integer) {
                if (min != null) {
                    prop.setMinValue(min.intValue());
                }
                if (max != null) {
                    prop.setMaxValue(max.intValue());
                }
            } else if (value instanceof Double) {
                if (min != null) {
                    prop.setMinValue(min.doubleValue());
                }
                if (max != null) {
                    prop.setMaxValue(max.doubleValue());
                }
            } else {
                L.warn("Unsupported number type {} for {}.{}",
                        value.getClass().getSimpleName(), propName, catName);
            }
        } else if (configValue instanceof ConfigEnum) {
            // set valid values for enums
            ConfigEnum configEnum = (ConfigEnum) configValue;
            prop.setValidValues(configEnum.getChoices().toArray(new String[]{}));
        }
        
        // add to internal category map
        Pair<String, String> mapKey = new ImmutablePair<String, String>(catName, propName);
        Pair<ConfigValue, Property> mapValue = new ImmutablePair<ConfigValue, Property>(configValue, prop);
        propMap.put(mapKey, mapValue);
        
        // using insertion order for properties
        List<String> propertyOrder = new ArrayList<String>();
        for (Map.Entry<Pair<String, String>, Pair<ConfigValue, Property>> category : propMap.entrySet()) {
            if (category.getKey().getLeft().equals(catName)) {
                propertyOrder.add(category.getKey().getRight());
            }
        }
        cat.setPropertyOrder(propertyOrder);
        
        L.debug("Registered prop {}.{} of type {}", catName, propName, type);
    }
    
    private Property.Type getPropertyType(Object obj) {
        if (obj instanceof Integer) {
            return Property.Type.INTEGER;
        }
        if (obj instanceof Double) {
            return Property.Type.DOUBLE;
        }
        if (obj instanceof Boolean) {
            return Property.Type.BOOLEAN;
        }
        if (obj instanceof String) {
            return Property.Type.STRING;
        }
        return null;
    }
}
