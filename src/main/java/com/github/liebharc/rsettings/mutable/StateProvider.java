package com.github.liebharc.rsettings.mutable;

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.events.Event;
import com.github.liebharc.rsettings.immutable.State;

interface StateProvider {

	void set(State state) throws CheckFailedException;

	State get();

	Event<State> getStateChangedEvent();
}