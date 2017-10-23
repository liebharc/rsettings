package com.github.liebharc.rsettings;

/**
 * Helper class for commonly used checks.
 */
public class Checks {

	/**
	 * Checks that the value is between min and max.
	 * @param value A value
	 * @param min A maximum for value
	 * @param max A minimum for value
	 * @throws OufOfRangeException if value < min or value > max
	 */
	public static <T extends Number & Comparable<T>> void CheckMinMax(T value, T min, T max) throws OufOfRangeException {
		boolean outOfRange = value.compareTo(min) < 0 || value.compareTo(max) > 0;
		if (outOfRange) {
			throw new OufOfRangeException(value, min, max);
		}
 	}
}
