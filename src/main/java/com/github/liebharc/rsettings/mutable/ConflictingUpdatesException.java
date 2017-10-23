package com.github.liebharc.rsettings.mutable;

import com.github.liebharc.rsettings.CheckFailedException;

public class ConflictingUpdatesException extends CheckFailedException {

	private static final long serialVersionUID = -4263203035182022882L;
	
	public ConflictingUpdatesException() {
		super("State has been meanwhile updated by someone else");
	}
}
