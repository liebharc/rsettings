package com.github.liebharc.rsettings;

/**
 * This exception will be throws if the network fails to initialize.
 */
public class NetworkInitException extends RuntimeException {

	private static final long serialVersionUID = -3551527143586669429L;

	public NetworkInitException(String message) {
		super(message);
	}
}
