package com.github.liebharc.rsettings.immutable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.liebharc.rsettings.NetworkInitException;

final class PropertyDependencies {
	
	private final List<ReadSetting<?>> properties = new ArrayList<>();
	
	private final Map<ReadSetting<?>, List<ReadSetting<?>>> propertyDependencies = new HashMap<>();
	
	public int getNumberOfProperties() {
		return properties.size();
	}
	
	public final <TProp extends ReadSetting<TValue>, TValue> void register(TProp property) {
		properties.add(property);
		propertyDependencies.put(property, new ArrayList<>());
		List<ReadSetting<?>> sources = findDependencies(property);
		for (ReadSetting<?> source : sources) {
			propertyDependencies.get(source).add(property);
		}
	}
	
	public List<ReadSetting<?>> getDependencies(ReadSetting<?> property) {
		return propertyDependencies.get(property);
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
