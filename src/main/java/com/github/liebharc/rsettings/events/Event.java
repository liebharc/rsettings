package com.github.liebharc.rsettings.events;

import java.util.function.Consumer;

/**
 * An event/observable implementation. 
 * TODO: Could be replaced with a Guava EventBus in future.
 * @param <T> The event argument type.
 */
public final class Event<T> {

	private EventPublisher<T> publisher;
	
	Event(EventPublisher<T> publisher)
	{
		this.publisher = publisher;
	}
	
	public void subscribe(Consumer<T> listener)
	{
		publisher.subscribe(listener);
	}
	
	public void unsubscribe(Consumer<T> listener)
	{
		publisher.unsubscribe(listener);
	}
}