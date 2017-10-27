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
		count = new Count(getRegister());
		enableIfCountEquals5 = new EnableIfCountEquals5(getRegister(), count);
		doubleCount = new DoubleCount(getRegister(), count);
		name = new Name(getRegister());
		interdependent = new Interdependent(getRegister());
		interdependent2 = new Interdependent2(getRegister(), interdependent);
		boundedDouble = new BoundedDouble(getRegister());
		getRegister().complete();
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
