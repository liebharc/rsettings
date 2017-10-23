package com.github.liebharc.rsettings;

public class OufOfRangeException extends CheckFailedException {

	private static final long serialVersionUID = 6460412299299059328L;
	
	public <T extends Number> OufOfRangeException(T value, T min, T max) {
		super("Value must be between " + min + " and " + max + " but it is " + value);
	}
}
