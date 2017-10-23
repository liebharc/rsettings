package com.github.liebharc.rsettings.mutable;

import static org.assertj.core.api.Assertions.*;

import java.util.function.Consumer;

import org.junit.Test;

import com.github.liebharc.rsettings.events.*;

public class EventTest {

	@Test
	public void subscribeAndUnsubscribe() {
		EventPublisher<Integer> publisher = new EventPublisher<Integer>();
		Event<Integer> event = publisher.getEvent();
		IntBox value = new IntBox();
		Consumer<Integer> listener = i -> value.setValue(i);
		publisher.raise(1);
		assertThat(value).isEqualTo(0); 
		event.subscribe(listener);
		publisher.raise(2);
		assertThat(value).isEqualTo(2);
		event.unsubscribe(listener);
		publisher.raise(3);
		assertThat(value).isEqualTo(2);
	}
}
