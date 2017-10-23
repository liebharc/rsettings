package com.github.liebharc.rsettings.immutable;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.liebharc.rsettings.NetworkInitException;

final class PropertyDependencies {
	
	private final List<ReadOnlySetting<?>> properties = new ArrayList<>();
	
	private final Map<ReadOnlySetting<?>, List<ReadOnlySetting<?>>> propertyDependencies = new HashMap<>();
	
	public int getNumberOfProperties() {
		return properties.size();
	}
	
	public final <TProp extends ReadOnlySetting<TValue>, TValue> void register(TProp property) {
		properties.add(property);
		propertyDependencies.put(property, new ArrayList<>());
		List<ReadOnlySetting<?>> sources = findSources(property);
		for (ReadOnlySetting<?> source : sources) {
			propertyDependencies.get(source).add(property);
		}
	}
	
	public List<ReadOnlySetting<?>> getDependencies(ReadOnlySetting<?> property) {
		return propertyDependencies.get(property);
	}
	
	private List<ReadOnlySetting<?>> findSources(ReadOnlySetting<?> property) {
		List<ReadOnlySetting<?>> result = new ArrayList<>();
		Constructor<?>[] constructors = property.getClass().getConstructors();
		if (constructors.length != 1) {
			throw new NetworkInitException(property.getClass().getName() + " must have exactly one constructor");
		}
		
		Constructor<?> constructor = constructors[0];
		Class<?>[] ctorArguments = constructor.getParameterTypes();
		for (Class<?> argument : ctorArguments) {
			if (Setting.class.isAssignableFrom(argument)) {
				result.add(findPropertyOfType(argument));
			}
		}
		
		return result;
	}
	
	private ReadOnlySetting<?> findPropertyOfType(Class<?> type) {
		Optional<ReadOnlySetting<?>> instance = properties.stream().filter(p -> type.isInstance(p)).findFirst();
		if (instance.isPresent()) {
			return instance.get();
		}
		
		throw new NetworkInitException("Properties must be registered in the order in which they are created");
	}
}
