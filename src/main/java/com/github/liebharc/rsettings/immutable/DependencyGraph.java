package com.github.liebharc.rsettings.immutable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is responsible to detect the dependencies between settings and to provide
 * other classes with the information on which settings depend on a given setting. 
 */
final class DependencyGraph {
	
	private static class DependencyNode {
		
		protected final List<ReadSetting<?>> dependencies = new ArrayList<>();

		public void add(ReadSetting<?> dependency) {
			dependencies.add(dependency);
		}
		
		public void visit(Path path) {
			path.next.addAll(dependencies);
		}
	}
	
	private static class CyclicDependencyNode extends DependencyNode {

		private final ReadSetting<?> setting;

		public CyclicDependencyNode(ReadSetting<?> setting) {
			this.setting = setting;
		}
		
		public CyclicDependencyNode() {
			this(null);
		}

		@Override
		public void visit(Path path) {
			super.visit(path);
			path.visited.removeAll(dependencies);
			path.next.add(setting);
		}
	}
	
	/**
	 * Iterates over the dependencies.
	 */
	public class Path {
		private final Set<ReadSetting<?>> next = new HashSet<>();
		private final Set<ReadSetting<?>> visited = new HashSet<>();
		private ReadSetting<?> current;
		private final List<ReadSetting<?>> forcedUpdates;
		
		private Path(List<ReadSetting<?>> init) {
			forcedUpdates = new ArrayList<>(init);
			next.addAll(init);
			for (ReadSetting<?> setting : init) {
				settingDependencies.get(setting).visit(this);
			}
			
			selectCurrent();
		}
		
		public ReadSetting<?> current() {
			return current;
		}
		
		public boolean moveNext(boolean hasCurrentBeenModified) {
			visited.add(current);
			
			Set<ReadSetting<?>> forced = 
					forcedUpdates.stream()
						.filter(s -> s.getId() == current.getId())
						.collect(Collectors.toSet());
			if (!forced.isEmpty()) {
				forcedUpdates.removeAll(forced);
				settingDependencies.get(current).visit(this);
			}
			else if (hasCurrentBeenModified) {
				settingDependencies.get(current).visit(this);
			}
			
			selectCurrent();
			return current != null;
		}
		
		private void selectCurrent() {
			current = settings.stream()
					.filter(s -> next.contains(s) && !visited.contains(s))
					.findFirst().orElseGet(() -> null);
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
				if (settingToPlaceholder.containsKey(setting) 
						&& settingToPlaceholder.get(setting).stream().anyMatch(s -> s.getType() == PlaceholderType.Cyclic)) {
					settingDependencies.put(setting, new CyclicDependencyNode());
				}
				else {
					settingDependencies.put(setting, new DependencyNode());
				}
			}
			else {
				Placeholder<?> placeholder = (Placeholder<?>)setting;
				if (placeholder.getType() == PlaceholderType.Linear) {
					settingDependencies.put(setting, new DependencyNode());
				}
				else {
					settingDependencies.put(setting, new CyclicDependencyNode(setting));
				}
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
