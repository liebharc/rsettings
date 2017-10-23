package com.github.liebharc.rsettings.immutable;

import java.util.*;

import com.google.common.collect.ImmutableList;

public class SettingsChangeListBuilder {

	private List<ReadOnlySetting<?>> changes;
	
	public void add(ReadOnlySetting<?> setting) {
		if (changes.contains(setting)) {
			changes.remove(setting);
		}
		
		changes.add(setting);
	}
	
	public SettingsChangeListBuilder(Collection<ReadOnlySetting<?>> settings) {
		 changes = new ArrayList<>(settings);
	}
	
	public List<ReadOnlySetting<?>> build() {
		return makeImmutable(changes);
	}
	
	private List<ReadOnlySetting<?>> makeImmutable(List<ReadOnlySetting<?>> list) {
		ImmutableList.Builder<ReadOnlySetting<?>> immutable = new ImmutableList.Builder<ReadOnlySetting<?>>();
		immutable.addAll(list);
		return immutable.build();
	}
}
