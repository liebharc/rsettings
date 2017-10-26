package com.github.liebharc.rsettings.immutable;

import java.util.*;

public class Dependencies {

	private static final Dependencies NO_DEPENDENCIES = new Dependencies(new ReadSetting<?>[0]);
	
	static Dependencies empty() {
		return NO_DEPENDENCIES;
	}
	
	private List<ReadSetting<?>> dependencies;

	Dependencies(ReadSetting<?>[] dependencies) {
		this.dependencies = Collections.unmodifiableList(Arrays.asList(dependencies));
		
	}

	public List<ReadSetting<?>> asList() {
		return dependencies;
	}
}
