package com.github.liebharc.rsettings.mutable;

public interface MinMaxLimitedMut<T extends Comparable<T>> {
	T getMin();
	T getMax();
}
