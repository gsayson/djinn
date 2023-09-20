package bz.gsn.djinn.core;

import bz.gsn.djinn.core.app.AppImpl;
import bz.gsn.djinn.core.module.DjinnModule;
import bz.gsn.djinn.core.module.Runtime;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

/**
 * Djinn is a JVM framework powered by annotations.
 */
public abstract sealed class Djinn permits AppImpl {

	/**
	 * Runs the Djinn app.
	 */
	public abstract void run();

	/**
	 * Returns a builder for a Djinn module. Modules expose
	 * annotation detectors, which can be used together with runtimes.
	 * @return a new {@link ModuleBuilder} for building a module.
	 */
	@Contract(value = " -> new", pure = true)
	public static @NotNull ModuleBuilder module() {
		return new ModuleBuilder();
	}

	/**
	 * A builder for building {@linkplain DjinnModule Djinn modules}.
	 * <p>
	 * Djinn modules are a core tenet of the Djinn framework. They expose:
	 * <ul>
	 *     <li>Runtimes, which run asynchronously;</li>
	 *     <li>Annotation detectors, which read annotations on entities.</li>
	 * </ul>
	 * This class is not thread-safe.
	 */
	public static final class ModuleBuilder {

		private final Set<List<AnnotationDetector<?>>> annotationDetectors = new HashSet<>();
		private final Set<Runtime> runtimes = new HashSet<>();
		private boolean consumed = false;

		ModuleBuilder() {}

		/**
		 * Returns whether this builder has already built a module.
		 * @return whether this builder is consumed.
		 */
		public boolean isConsumed() {
			return consumed;
		}

		/**
		 * Register a set of annotation detectors. These annotation detectors are guaranteed
		 * to execute sequentially, compared to multiple calls of this method where the annotation detectors may
		 * not execute sequentially (but instead in parallel, for performance).
		 * @param annotationDetectors The annotation detectors to register.
		 * @return this {@link ModuleBuilder}.
		 * @throws UnsupportedOperationException if the builder has already been consumed.
		 */
		@Contract(value = "_ -> this", mutates = "this")
		public ModuleBuilder register(@NotNull AnnotationDetector<?> @NotNull... annotationDetectors) {
			throwIfConsumed();
			this.annotationDetectors.add(List.of(annotationDetectors));
			return this;
		}

		/**
		 * Register a set of runtimes. These runtimes may execute in parallel.
		 * @param runtimes The runtimes to register.
		 * @return this {@link ModuleBuilder}.
		 * @throws UnsupportedOperationException if the builder has already been consumed.
		 */
		@Contract(value = "_ -> this", mutates = "this")
		public ModuleBuilder register(@NotNull Runtime @NotNull... runtimes) {
			throwIfConsumed();
			this.runtimes.addAll(List.of(runtimes));
			return this;
		}

		/**
		 * Builds the Djinn module. Note that this builder will be consumed. I.e.,
		 * no operations such as this or {@link #register(AnnotationDetector[]) register} will be permitted;
		 * an {@link UnsupportedOperationException} will be thrown.
		 * @return the newly-built {@link DjinnModule}.
		 * @throws UnsupportedOperationException if the builder has already been consumed.
		 */
		public @NotNull DjinnModule build() {
			throwIfConsumed();
			this.consumed = true;
			return new DjinnModule() {
				@NotNull
				@Override
				@Unmodifiable
				public Set<List<AnnotationDetector<?>>> getAnnotationDetectors() {
					return Collections.unmodifiableSet(annotationDetectors);
				}
				@NotNull
				@Override
				@Unmodifiable
				public Set<Runtime> getRuntimes() {
					return Collections.unmodifiableSet(runtimes);
				}
			};
		}

		private void throwIfConsumed() {
			if(this.consumed) throw new UnsupportedOperationException("the builder was already consumed");
		}

	}

}
