package com.github.liebharc.rsettings.mutable;

import com.github.liebharc.rsettings.immutable.MinMaxLimited;

public interface MinMaxLimitedMut<T extends Comparable<T>> extends MinMaxLimited<T> {
	default T getMin() {
		return getMin(null);
	}
	
	default T getMax() {
		return getMax(null);
	}
}
