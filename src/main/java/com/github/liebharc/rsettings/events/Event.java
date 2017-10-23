package com.github.liebharc.rsettings.events;

import java.util.function.Consumer;

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