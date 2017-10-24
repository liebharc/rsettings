package com.github.liebharc.rsettings.mutable;

import com.github.liebharc.rsettings.immutable.CanBeDisabled;

public interface CanBeDisabledMut<T> extends CanBeDisabled<T>{
	
	default boolean isEnabled() {
		return isEnabled(null);
	}
}
