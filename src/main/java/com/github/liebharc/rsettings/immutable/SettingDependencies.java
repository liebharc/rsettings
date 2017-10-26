package com.github.liebharc.rsettings.immutable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.liebharc.rsettings.StateInitException;

/**
 * This class is responsible to detect the dependencies between settings and to provide
 * other classes with the information on which settings depend on a given setting. 
 */
final class SettingDependencies {
	
	private final List<ReadSetting<?>> settings = new ArrayList<>();
	
	private final Map<ReadSetting<?>, List<ReadSetting<?>>> settingDependencies = new HashMap<>();
	
	/**
	 * Adds a setting to the dependency tree.
	 * @param setting A setting
	 * @throws StateInitException if a dependency to an unknown setting exists
	 */
	public final <TSetting extends ReadSetting<TValue>, TValue> void register(TSetting setting) {
		settings.add(setting);
		settingDependencies.put(setting, new ArrayList<>());
		List<ReadSetting<?>> sources = findDependencies(setting);
		for (ReadSetting<?> source : sources) {
			if (!isFutureSetting(source))
				settingDependencies.get(source).add(setting);
		}
	}
	
	/**
	 * Gets all settings which depend on the given setting and therefore must be updated.
	 * @param setting A setting.
	 * @return All settings which depend on the given setting.
	 */
	public List<ReadSetting<?>> getDependencies(ReadSetting<?> setting) {
		return settingDependencies.get(setting);
	}
	
	private List<ReadSetting<?>> findDependencies(ReadSetting<?> setting) {
		List<ReadSetting<?>> dependencies = setting.getDependencies();
	    for (ReadSetting<?> dependency : dependencies) {
	    	boolean weShouldSeeThisSettingInFuture = isFutureSetting(dependency);
			if (!settings.contains(dependency) && !weShouldSeeThisSettingInFuture) {
				throw new StateInitException("Setting must be registered in the order in which they are created");
			}
		}
		return setting.getDependencies();
	}
	
	private boolean isFutureSetting(ReadSetting<?> setting) {
		return setting instanceof FutureSetting<?>;
	}
}
