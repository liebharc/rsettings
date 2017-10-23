package com.github.liebharc.rsettings.immutable;

/**
 * Provides some utility functions which can be used on all objects.
 */
class ObjectHelper {
	static boolean NullSafeEquals(Object a, Object b) {
		if (a == null)
			return b == null;
		
		if (b == null)
			return false;
		
		return a.equals(b);
	}
}
