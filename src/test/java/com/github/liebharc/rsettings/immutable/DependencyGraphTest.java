package com.github.liebharc.rsettings.immutable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

public class DependencyGraphTest {

	private static class DepTestSetting extends ReadWriteSetting<Integer> {
		
		public DepTestSetting() {
			super(0, NoDependencies());
		}

		public DepTestSetting(ReadSetting<?> first, ReadSetting<?>... dependencies) {
			super(0, Dependencies(first, dependencies));
		}
	}
	
	private static class Model {
		final DepTestSetting a;
		final DepTestSetting b;
		final DepTestSetting dependsOnA;
		final DepTestSetting dependsOnB;
		final DepTestSetting c;
		final DepTestSetting dependsOnBAndC;
		final DepTestSetting d;
		final Placeholder<Integer> dependsOnBAndCPlaceholder;
		final DepTestSetting dependsOnPlaceholder;
		
		final List<ReadSetting<?>> settings;
		
		public Model() {
			a = new DepTestSetting();
			b = new DepTestSetting();
			dependsOnA = new DepTestSetting(a);
			dependsOnB = new DepTestSetting(b);
			c = new DepTestSetting();
			dependsOnBAndC = new DepTestSetting(dependsOnB, c);
			d = new DepTestSetting();
			dependsOnBAndCPlaceholder = new Placeholder<>();
			dependsOnBAndCPlaceholder.substitute(dependsOnBAndC);
			dependsOnPlaceholder = new DepTestSetting(dependsOnBAndCPlaceholder);
			settings = Arrays.asList(a, b, dependsOnA, dependsOnB, c, dependsOnBAndC, d, dependsOnPlaceholder);
			
		}
	}
	
	@Test
	public void findDependencies() {
		Model model = new Model();
		DependencyGraph graph = new DependencyGraph(model.settings);
		assertThat(getDependencies(graph, model.a)).containsExactly(model.dependsOnA);
		assertThat(getDependencies(graph, model.b)).containsExactly(model.dependsOnB);
		assertThat(getDependencies(graph, model.dependsOnA)).isEmpty();
		assertThat(getDependencies(graph, model.dependsOnB)).containsExactly(model.dependsOnBAndC);
		assertThat(getDependencies(graph, model.c)).containsExactly(model.dependsOnBAndC);
		assertThat(getDependencies(graph, model.dependsOnBAndC)).containsExactly(model.dependsOnPlaceholder);
		assertThat(getDependencies(graph, model.dependsOnBAndCPlaceholder)).containsExactly(model.dependsOnPlaceholder);
		assertThat(getDependencies(graph, model.d)).isEmpty();
		assertThat(getDependencies(graph, model.dependsOnPlaceholder)).isEmpty();
	}
	
	private List<ReadSetting<?>> getDependencies(DependencyGraph graph, ReadSetting<?> setting) {
		List<ReadSetting<?>> result = new ArrayList<>();
		List<ReadSetting<?>> settings = new ArrayList<>();
		settings.add(setting);
		DependencyGraph.Path path = graph.getDependencies(settings);
		while (path.moveNext(false)) {
			result.add(path.current());	
		}
		
		return result;
	}
}
