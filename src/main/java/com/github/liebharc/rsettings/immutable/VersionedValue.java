package com.github.liebharc.rsettings.immutable;

class VersionedValue {
	private long stateVersion;
	private Object value;

	public VersionedValue(long stateVersion, Object value) {
		this.stateVersion = stateVersion;
		this.value = value;
	}

	public long getVersion() {
		return stateVersion;
	}

	public Object getValue() {
		return value;
	}
}
