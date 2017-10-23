package com.github.liebharc.rsettings.mutable;

public class IntBox {

	private int value = 0;
	
	public IntBox() {
		
	}
	
	public IntBox(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	public void increment()
	{
		value++;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Integer)
		{
			// This additional case makes the testing code a little briefer
			// however it also breaks the commutative requirement of the equality operation.
			// We accept this break of the rules for this specific case, but it's something
			// we should keep in mind.
			Integer otherValue = (Integer)obj;
			return otherValue == value;
		}
		return super.equals(obj);
	}
	
	@Override
	public String toString() {
		return "[" + value + "]";
	}
	
}
