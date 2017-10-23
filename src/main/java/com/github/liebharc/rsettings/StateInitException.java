package com.github.liebharc.rsettings;

import com.github.liebharc.rsettings.mutable.ConflictingUpdatesException;

/**
 * This exception will be throws if the state fails to initialize.
 */
public class StateInitException extends RuntimeException {

	private static final long serialVersionUID = -3551527143586669429L;

	public StateInitException(String message) {
		super(message);
	}

	public StateInitException(String message, ConflictingUpdatesException e) {
		super(message, e);
	}
}
