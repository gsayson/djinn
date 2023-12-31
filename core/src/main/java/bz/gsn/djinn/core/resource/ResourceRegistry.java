package bz.gsn.djinn.core.resource;

import java.util.Optional;

/**
 * A {@link Resource} registry.
 * <p>
 * Resources are stored as singletons in this registry.
 */
// We permit ResourceRegistryImpl, but it's only a shim; all our functionality will be implemented here.
public abstract class ResourceRegistry {

	/**
	 * Returns an {@link Optional} containing the {@link Resource} stored.
	 * Multiple methods or runtimes may be modifying the same resource,
	 * so it is the resource's responsibility to implement thread-safe functionality.
	 * @param resource The class of the required resource.
	 * @return an {@link Optional} holding the {@link Resource}.
	 * @param <T> The type of the required resource.
	 */
	public abstract <T extends Resource> Optional<T> getResource(Class<T> resource);

}
