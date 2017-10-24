package com.github.liebharc.rsettings.immutable;

import java.util.*;

/**
 * A builder to create a list of changed settings. If the same setting is changed twice by an user
 * then only the last change will be recorded since the first change never takes effect if it gets immediately 
 * overwritten.
 */
public class SettingsChangeListBuilder {

	private List<ReadSetting<?>> changes;
	
	public void add(ReadSetting<?> setting) {
		if (changes.contains(setting)) {
			changes.remove(setting);
		}
		
		changes.add(setting);
	}
	
	public SettingsChangeListBuilder(Collection<ReadSetting<?>> settings) {
		 changes = new ArrayList<>(settings);
	}
	
	public List<ReadSetting<?>> build() {
		return makeImmutable(changes);
	}
	
	private List<ReadSetting<?>> makeImmutable(List<ReadSetting<?>> list) {
		return Collections.unmodifiableList(list);
	}
}
