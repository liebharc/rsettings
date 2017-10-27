package com.github.liebharc.rsettings.immutable;

import com.github.liebharc.rsettings.StateInitException;

/**
 * Warning: Cyclic dependencies can lead to infinite loops. Use with care.
 * 
 * A placeholder can be used instead of a real setting to retrieve the value of a setting.
 * It is intended to be used to expressed cyclic dependencies. In order to achieve that first a placeholder
 * is created and then the placeholder is used whenever a reference to the not yet created setting is needed.
 * When the new setting is then created the substitute method may be used to signal that this settings value
 * should be used when ever the placeholder is used.
 *
 * @param <T> The value of the setting.
 */
public final class Placeholder<T> extends ReadSetting<T> {

	static boolean isPlaceholder(ReadSetting<?> setting) {
		return setting instanceof Placeholder<?>; 
	}
	
	private SettingId id = null;
	
	public Placeholder() {
		super(null, NoDependencies());
	}

	public void substitute(ReadSetting<T> setting) {
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
