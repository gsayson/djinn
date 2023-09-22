package bz.gsn.djinn.core.resource;

import bz.gsn.djinn.core.app.AppResourceRegistry;

import java.util.*;

/**
 * A {@link Resource} registry.
 * <p>
 * Resources are stored as singletons in this registry.
 */
// We permit ResourceRegistryImpl, but it's only a shim; all our functionality will be implemented here.
public abstract sealed class ResourceRegistry permits AppResourceRegistry {

	/**
	 * Returns an {@link Optional} containing the {@link Resource} stored.
	 * multiple methods or runtimes are modifying the same resource.
	 * @param resource The class of the required resource.
	 * @return an {@link Optional} holding the {@link Resource}.
	 * @param <T> The type of the required resource.
	 */
	public abstract <T extends Resource> Optional<T> getResource(Class<T> resource);

}
