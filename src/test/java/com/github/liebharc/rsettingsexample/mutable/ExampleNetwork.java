package com.github.liebharc.rsettingsexample.mutable;

import com.github.liebharc.rsettings.mutable.StateMut;

public final class ExampleNetwork extends StateMut {

	private Count count;
	private DoubleCount doubleCount;
	private Name name;
	private EnableIfCountEquals5 enableIfCountEquals5;
	private Interdependent interdependent;
	private Interdependent2 interdependent2;
	private BoundedDouble boundedDouble;
	
	public ExampleNetwork() {
		count = register(new Count());
		enableIfCountEquals5 = register(new EnableIfCountEquals5(count));
		doubleCount = register(new DoubleCount(count));
		name = register(new Name());
		interdependent = register(new Interdependent());
		interdependent2 = register(new Interdependent2(interdependent));
		boundedDouble = register(new BoundedDouble());
	}
	
	public Count getCount() {
		return count;
	}
	
	public DoubleCount getDoubleCount() {
		return doubleCount;
	}
	
	public Name getName() {
		return name;
	}
	
	public EnableIfCountEquals5 getEnableIfCountEquals5() {
		return enableIfCountEquals5;
	}
	
	public Interdependent getInterdependent() {
		return interdependent;
	}
	
	public Interdependent2 getInterdependent2() {
		return interdependent2;
	}
	
	public BoundedDouble getBoundedDouble() {
		return boundedDouble;
	}
}
