package com.github.liebharc.rsettings.mutable;

public interface HasMinMaxMut<T extends Comparable<T>> {
	public T getMin();
	public T getMax();
}
