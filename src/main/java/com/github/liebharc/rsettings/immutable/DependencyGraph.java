package com.github.liebharc.rsettings.immutable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is responsible to detect the dependencies between settings and to provide
 * other classes with the information on which settings depend on a given setting. 
 */
final class DependencyGraph {
	
	private static class DependencyNode {
		
		private final List<ReadSetting<?>> dependencies = new ArrayList<>();

		public void add(ReadSetting<?> dependency) {
			dependencies.add(dependency);
		}
		
		public void visit(Path path) {
			path.next.addAll(dependencies);
		}

		public List<ReadSetting<?>> getDependencies() {
			return dependencies;
		}
	}
	
	private class CyclicDependencyNode extends DependencyNode {

		private final List<ReadSetting<?>> placeholders;

		public CyclicDependencyNode(List<ReadSetting<?>> cyclicReferences) {
			this.placeholders = cyclicReferences;
		}
		
		@Override
		public void visit(Path path) {
			super.visit(path);
			path.visited.removeAll(getDependencies());
			forceAddAllDependencies(path);
		}

		private void forceAddAllDependencies(Path path) {
			for (ReadSetting<?> setting : placeholders) {
				DependencyNode dep = settingDependencies.get(setting);
				path.next.addAll(dep.getDependencies());
				path.visited.removeAll(dep.getDependencies());
				path.nextHint = 0;
			}
		}
	}
	
	/**
	 * Iterates over the dependencies.
	 */
	public class Path {
		private final Set<ReadSetting<?>> next = new HashSet<>();
		private final Set<ReadSetting<?>> visited = new HashSet<>();
		private ReadSetting<?> current;
		private int nextHint = 0;
		
		private Path(List<ReadSetting<?>> init) {
			List<ReadSetting<?>> expanded =
				settings.stream().filter(s -> { 
					return init.stream().anyMatch(i -> i.getId() == s.getId());
				})
				.collect(Collectors.toList());
			next.addAll(expanded);
			for (ReadSetting<?> setting : expanded) {
				settingDependencies.get(setting).visit(this);
			}
			
			selectCurrent();
		}
		
		public ReadSetting<?> current() {
			return current;
		}
		
		public boolean moveNext(boolean hasCurrentBeenModified) {
			visited.add(current);
			
			if (hasCurrentBeenModified) {
				settingDependencies.get(current).visit(this);
			}
			
			selectCurrent();
			return current != null;
		}
		
		private void selectCurrent() {
			if (next.size() == 1) {
				ReadSetting<?> onlySetting = next.iterator().next();
				if (!visited.contains(onlySetting)) {
					current = onlySetting;
					return;
				}
			}
			
			for (int i = nextHint; i < settings.size(); i++) {
				ReadSetting<?> setting = settings.get(i);
				if (next.contains(setting) && !visited.contains(setting)) {
					current = setting;
					nextHint = i + 1;
					return;
				}
			}
			
			current = null;
		}
		
		 List<ReadSetting<?>> getVisited() {
			 return settings.stream()
					 .filter(s -> visited.contains(s))
					 .collect(Collectors.toList());
		 }
	}
	
	private final Map<ReadSetting<?>, DependencyNode> settingDependencies = new HashMap<>();
	private final List<ReadSetting<?>> settings;
	
	public DependencyGraph(List<ReadSetting<?>> settings) {
		this.settings = settings;
		Map<ReadSetting<?>, List<Placeholder<?>>> settingToPlaceholder = new HashMap<>();
		Map<ReadSetting<?>, ReadSetting<?>> placeholderToSetting = new HashMap<>();
		
		catalogPlaceholders(settings, settingToPlaceholder, placeholderToSetting);
		initializeEmptyDependenciesMap(settings, settingToPlaceholder);
		catalogDependencies(settings, settingToPlaceholder, placeholderToSetting);
	}

	private void catalogPlaceholders(List<ReadSetting<?>> settings,
			Map<ReadSetting<?>, List<Placeholder<?>>> settingToPlaceholder,
			Map<ReadSetting<?>, ReadSetting<?>> placeholderToSetting) {
		for (ReadSetting<?> setting : settings) {
			if (Placeholder.isPlaceholder(setting)) {
				Placeholder<?> placeholder = (Placeholder<?>)setting;
				ReadSetting<?> reference = 
						settings.stream()
						.filter(s -> s.getId() == placeholder.getId() && !Placeholder.isPlaceholder(s))
						.findFirst()
						.get();
				placeholderToSetting.put(placeholder, reference);
				if (!settingToPlaceholder.containsKey(reference)) {
					settingToPlaceholder.put(reference, new ArrayList<>());
				}
				
				settingToPlaceholder.get(reference).add(placeholder);
			}
		}
	}

	private void initializeEmptyDependenciesMap(
			List<ReadSetting<?>> settings,
			Map<ReadSetting<?>, List<Placeholder<?>>> settingToPlaceholder) {
		for (ReadSetting<?> setting : settings) {
			if (!Placeholder.isPlaceholder(setting)) {
				if (settingToPlaceholder.containsKey(setting)) {
					List<ReadSetting<?>> cyclicReferences =
							settingToPlaceholder.get(setting)
							.stream()
							.filter(s -> s.getType() == PlaceholderType.Cyclic)
							.collect(Collectors.toList());
					if (!cyclicReferences.isEmpty()) {
						settingDependencies.put(setting, new CyclicDependencyNode(cyclicReferences));
					}
					else {
						settingDependencies.put(setting, new DependencyNode());
					}
				}
				else {
					settingDependencies.put(setting, new DependencyNode());
				}
			}
			else {
				settingDependencies.put(setting, new DependencyNode());
			}
		}
	}

	private void catalogDependencies(List<ReadSetting<?>> settings,
			Map<ReadSetting<?>, List<Placeholder<?>>> settingToPlaceholder,
			Map<ReadSetting<?>, ReadSetting<?>> placeholderToSetting) {
		for (ReadSetting<?> setting : settings) {
			List<ReadSetting<?>> dependencies = handlePlaceholders(
					setting,
					settingToPlaceholder,
					placeholderToSetting);
			for (ReadSetting<?> source : dependencies) {
				settingDependencies.get(source).add(setting);
			}
		}
	}

	private List<ReadSetting<?>> handlePlaceholders(
			ReadSetting<?> setting,
			Map<ReadSetting<?>, List<Placeholder<?>>> settingToPlaceholder,
			Map<ReadSetting<?>, ReadSetting<?>> placeholderToSetting) {
		if (Placeholder.isPlaceholder(setting)) {
			return Arrays.asList(placeholderToSetting.get(setting));
		}
		else {
			List<ReadSetting<?>> dependencies = new ArrayList<>();
			dependencies.addAll(setting.getDependencies().asList());
			if (settingToPlaceholder.containsKey(setting)) {
				dependencies.addAll(settingToPlaceholder.get(setting));
			}
			
			return dependencies;
		}
	}
	
	/**
	 * Gets all settings which depend on the given setting and therefore must be updated.
	 * @param setting A setting.
	 * @return All settings which depend on the given setting.
	 */
	public Path getDependencies(List<ReadSetting<?>> settings) {
		return new Path(settings);
	}
}
