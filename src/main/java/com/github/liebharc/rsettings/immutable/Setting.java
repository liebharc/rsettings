package com.github.liebharc.rsettings.immutable;

/**
 * A setting which is associated with a certain value type.
 * 
 * @param <T> The type of a setting.
 */
public interface Setting<T> {
	boolean shouldBeStored();
}
