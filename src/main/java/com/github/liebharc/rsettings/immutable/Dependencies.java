package com.github.liebharc.rsettings.immutable;

import java.util.*;

public class Dependencies {

	private static final Dependencies NO_DEPENDENCIES = new Dependencies(new ArrayList<>());
	
	static Dependencies empty() {
		return NO_DEPENDENCIES;
	}
	
	private List<ReadSetting<?>> dependencies;

	Dependencies(List<ReadSetting<?>> dependencies) {
		this.dependencies = Collections.unmodifiableList(dependencies);
		
	}

	public List<ReadSetting<?>> asList() {
		return dependencies;
	}
}
