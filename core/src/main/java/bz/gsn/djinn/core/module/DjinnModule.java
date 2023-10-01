package bz.gsn.djinn.core.module;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a Djinn module.
 * @implNote Implementors should only declare a constructor and use {@link #register(AnnotationDetector[])}
 * or {@link #register(Runtime...)} to construct modules.
 */
public abstract class DjinnModule {

	private final Set<List<AnnotationDetector<?>>> ad = new HashSet<>();
	private final Set<Runtime> rt = new HashSet<>();

	/**
	 * Register a set of annotation detectors. These annotation detectors are guaranteed
	 * to execute sequentially, compared to multiple calls of this method where the annotation detectors may
	 * not execute sequentially (but instead in parallel, for performance).
	 * @param annotationDetectors The annotation detectors to register.
	 * @throws UnsupportedOperationException if the builder has already been consumed.
	 */
	protected final void register(@NotNull AnnotationDetector<?> @NotNull... annotationDetectors) {
		this.ad.add(List.of(annotationDetectors));
	}

	/**
	 * Register a set of runtimes. These runtimes may execute in parallel.
	 * @param runtimes The runtimes to register.
	 * @throws UnsupportedOperationException if the builder has already been consumed.
	 */
	protected final void register(@NotNull Runtime @NotNull... runtimes) {
		this.rt.addAll(List.of(runtimes));
	}

	/**
	 * Returns an immutable {@link Set} of {@link AnnotationDetector}s.
	 *
	 * @return a {@code Set} of {@linkplain AnnotationDetector annotation detectors}.
	 */
	@NotNull @Unmodifiable
	public final Set<List<AnnotationDetector<?>>> getAnnotationDetectors() {
		return Collections.unmodifiableSet(ad);
	}

	/**
	 * Returns an immutable {@link Set} of {@link Runtime}s.
	 * @return a {@code Set} of {@linkplain Runtime runtimes}.
	 */
	@NotNull
	@Unmodifiable
	public final Set<Runtime> getRuntimes() {
		return Collections.unmodifiableSet(rt);
	}

}
