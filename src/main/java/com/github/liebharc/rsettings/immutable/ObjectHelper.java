package com.github.liebharc.rsettings.immutable;

class ObjectHelper {
	static boolean NullSafeEquals(Object a, Object b) {
		if (a == null)
			return b == null;
		
		if (b == null)
			return false;
		
		return a.equals(b);
	}
}