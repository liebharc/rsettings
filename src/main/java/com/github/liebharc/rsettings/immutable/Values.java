package com.github.liebharc.rsettings.immutable;

import java.util.*;
import java.util.Map.Entry;

class Values {	
	static class Builder {
		
		private final Map<ReadSetting<?>, VersionedValue> values;
		
		private Builder(Values values) {
			this.values = new HashMap<>(values.values);
		}
	
		public void update(ReadSetting<?> setting, Object value, long version) {
			if (values.containsKey(setting)) {
				replace(setting, value, version);
			} else {
				put(setting, value, version);
			}
		}

		public void putAll(Values values) {
			this.values.putAll(values.values);
		}

		public void replace(ReadSetting<?> setting, Object value, long version) {
			values.replace(setting, new VersionedValue(version, value));		
		}

		public void put(ReadSetting<?> setting, Object 	value, long version) {
			values.put(setting, new VersionedValue(version, value));
		}

		public void replace(ReadSetting<?> setting, VersionedValue value) {
			values.replace(setting, value);
		}
		
		public boolean containsKey(ReadSetting<?> setting) {
			return values.containsKey(setting);
		}

		public VersionedValue get(ReadSetting<?> setting) {
			return values.get(setting);
		}

		public Collection<ReadSetting<?>> keySet() {
			return values.keySet();
		}

		
		public Values build() {
			return new Values(values);
		}
	}
	
	private final Map<ReadSetting<?>, VersionedValue> values;
	
	public Values() {
		values = new HashMap<>();
	}
	
	private Values(Map<ReadSetting<?>, VersionedValue> values) {
		this.values = values;
	}

	public boolean containsKey(ReadSetting<?> setting) {
		return values.containsKey(setting);
	}

	public VersionedValue get(ReadSetting<?> setting) {
		return values.get(setting);
	}
	
	public Set<Entry<ReadSetting<?>,VersionedValue>> entrySet() {
		return values.entrySet();
	}

	public Builder change() {
		return new Builder(this);
	}
}
