package com.github.liebharc.rsettings.immutable;

/**
 * Allows a setting to inform a client about its min and max value. 
 * 
 * The purpose is to inform a client about min and max values.
 * This has no default impact on the state. However implementations will likely throw 
 * an exception if a value is outside the bounds of min and max. 
 */
public interface MinMaxLimited<T extends Comparable<T>> {

	T getMin(SettingState state);
	
	T getMax(SettingState state);
}
