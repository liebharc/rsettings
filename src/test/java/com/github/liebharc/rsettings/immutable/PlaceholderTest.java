package com.github.liebharc.rsettings.immutable;

import java.util.*;

import org.junit.Test;

import com.github.liebharc.rsettings.CheckFailedException;
import static org.assertj.core.api.Assertions.*;

public class PlaceholderTest {

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
			super(0, Dependencies(selection, aOrB));
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
			super(0, Dependencies(selection, aOrB));
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
			super(0, Dependencies(selection, a, b));
			this.selection = selection;
			this.a = a;
			this.b = b;
		}
		
		@Override
		protected Optional<Integer> update(State state) throws CheckFailedException {
			if (!state.hasChanged(this)) {
				if (state.get(selection) == Selection.A) {
					return Optional.of(state.get(a));
				}
				
				return Optional.of(state.get(b));
			}
			
			return Optional.empty();
		}
	}
	
	public static class Model {
		private final Register reg = new Register();
		private final A a;
		private final B b;
		private final Selected selected;
		private final AOrB aOrB;

		public Model() {
			selected = reg.add(new Selected());
			Placeholder<Integer> aOrBPlaceholder = reg.add(new Placeholder<>());
			a = reg.add(new A(selected, aOrBPlaceholder));
			b = reg.add(new B(selected, aOrBPlaceholder));
			aOrB = reg.add(new AOrB(selected, a, b));
			aOrBPlaceholder.substitute(aOrB);
		}
		
		public Register getSettings() {
			return reg;
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
	
	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void equality() throws CheckFailedException {
		Register reg = new Register();
		Selected setting = reg.add(new Selected());
		Placeholder<Selection> placeholder = new Placeholder<>();
		placeholder.substitute(setting);
		assertThat(setting.equals(placeholder)).isTrue();
		assertThat(setting.hashCode() == placeholder.hashCode()).isTrue();
		List<Object> list = new ArrayList<>();
		list.add(setting);
		assertThat(list.contains(placeholder)).isTrue();
		list.clear();
		list.add(placeholder);
		assertThat(list.contains(setting)).isTrue();
		Map<Setting<?>, Integer> map = new HashMap<>();
		map.put(setting, 5);
		assertThat(map.get(placeholder)).isEqualTo(5);
		map.clear();
		map.put(placeholder, 3);
		assertThat(map.get(setting)).isEqualTo(3);
		State state = new State(reg);
		state = state.change().set(setting, Selection.B).build();
		assertThat(state.get(placeholder)).isEqualTo(Selection.B);
		assertThat(state.get(setting)).isEqualTo(Selection.B);
	}
}
