package bz.gsn.djinn.hook;

import bz.gsn.djinn.core.app.AppImpl;
import bz.gsn.djinn.core.app.AppResourceRegistry;
import bz.gsn.djinn.core.module.DjinnModule;
import bz.gsn.djinn.core.resource.Resource;
import bz.gsn.djinn.core.resource.ResourceRegistry;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The class holding Djinn main methods used by the compiler.
 */
public class Hook {

	private static final Logger logger = LoggerFactory.getLogger(Hook.class);

	@SuppressWarnings("unused")
	public static void standardMain(@NotNull String @NotNull [] modules, @NotNull String @NotNull [] resources, @NotNull String @NotNull [] buildVariables) {
		System.out.println("""
				_|_|_|    _|  _|
				_|    _|          _|_|_|    _|_|_|
				_|    _|  _|  _|  _|    _|  _|    _|
				_|    _|  _|  _|  _|    _|  _|    _|
				_|_|_|    _|  _|  _|    _|  _|    _|
				          _|
				        _|""");
		logger.info("Using Djinn standard bootstrap method with {} modules and {} resources detected", modules.length, resources.length);
		logger.info("{} build variables are included inside bootstrapper", buildVariables.length);
		new AppImpl(Arrays.stream(modules).map(e -> {
			try {
				var x = Class.forName(e).asSubclass(DjinnModule.class);
				return (DjinnModule) x.getDeclaredConstructor().newInstance();
			} catch(Exception ex) {
				throw new RuntimeException(ex);
			}
		}).toList(), new InternalRR(List.of(resources)), buildVariables).run();
	}

	private static final class InternalRR extends ResourceRegistry {

		private final ResourceRegistry inner;

		public InternalRR(@NotNull List<String> resources) {
			this.inner = new AppResourceRegistry(
					resources.parallelStream()
							.map(e -> {
								try { return Class.forName(e); }
								catch(Exception ex) { throw new RuntimeException(ex); }
							})
							.map(e -> {
								@SuppressWarnings("UnnecessaryLocalVariable") // type error if we inline
								var x = e.asSubclass(Resource.class);
								return x;
							})
							.collect(Collectors.toUnmodifiableSet())
			);
		}

		/**
		 * Returns an {@link Optional} containing the {@link Resource} stored.
		 * multiple methods or runtimes are modifying the same resource.
		 *
		 * @param resource The class of the required resource.
		 * @return an {@link Optional} holding the {@link Resource}.
		 */
		@Override
		public <T extends Resource> Optional<T> getResource(Class<T> resource) {
			return inner.getResource(resource);
		}
	}

}