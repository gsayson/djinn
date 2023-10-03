package foo.bar.resource;

import bz.gsn.djinn.core.resource.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link Resource} storing singletons.
 */
@SuppressWarnings("unused")
@Slf4j
public class SingletonResource extends Resource {

	/**
	 * The map of singletons.
	 */
	// we should really abstract the mutation of this field.
	private final ConcurrentHashMap<Class<?>, Object> singletons = new ConcurrentHashMap<>();

	/**
	 * Puts a singleton into this {@link SingletonResource}.
	 * @param singletonClass The class of the singleton.
	 * @param singleton The instance itself.
	 * @param <T> The type of the singleton.
	 * @throws IllegalStateException if {@link #get(Class)} does not return {@code null};
	 * i.e., it already exists.
	 */
	public <T> void put(@NotNull Class<T> singletonClass, @NotNull T singleton) {
		if(this.singletons.containsKey(singletonClass)) throw new IllegalStateException(singletonClass + " is already registered");
		log.info("Registering {} with instance of hashcode {}", singleton, Integer.toHexString(System.identityHashCode(singleton)));
		this.singletons.put(singletonClass, singleton);
	}

	public <T> T get(@NotNull Class<T> singletonClass) {
		//noinspection unchecked
		return (T) this.singletons.get(singletonClass);
	}

}
