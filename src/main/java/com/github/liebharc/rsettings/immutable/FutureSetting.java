package com.github.liebharc.rsettings.immutable;

import com.github.liebharc.rsettings.StateInitException;

/**
 * Warning: Cycic dependencies can lead to infinite loops. Use with care.
 * 
 * A future setting can be used instead of a real setting to retrieve the value of a setting.
 * It is intended to be used to expressed cyclic dependencies. In order to achieve that first a FutureSetting
 * is created and then the FutureSetting is used whenever a reference to the not yet created setting is needed.
 * When the new setting is then created the substitute method may be used to signal that this settings value
 * should be used when ever the future setting is used.
 *
 * @param <T> The value of the setting.
 */
public final class FutureSetting<T> extends ReadSetting<T> {

	private SettingId id = null;
	
	public FutureSetting() {
		super(null, NoDependencies());
	}

	public void substitue(ReadSetting<T> setting) {
		if (id != null) {
			throw new StateInitException("Substitute can only be called once");
		}
		
		id = setting.getId();
	}
	
	@Override
	protected SettingId getId() {
		if (id == null) {
			return super.getId();
		}
		
		return id;
	}
}
