package com.github.liebharc.rsettings.immutable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.liebharc.rsettings.NetworkInitException;

/**
 * This class is responsible to detect the dependencies between settings and to provide
 * other classes with the information on which settings depend on a given setting. 
 */
final class SettingDependencies {
	
	private final List<ReadSetting<?>> properties = new ArrayList<>();
	
	private final Map<ReadSetting<?>, List<ReadSetting<?>>> propertyDependencies = new HashMap<>();
	
	/**
	 * Adds a setting to the dependency tree.
	 * @param setting A setting
	 * @throws NetworkInitException if a dependency to an unknown setting exists
	 */
	public final <TSetting extends ReadSetting<TValue>, TValue> void register(TSetting setting) {
		properties.add(setting);
		propertyDependencies.put(setting, new ArrayList<>());
		List<ReadSetting<?>> sources = findDependencies(setting);
		for (ReadSetting<?> source : sources) {
			propertyDependencies.get(source).add(setting);
		}
	}
	
	/**
	 * Gets all settings which depend on the given setting and therefore must be updated.
	 * @param setting A setting.
	 * @return All settings which depend on the given setting.
	 */
	public List<ReadSetting<?>> getDependencies(ReadSetting<?> setting) {
		return propertyDependencies.get(setting);
	}
	
	private List<ReadSetting<?>> findDependencies(ReadSetting<?> property) {
		List<ReadSetting<?>> dependencies = property.getDependencies();
	    for (ReadSetting<?> setting : dependencies) {
			if (!properties.contains(setting)) {
				throw new NetworkInitException("Properties must be registered in the order in which they are created");
			}
		}
		return property.getDependencies();
	}
}
