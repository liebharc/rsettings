package com.github.liebharc.rsettings;

public class Checks {

	public static <T extends Number & Comparable<T>> void CheckMinMax(T value, T min, T max) throws OufOfRangeException {
		boolean outOfRange = value.compareTo(min) < 0 || value.compareTo(max) > 0;
		if (outOfRange) {
			throw new OufOfRangeException(value, min, max);
		}
 	}
}
