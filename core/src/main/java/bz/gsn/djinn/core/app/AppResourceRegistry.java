package bz.gsn.djinn.core.app;

import bz.gsn.djinn.core.resource.Resource;
import bz.gsn.djinn.core.resource.ResourceRegistry;
import bz.gsn.djinn.core.util.CoreUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An internal subclass of {@link bz.gsn.djinn.core.resource.ResourceRegistry}, which can be instantiated.
 */
public final class AppResourceRegistry extends ResourceRegistry {

	private final Map<Class<? extends Resource>, Resource> resourceEntries = new HashMap<>();

	/**
	 * Creates a new {@link AppResourceRegistry}. This will scan for the {@link java.lang.invoke.MethodHandle MethodHandle}s
	 * of the <b>no-args</b> constructors (of public visibility) and invoke them.
	 * @param classes The classes of the {@link Resource} to instantiate.
	 */
	public AppResourceRegistry(@NotNull Collection<Class<? extends Resource>> classes) {
		var resources = classes.parallelStream()
				.map(resourceClass -> CoreUtils.sneakyThrows(() -> {
					var lookup = MethodHandles.publicLookup();
					try {
						return lookup.findConstructor(resourceClass, MethodType.methodType(void.class));
					} catch(NoSuchMethodException ignored) {
						throw new NoSuchMethodException("Resource " + resourceClass.getName() + " must expose a public no-args constructor");
					} catch(IllegalAccessException ignored) {
						throw new NoSuchMethodException("The no-args constructor of " + resourceClass.getName() + " must be public");
					}
				}))
				.map(handle -> handle.type().returnType().cast(CoreUtils.sneakyThrows(handle::invoke)))
				.collect(Collectors.toUnmodifiableSet());
		for(var resource : resources) {
			var res = (Resource) resource;
			this.resourceEntries.put(res.getClass(), res);
		}
	}

	/**
	 * Returns an {@link Optional} containing the {@link Resource} stored.
	 * multiple methods or runtimes are modifying the same resource.
	 * @param resource The class of the required resource.
	 * @return an {@link Optional} holding the {@link Resource}.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Resource> Optional<T> getResource(Class<T> resource) {
		return resourceEntries.containsKey(resource) ? (Optional<T>) Optional.of(resourceEntries.get(resource)) : Optional.empty();
	}
}
