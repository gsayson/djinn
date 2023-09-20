package bz.gsn.djinn.core.app;

import io.github.classgraph.*;
import lombok.SneakyThrows;
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
			.enableAnnotationInfo();

	private Classpath() {
		//no instance
	}

	/**
	 * Returns a {@link Set} of methods annotated with a given annotation.
	 * @return the annotated classes.
	 */
	@NotNull
	@Unmodifiable
	@SneakyThrows
	public static <A extends Annotation> Set<Method> annotatedMethods(Class<A> clazz) {
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
	}

}
