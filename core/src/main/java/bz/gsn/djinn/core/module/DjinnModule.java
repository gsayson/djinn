package bz.gsn.djinn.core.module;

import bz.gsn.djinn.core.AnnotationDetector;
import bz.gsn.djinn.core.module.Runtime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Set;

/**
 * Represents a Djinn module.
 */
public interface DjinnModule {

	/**
	 * Returns an immutable {@link Set} of {@link AnnotationDetector}s.
	 *
	 * @return a {@code Set} of {@linkplain AnnotationDetector annotation detectors}.
	 */
	@NotNull @Unmodifiable Set<List<AnnotationDetector<?>>> getAnnotationDetectors();

	/**
	 * Returns an immutable {@link Set} of {@link Runtime}s.
	 * @return a {@code Set} of {@linkplain Runtime runtimes}.
	 */
	@NotNull
	@Unmodifiable
	Set<Runtime> getRuntimes();

}
