package com.github.liebharc.rsettings.immutable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.github.liebharc.rsettings.CheckFailedException;

public abstract class ReadSetting<T> {
	private T defaultValue;
	
	private List<ReadSetting<?>> dependencies;
	
	public ReadSetting(T defaultValue, ReadSetting<?>... dependencies) {
		this.defaultValue = defaultValue;
		this.dependencies = Arrays.asList(dependencies); 
	}
	
	public final T getDefaultValue() {
		return defaultValue;
	}
	
	public Optional<T> update(SettingState state) throws CheckFailedException {
		return Optional.empty();
	}
	
	List<ReadSetting<?>> getDependencies() {
		return this.dependencies;
	}
}
