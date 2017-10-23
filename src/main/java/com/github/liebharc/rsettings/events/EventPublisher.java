package com.github.liebharc.rsettings.events;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class EventPublisher<T> {
	
	private Event<T> event;
	
	private List<Consumer<T>> listeners;

	public EventPublisher()
	{
		event = new Event<T>(this);
		listeners = new ArrayList<Consumer<T>>();
	}
	
	public void raise(T argument)
	{
		for (Consumer<T> listener : listeners) {
			listener.accept(argument);
		}
	}
	
	void subscribe(Consumer<T> listener)
	{
		listeners.add(listener);
	}
	
	void unsubscribe(Consumer<T> listener)
	{
		listeners.remove(listener);
	}

	public Event<T> getEvent() {
		return event;
	}
}
