package com.github.liebharc.rsettings;

public class Reject {
	public static void ifNull(Object value) {
		if (value == null) {
			throw new IllegalArgumentException("Argument must not be null");
		}
	}
}
