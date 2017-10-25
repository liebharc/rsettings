package com.github.liebharc.rsettings.immutable;

import java.util.*;
 
class Values {	
	static class Builder {
		
		private final Map<StorageToken, VersionedValue> values;
		
		private Builder(Values values) {
			this.values = new HashMap<>(values.values);
		}
	
		public void update(ReadSetting<?> setting, Object value, long version) {
			if (values.containsKey(setting.getStorageToken())) {
				replace(setting, value, version);
			} else {
				put(setting, value, version);
			}
		}

		public void putAll(Values values) {
			this.values.putAll(values.values);
		}

		public void replace(ReadSetting<?> setting, Object value, long version) {
			values.replace(setting.getStorageToken(), new VersionedValue(version, value));		
		}

		public void put(ReadSetting<?> setting, Object 	value, long version) {
			values.put(setting.getStorageToken(), new VersionedValue(version, value));
		}

		public void replace(ReadSetting<?> setting, VersionedValue value) {
			values.replace(setting.getStorageToken(), value);
		}
		
		public boolean containsKey(ReadSetting<?> setting) {
			return values.containsKey(setting.getStorageToken());
		}

		public VersionedValue get(ReadSetting<?> setting) {
			return values.get(setting.getStorageToken());
		}
		
		public Values build() {
			return new Values(values);
		}
	}
	
	private final Map<StorageToken, VersionedValue> values;
	
	public Values() {
		values = new HashMap<>();
	}
	
	private Values(Map<StorageToken, VersionedValue> values) {
		this.values = values;
	}

	public boolean containsKey(ReadSetting<?> setting) {
		return values.containsKey(setting.getStorageToken());
	}

	public VersionedValue get(ReadSetting<?> setting) {
		return values.get(setting.getStorageToken());
	}

	public Builder change() {
		return new Builder(this);
	}
}
