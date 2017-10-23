package com.github.liebharc.rsettings;

/**
 * This exception gets throws if the state is inconsistent.
 */
public class CheckFailedException extends Exception {

	private static final long serialVersionUID = 5295798195659633077L;

	public CheckFailedException(String message) {
		super(message);
	}
}
