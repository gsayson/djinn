package bz.gsn.djinn.core.app;

import bz.gsn.djinn.core.util.CoreUtils;
import io.github.classgraph.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * A utility class for performing operations on the classpath.
 * Take note that operations in this class may be very expensive.
 */
public final class Classpath {

	private static final ClassGraph cg = new ClassGraph()
			.enableClassInfo()
			.enableMethodInfo()
			.enableAnnotationInfo()
			.disableRuntimeInvisibleAnnotations();

	private Classpath() {
		//no instance
	}

	/**
	 * Returns a {@link Set} of methods annotated with a given annotation.
	 * @param clazz The class of the annotation.
	 * @return the annotated classes.
	 * @param <A> The type of the annotation.
	 */
	@NotNull
	@Unmodifiable
	public static <A extends Annotation> Set<Method> annotatedMethods(@NotNull Class<A> clazz) {
		return CoreUtils.sneakyThrows(() -> {
			try(ScanResult result = cg.scanAsync(Executors.newVirtualThreadPerTaskExecutor(), 10).get()) {
				return result.getClassesWithMethodAnnotation(clazz)
						.parallelStream()
						.map(ClassInfo::getMethodInfo)
						.map(x -> x.parallelStream().map(MethodInfo::loadClassAndGetMethod).collect(Collectors.toUnmodifiableSet()))
						.reduce(new HashSet<>(), (x, y) -> {
							x.addAll(y); // safe: x is known to be modifiable.
							return x;
						});
			}
		});
	}

	@NotNull
	@Unmodifiable
	public static <A> Set<Class<? extends A>> directlyExtendingClasses(@NotNull Class<A> clazz) {
		return CoreUtils.sneakyThrows(() -> {
			try(ScanResult result = cg.scanAsync(Executors.newVirtualThreadPerTaskExecutor(), 10).get()) {
				return result.getSubclasses(clazz)
						.directOnly()
						.loadClasses()
						.stream()
						.map(f -> (Class<? extends A>) f.asSubclass(clazz))
						.collect(Collectors.toUnmodifiableSet());
			}
		});
	}

}
