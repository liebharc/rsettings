package com.github.liebharc.rsettings.immutable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.liebharc.rsettings.StateInitException;

/**
 * This class is responsible to detect the dependencies between settings and to provide
 * other classes with the information on which settings depend on a given setting. 
 */
final class DependencyGraph {
	
	private final List<ReadSetting<?>> settings;
	
	private final Map<ReadSetting<?>, List<ReadSetting<?>>> settingDependencies = new HashMap<>();
	
	public DependencyGraph(List<ReadSetting<?>> settings) {
		this.settings = settings;
		for (ReadSetting<?> setting : settings) {
			settingDependencies.put(setting, new ArrayList<>());
		}
		
		for (ReadSetting<?> setting : settings) {
			List<ReadSetting<?>> dependencies = replacePlaceholders(setting.getDependencies().asList());
			for (ReadSetting<?> source : dependencies) {
				if (!settingDependencies.containsKey(source)) {
					throw new StateInitException("Failed to resolve dependency to " + source.getClass().getName());
				}
				
				settingDependencies.get(source).add(setting);
			}
		}
		
		for (ReadSetting<?> setting : settings) {
			settingDependencies.replace(
					setting, 
					Collections.unmodifiableList(settingDependencies.get(setting)));
		}
	}
	
	private List<ReadSetting<?>> replacePlaceholders(List<ReadSetting<?>> settings) {
		return settings.stream().map(s -> {
				if (Placeholder.isPlaceholder(s)) {return this.settings.stream()
							.filter(r -> r.equals(s))
							.findFirst()
							.get();
				} else {
					return s;
				}
			})
			.collect(Collectors.toList());
	}
	
	/**
	 * Gets all settings which depend on the given setting and therefore must be updated.
	 * @param setting A setting.
	 * @return All settings which depend on the given setting.
	 */
	public List<ReadSetting<?>> getDependencies(ReadSetting<?> setting) {
		return settingDependencies.get(setting);
	}
}
