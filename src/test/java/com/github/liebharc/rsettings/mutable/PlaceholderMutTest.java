package com.github.liebharc.rsettings.mutable;

import java.util.*; 

import org.junit.Test;

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.immutable.ReadSetting;
import com.github.liebharc.rsettings.immutable.State;

import static org.assertj.core.api.Assertions.*;

public class PlaceholderMutTest {

	private enum Selection {
		A,
		B
	}
	
	private static class Selected extends ReadWriteSettingMut<Selection> {

		public Selected(RegisterMut register) {
			super(register, Selection.A, NoDependencies());
		}
	}
	
	private static class A extends ReadWriteSettingMut<Integer> {

		private ReadSetting<Integer> aOrB;
		private Selected selection;

		public A(
				RegisterMut register,
				Selected selection,
				ReadSetting<Integer> aOrB) {
			super(register, 0, Dependencies(selection, aOrB));
			this.selection = selection;
			this.aOrB = aOrB;
		}
		
		@Override
		protected Optional<Integer> update(State state) throws CheckFailedException {
			if (state.hasChanged(aOrB) && state.get(selection) == Selection.A)
				return Optional.of(state.get(aOrB));
				 
			return super.update(state);
		}
	}
	
	private static class B extends ReadWriteSettingMut<Integer> {

		private ReadSetting<Integer> aOrB;
		private Selected selection;

		public B(
				RegisterMut register,
				Selected selection,
				ReadSetting<Integer> aOrB) {
			super(register, 0, Dependencies(selection, aOrB));
			this.selection = selection;
			this.aOrB = aOrB;
		}
		
		@Override
		protected Optional<Integer> update(State state) throws CheckFailedException {
			if (state.hasChanged(aOrB) && state.get(selection) == Selection.B)
				return Optional.of(state.get(aOrB));
				 
			return super.update(state);
		}
	}

	
	private static class AOrB extends ReadWriteSettingMut<Integer> {
		private final Selected selection;
		private final A a;
		private final B b;

		public AOrB(RegisterMut register, Selected selection, A a, B b) {
			super(register, 0, Dependencies(selection, a, b));
			this.selection = selection;
			this.a = a;
			this.b = b;
		}
		
		@Override
		protected Optional<Integer> update(State state) throws CheckFailedException {
			if (state.get(selection) == Selection.A) {
				return Optional.of(state.get(a));
			}
			
			return Optional.of(state.get(b));
		}
	}
	
	public static class Model extends StateMut {
		private final A a;
		private final B b;
		private final Selected selected;
		private final AOrB aOrB;

		public Model() {
			selected = new Selected(getRegister());
			PlaceholderMut<Integer> aOrBPlaceholder = new PlaceholderMut<>(getRegister());
			a = new A(getRegister(), selected, aOrBPlaceholder);
			b = new B(getRegister(), selected, aOrBPlaceholder);
			aOrB = new AOrB(getRegister(), selected, a, b);
			aOrBPlaceholder.substitute(aOrB);
			getRegister().complete();
		}
	}
	
	@Test
	public void changeToSourceSettingShouldUpdateAOrB() throws CheckFailedException {
		Model model = new Model();
		model.aOrB.setValue(5);
		assertThat(model.a.getValue()).isEqualTo(5);
		assertThat(model.b.getValue()).isEqualTo(0);
		assertThat(model.aOrB.getValue()).isEqualTo(5);
		
		model.startTransaction()
				.set(model.aOrB, 3)
				.set(model.selected, Selection.B)
				.execute();

		assertThat(model.a.getValue()).isEqualTo(5);
		assertThat(model.b.getValue()).isEqualTo(3);
		assertThat(model.aOrB.getValue()).isEqualTo(3);
		
		model.selected.setValue(Selection.A);
		assertThat(model.a.getValue()).isEqualTo(5);
		assertThat(model.b.getValue()).isEqualTo(3);
		assertThat(model.aOrB.getValue()).isEqualTo(5);
	}
}
