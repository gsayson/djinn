package foo.bar.resource;

import bz.gsn.djinn.core.resource.Resource;
import foo.bar.TestRuntime;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link Resource} storing singletons.
 */
@SuppressWarnings("unused")
public class SingletonResource extends Resource {

	private static final Logger log = LoggerFactory.getLogger(TestRuntime.class);

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
