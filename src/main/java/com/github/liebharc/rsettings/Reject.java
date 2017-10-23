package com.github.liebharc.rsettings;

import org.apache.commons.math3.exception.NullArgumentException;

public class Reject {
	public static void ifNull(Object value) {
		if (value == null) {
			throw new NullArgumentException();
		}
	}
}
