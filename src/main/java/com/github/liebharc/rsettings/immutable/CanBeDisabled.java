package com.github.liebharc.rsettings.immutable;

/**
 * Allows a setting to indicate whether or not it is enabled. 
 * 
 * The purpose is to allow a GUI client to enable or disable certain GUI controls, the enabled
 * value has no default impact on the state. However implementations may throw exceptions
 * if a disabled setting is changed. 
 */
public interface CanBeDisabled<T> extends Setting<T> {
	boolean isEnabled(State state);
}
