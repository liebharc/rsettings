package com.github.liebharc.rsettings.immutable;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
		List<ReadSetting<?>> sources = findSources(property);
		for (ReadSetting<?> source : sources) {
			propertyDependencies.get(source).add(property);
		}
	}
	
	public List<ReadSetting<?>> getDependencies(ReadSetting<?> property) {
		return propertyDependencies.get(property);
	}
	
	private List<ReadSetting<?>> findSources(ReadSetting<?> property) {
		List<ReadSetting<?>> result = new ArrayList<>();
		Constructor<?>[] constructors = property.getClass().getConstructors();
		if (constructors.length != 1) {
			throw new NetworkInitException(property.getClass().getName() + " must have exactly one constructor");
		}
		
		Constructor<?> constructor = constructors[0];
		Class<?>[] ctorArguments = constructor.getParameterTypes();
		for (Class<?> argument : ctorArguments) {
			if (ReadSetting.class.isAssignableFrom(argument)) {
				// TODO Need to find a better way to resolve dependencies. Just looking at the type
				// isn't sufficient. We would need to know the instance.
				result.add(findPropertyOfType(argument));
			}
		}
		
		return result;
	}
	
	private ReadSetting<?> findPropertyOfType(Class<?> type) {
		Optional<ReadSetting<?>> instance = properties.stream().filter(p -> type.isInstance(p)).findFirst();
		if (instance.isPresent()) {
			return instance.get();
		}
		
		throw new NetworkInitException("Properties must be registered in the order in which they are created");
	}
}
