package com.github.liebharc.rsettings.immutable;

import java.util.*;

import org.junit.Ignore;
import org.junit.Test;

import com.github.liebharc.rsettings.CheckFailedException;

import static org.assertj.core.api.Assertions.*;

public class MutualDependentSettingsTest {

	private enum Selection {
		A,
		B
	}
	
	private static class A extends ReadWriteSetting<Integer> {

		public A() {
			super(0);
		}
	}
	
	private static class B extends ReadWriteSetting<Integer> {

		public B() {
			super(0);
		}
	}
	
	private static class Selected extends ReadWriteSetting<Selection> {

		public Selected() {
			super(Selection.A);
		}
	}
	
	private static class AOrB extends ReadWriteSetting<Integer> {
		private final Selected selection;
		private final A a;
		private final B b;

		public AOrB(Selected selection, A a, B b) {
			super(0, selection, a, b);
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
	
	public static class Model {
		private final A a;
		private final B b;
		private final Selected selected;
		private final AOrB aOrB;

		public Model() {
			a = new A();
			b = new B();
			selected = new Selected();
			aOrB = new AOrB(selected, a, b);
		}
		
		public ReadSetting<?>[] getSettings() {
			return new ReadSetting<?>[] { a, b, selected, aOrB };
		}
	}
	
	@Test
	public void changeToAOrBShouldUpdateDerivedSetting() throws CheckFailedException {
		Model model = new Model();
		State state = new State(model.getSettings());
		state = state
			.change()
			.set(model.a, 5)
			.build();
		assertThat(state.get(model.aOrB)).isEqualTo(5);
		
		state = state
				.change()
				.set(model.b, 3)
				.set(model.selected, Selection.B)
				.build();
		assertThat(state.get(model.aOrB)).isEqualTo(3);
		
		state = state
				.change()
				.set(model.selected, Selection.A)
				.build();
		assertThat(state.get(model.aOrB)).isEqualTo(5);
	}
	
	@Test
	@Ignore
	public void changeToSourceSettingShouldUpdateAOrB() throws CheckFailedException {
		Model model = new Model();
		State state = new State(model.getSettings());
		state = state.change()
				.set(model.aOrB, 5)
				.build();
		assertThat(state.get(model.a)).isEqualTo(5);
		assertThat(state.get(model.b)).isEqualTo(0);
		assertThat(state.get(model.aOrB)).isEqualTo(5);
		
		state = state.change()
				.set(model.aOrB, 3)
				.set(model.selected, Selection.B)
				.build();
		assertThat(state.get(model.a)).isEqualTo(5);
		assertThat(state.get(model.b)).isEqualTo(3);
		assertThat(state.get(model.aOrB)).isEqualTo(3);
		
		state = state.change()
				.set(model.selected, Selection.A)
				.build();
		
		assertThat(state.get(model.a)).isEqualTo(5);
		assertThat(state.get(model.b)).isEqualTo(3);
		assertThat(state.get(model.aOrB)).isEqualTo(5);
	}
	
	@Test
	@Ignore
	public void mutualUpdateNoConflictTest() throws CheckFailedException {
		Model model = new Model();
		State state = new State(model.getSettings());
		state = state.change()
				.set(model.selected, Selection.A)
				.set(model.a, 5)
				.set(model.b, 3)
				.set(model.aOrB, 5)
				.build();
		assertThat(state.get(model.a)).isEqualTo(5);
		assertThat(state.get(model.b)).isEqualTo(3);
		assertThat(state.get(model.aOrB)).isEqualTo(5);
	}
	
	@Test
	@Ignore
	public void mutualUpdateConflictTest() throws CheckFailedException {
		Model model = new Model();
		State state = new State(model.getSettings());
		State.Builder builder = state.change()
				.set(model.selected, Selection.A)
				.set(model.a, 5)
				.set(model.b, 3)
				.set(model.aOrB, 4);
		assertThatThrownBy(() -> builder.build());
	}
}
