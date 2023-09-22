package bz.gsn.djinn.core.util;

import bz.gsn.djinn.core.module.AnnotationDetector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AnnotatedElement;

/**
 * Core utilities for Djinn.
 */
public class CoreUtils {

	private CoreUtils() {
		//no instance
	}

	/**
	 * Returns the type of the given {@link AnnotationDetector}, using a {@link java.lang.invoke.MethodHandle MethodHandle}.
	 * @param annotationDetector The {@code AnnotationDetector} to extract the type from.
	 * @return the class of the {@link AnnotationDetector}'s generic type.
	 * @param <T> The annotation type detected by the given {@code AnnotationDetector}.
	 */
	@NotNull
	@SuppressWarnings("unchecked")
	public static <T extends Annotation> Class<T> getTypeOfAD(@NotNull AnnotationDetector<T> annotationDetector) {
		return sneakyThrows(() -> (Class<T>) MethodHandles.privateLookupIn(AnnotationDetector.class, MethodHandles.lookup())
				.findGetter(AnnotationDetector.class, "type", Class.class)
				.invokeExact(annotationDetector));
	}

	@FunctionalInterface
	public interface ExceptionalSupplier<T> {
		T run() throws Throwable;
	}

	/**
	 * Wraps a code block (which may throw an {@link Exception}).
	 * This is a useful method for lambdas.
	 * @param supplier The code block to execute.
	 * @return a value returned by the code block.
	 * @param <T> The type of the returned value.
	 */
	@SuppressWarnings("CatchMayIgnoreException")
	public static <T> T sneakyThrows(@NotNull ExceptionalSupplier<T> supplier) {
		try {
			return supplier.run();
		} catch(Throwable throwable) {
			doThrow(throwable);
		}
		throw new InternalError("unreachable");
	}

	private static void doThrow(Throwable e) {
		switch(e) {
			case RuntimeException runtimeException -> {
				throw runtimeException;
			}
			case Error error -> {
				throw error;
			}
			default -> {
				doThrow0(e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static <E extends Throwable> void doThrow0(Throwable e) throws E {
		throw (E) e;
	}

	/**
	 * Returns the annotation of an annotation detector present on an {@link AnnotatedElement}, else {@code null} if it's not present.
	 * @param annotationDetector The annotation detector.
	 * @param element The element to detect.
	 * @return an instance of the annotation.
	 * @param <T> The type of the annotation.
	 */
	@Nullable
	public static <T extends Annotation> T getAnnotation(@NotNull AnnotationDetector<T> annotationDetector, @NotNull AnnotatedElement element) {
		return element.getAnnotation(getTypeOfAD(annotationDetector));
	}

}
