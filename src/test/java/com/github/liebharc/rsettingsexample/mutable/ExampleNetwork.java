package com.github.liebharc.rsettingsexample.mutable;

import com.github.liebharc.rsettings.mutable.PropertyNetwork;

public final class ExampleNetwork extends PropertyNetwork {

	private CountProperty count;
	private DoubleCountProperty doubleCount;
	private NameProperty nameProperty;
	private EnableIfCountEquals5 enableIfCountEquals5Property;
	private InterdependentProperty interdependentProperty;
	private Interdependent2Property interdependent2Property;
	private BoundedDoubleProperty boundedDoubleProperty;
	
	public ExampleNetwork() {
		count = register(new CountProperty());
		enableIfCountEquals5Property = register(new EnableIfCountEquals5(count));
		doubleCount = register(new DoubleCountProperty(count));
		nameProperty = register(new NameProperty());
		interdependentProperty = register(new InterdependentProperty());
		interdependent2Property = register(new Interdependent2Property(interdependentProperty));
		boundedDoubleProperty = register(new BoundedDoubleProperty());
	}
	
	public CountProperty getCount() {
		return count;
	}
	
	public DoubleCountProperty getDoubleCount() {
		return doubleCount;
	}
	
	public NameProperty getName() {
		return nameProperty;
	}
	
	public EnableIfCountEquals5 getEnableIfCountEquals5() {
		return enableIfCountEquals5Property;
	}
	
	public InterdependentProperty getInterdependentProperty() {
		return interdependentProperty;
	}
	
	public Interdependent2Property getInterdependent2Property() {
		return interdependent2Property;
	}
	
	public BoundedDoubleProperty getBoundedDoubleProperty() {
		return boundedDoubleProperty;
	}
}
