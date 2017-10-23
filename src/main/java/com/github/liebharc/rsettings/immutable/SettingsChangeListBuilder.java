package com.github.liebharc.rsettings.immutable;

import java.util.*;

import com.google.common.collect.ImmutableList;

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
		ImmutableList.Builder<ReadSetting<?>> immutable = new ImmutableList.Builder<ReadSetting<?>>();
		immutable.addAll(list);
		return immutable.build();
	}
}
