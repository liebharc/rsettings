package com.github.liebharc.rsettings;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class PropertyDependencies {
	
	private final List<Setting<?>> properties = new ArrayList<>();
	
	private final Map<Setting<?>, List<Setting<?>>> propertyDependencies = new HashMap<>();
	
	public int getNumberOfProperties() {
		return properties.size();
	}
	
	public final <TProp extends Setting<TValue>, TValue> void register(TProp property) {
		properties.add(property);
		propertyDependencies.put(property, new ArrayList<>());
		List<Setting<?>> sources = findSources(property);
		for (Setting<?> source : sources) {
			propertyDependencies.get(source).add(property);
		}
	}
	
	public List<Setting<?>> getDependencies(Setting<?> property) {
		return propertyDependencies.get(property);
	}
	
	private List<Setting<?>> findSources(Setting<?> property) {
		List<Setting<?>> result = new ArrayList<>();
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
	
	private Setting<?> findPropertyOfType(Class<?> type) {
		Optional<Setting<?>> instance = properties.stream().filter(p -> type.isInstance(p)).findFirst();
		if (instance.isPresent()) {
			return instance.get();
		}
		
		throw new NetworkInitException("Properties must be registered in the order in which they are created");
	}
}
