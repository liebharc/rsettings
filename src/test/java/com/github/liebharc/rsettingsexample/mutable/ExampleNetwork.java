package com.github.liebharc.rsettingsexample.mutable;

import com.github.liebharc.rsettings.mutable.SettingStateMut;

public final class ExampleNetwork extends SettingStateMut {

	private Count count;
	private DoubleCountProperty doubleCount;
	private Name nameProperty;
	private EnableIfCountEquals5 enableIfCountEquals5Property;
	private Interdependent interdependentProperty;
	private Interdependent2 interdependent2Property;
	private BoundedDoubleProperty boundedDoubleProperty;
	
	public ExampleNetwork() {
		count = register(new Count());
		enableIfCountEquals5Property = register(new EnableIfCountEquals5(count));
		doubleCount = register(new DoubleCountProperty(count));
		nameProperty = register(new Name());
		interdependentProperty = register(new Interdependent());
		interdependent2Property = register(new Interdependent2(interdependentProperty));
		boundedDoubleProperty = register(new BoundedDoubleProperty());
	}
	
	public Count getCount() {
		return count;
	}
	
	public DoubleCountProperty getDoubleCount() {
		return doubleCount;
	}
	
	public Name getName() {
		return nameProperty;
	}
	
	public EnableIfCountEquals5 getEnableIfCountEquals5() {
		return enableIfCountEquals5Property;
	}
	
	public Interdependent getInterdependentProperty() {
		return interdependentProperty;
	}
	
	public Interdependent2 getInterdependent2Property() {
		return interdependent2Property;
	}
	
	public BoundedDoubleProperty getBoundedDoubleProperty() {
		return boundedDoubleProperty;
	}
}
