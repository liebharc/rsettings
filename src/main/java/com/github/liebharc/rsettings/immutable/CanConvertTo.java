package com.github.liebharc.rsettings.immutable;

/**
 * Allows a value to to implicitly converted in another value during a set operation
 * 
 * @param <T> The destination value.
 */
public interface CanConvertTo<T> {
	
	/**
	 * The conversion routine.
	 * @param previousValue The previous value in the state. This may in some cases be useful
	 * if the conversion only updates parts of a value.
	 * @return The conversion result.
	 */
	public T convertTo(T previousValue);
}
