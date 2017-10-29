package com.github.liebharc.rsettings.immutable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.github.liebharc.rsettings.StateInitException;

public class Register {
	private final List<ReadSetting<?>> settings;
	
	public Register() {
		settings = new ArrayList<>();
	}
	
	public<TValue, TSetting extends ReadSetting<TValue>> TSetting add(TSetting setting) {
		List<String> missingDependencies = 
			setting.getDependencies()
				.asList()
				.stream()
				.filter(s -> !settings.contains(s) && !Placeholder.isPlaceholder(s))
				.map(s -> s.getClass().getName())
				.collect(Collectors.toList());
		if (!missingDependencies.isEmpty()) {
			throw new StateInitException(
					"Setting depends on objects which haven't been added yet. "
					+ "Settings should be added in the order in which they are created. "
					+ "Missing dependencies are of type: " 
					+ String.join(", ", missingDependencies));
		}
		settings.add(setting);
		return setting;
	}
	
	public List<ReadSetting<?>> asList() {
		return Collections.unmodifiableList(settings);
	}
}
