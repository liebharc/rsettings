package com.github.liebharc.rsettings.immutable;

import java.util.*;

import org.junit.Test;

import com.github.liebharc.rsettings.CheckFailedException;

import static org.assertj.core.api.Assertions.*;

public class FutureSettingTest {

	private enum Selection {
		A,
		B
	}
	
	private static class Selected extends ReadWriteSetting<Selection> {

		public Selected() {
			super(Selection.A, NoDependencies());
		}
	}
	
	private static class A extends ReadWriteSetting<Integer> {

		private ReadSetting<Integer> aOrB;
		private Selected selection;

		public A(
				Selected selection,
				ReadSetting<Integer> aOrB) {
			super(0, NoDependencies());
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
	
	private static class B extends ReadWriteSetting<Integer> {

		private ReadSetting<Integer> aOrB;
		private Selected selection;

		public B(
				Selected selection,
				ReadSetting<Integer> aOrB) {
			super(0, NoDependencies());
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

	
	private static class AOrB extends ReadWriteSetting<Integer> {
		private final Selected selection;
		private final A a;
		private final B b;

		public AOrB(Selected selection, A a, B b) {
			super(0, new ReadSetting<?>[] { selection, a, b });
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
			selected = new Selected();
			FutureSetting<Integer> futureAOrB = new FutureSetting<>();
			a = new A(selected, futureAOrB);
			b = new B(selected, futureAOrB);
			aOrB = new AOrB(selected, a, b);
			futureAOrB.substitue(aOrB);
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
	public void mutualUpdateConflictTest() throws CheckFailedException {
		Model model = new Model();
		State state = new State(model.getSettings());
		state = state.change()
				.set(model.selected, Selection.A)
				.set(model.a, 5)
				.set(model.b, 3)
				.set(model.aOrB, 4)
				.build();
		assertThat(state.get(model.a)).isEqualTo(4);
		assertThat(state.get(model.b)).isEqualTo(3);
		assertThat(state.get(model.aOrB)).isEqualTo(4);
	}
}
