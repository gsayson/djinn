package bz.gsn.djinn.core.app;

import bz.gsn.djinn.core.Djinn;
import bz.gsn.djinn.core.module.AnnotationDetector;
import bz.gsn.djinn.core.module.DjinnModule;
import bz.gsn.djinn.core.module.MethodInfo;
import bz.gsn.djinn.core.resource.Inject;
import bz.gsn.djinn.core.resource.Resource;
import bz.gsn.djinn.core.resource.ResourceRegistry;
import bz.gsn.djinn.core.util.CoreUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.lang.annotation.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public final class AppImpl extends Djinn {

	private final Collection<DjinnModule> modules;
	private final AppResourceRegistry resourceRegistry;
	private final ExecutorService runtimeRunner = Executors.newCachedThreadPool(
			Thread.ofPlatform()
					.name("runtime-thread-", 0)
					.factory()
	);

	public AppImpl(Collection<DjinnModule> modules) {
		this.modules = modules;
		this.resourceRegistry = new AppResourceRegistry(Classpath.directlyExtendingClasses(Resource.class));
	}

	/**
	 * Runs the Djinn app.
	 */
	@Override
	public void run() {
		modules.parallelStream().forEach(module -> {
			runAnnotationDetectors(module, resourceRegistry);
			registerRuntimes(module, resourceRegistry);
		});
	}

	/**
	 * Initializes a module.
	 * @param module The module to initialize.
	 */
	@VisibleForTesting
	public static void runAnnotationDetectors(@NotNull DjinnModule module, @NotNull ResourceRegistry resourceRegistry) {
		var anno = module.getAnnotationDetectors();
		final class X<T extends Annotation> {
			private final AnnotationDetector<T> detector;
			private final Class<T> type;
			private final ResourceRegistry resourceRegistry;
			X(AnnotationDetector<T> detector, ResourceRegistry resourceRegistry) {
				this.detector = detector;
				this.type = CoreUtils.getTypeOfAD(this.detector);
				this.resourceRegistry = resourceRegistry;
			}
			private boolean doesNotTarget(ElementType type) {
				var ret = this.type.getAnnotation(Target.class);
				return ret == null || Arrays.stream(ret.value()).noneMatch(e -> e == type);
			}
			void handleMethod(Method method) {
				if(doesNotTarget(ElementType.METHOD)) return;
				var handle = CoreUtils.sneakyThrows(() -> resolveResources(method, resourceRegistry));
				detector.handleMethod(
						Objects.requireNonNull(CoreUtils.getAnnotation(detector, method)),
						handle,
						new MethodInfo(method, handle),
						resourceRegistry
				);
			}
			void handleField(Field field) {
				if(doesNotTarget(ElementType.FIELD)) return;
				var handle = CoreUtils.sneakyThrows(() -> MethodHandles.privateLookupIn(field.getDeclaringClass(), MethodHandles.lookup()).unreflectVarHandle(field));
				detector.handleField(
						Objects.requireNonNull(CoreUtils.getAnnotation(detector, field)),
						handle,
						resourceRegistry
				);
			}
			void handleType(Class<?> type) {
				if(doesNotTarget(ElementType.TYPE)) return;
				detector.handleType(
						Objects.requireNonNull(CoreUtils.getAnnotation(detector, type)),
						type,
						resourceRegistry
				);
			}
		}
		anno.parallelStream()
			.forEach(annotationDetectors -> {
				HashMap<Class<?>, Set<Method>> methodCache = new HashMap<>(); // <Annotation, Detector>
				HashMap<Class<?>, Set<Field>> fieldCache = new HashMap<>();
				HashMap<Class<?>, Set<Class<?>>> typeCache = new HashMap<>();
				for(var annotationDetector : annotationDetectors) {
					var annotationClass = CoreUtils.getTypeOfAD(annotationDetector);
					var x = new X<>(annotationDetector, resourceRegistry); // X is thread-safe by design
					methodCache.computeIfAbsent(annotationClass, ignored -> Classpath.annotatedMethods(annotationClass))
							.parallelStream()
							.forEach(f -> {
								logger.info("Running processor {} on method '{}'", annotationDetector.getClass().getName(), f.getName());
								x.handleMethod(f);
							});
					fieldCache.computeIfAbsent(annotationClass, ignored -> Classpath.annotatedFields(annotationClass))
							.parallelStream()
							.forEach(f -> {
								logger.info("Running processor {} on field '{}'", annotationDetector.getClass().getName(), f.getName());
								x.handleField(f);
							});
					typeCache.computeIfAbsent(annotationClass, ignored -> Classpath.annotatedTypes(annotationClass))
							.parallelStream()
							.forEach(f -> {
								logger.info("Running processor {} on type '{}'", annotationDetector.getClass().getName(), f.getName());
								x.handleType(f);
							});
				}
			});
	}

	private void registerRuntimes(@NotNull DjinnModule module, @NotNull ResourceRegistry resourceRegistry) {
		module.getRuntimes()
				.parallelStream()
				.forEach(e -> this.runtimeRunner.submit(() -> e.run(resourceRegistry)));
	}

	/**
	 * Resolves the resources required by the given {@link Method}.
	 * @param method The {@link Method} to resolve.
	 * @return a new {@link MethodHandle} with all non-{@link Inject @Raw} resource parameters filled in.
	 * @throws IllegalAccessException if the method cannot be accessed.
	 */
	private static MethodHandle resolveResources(Method method, ResourceRegistry resourceRegistry) throws IllegalAccessException {
		MethodHandle handle = MethodHandles.publicLookup().unreflect(method);
		int index = Modifier.isStatic(method.getModifiers()) ? 0 : 1;
		for(Parameter parameter : method.getParameters()) {
			var type = parameter.getType();
			if(Resource.class.isAssignableFrom(type)) {
				if(Arrays.stream(parameter.getDeclaredAnnotations()).anyMatch(param -> param.annotationType() == Inject.class)) {
					var resourceOptional = resourceRegistry.getResource(type.asSubclass(Resource.class));
					if(resourceOptional.isEmpty()) {
						// impossible, we should have scanned it already.
						logger.error("Resource {} is not available", type.getName());
						throw new Error("Unavailable resource " + type.getName());
					} else {
						logger.info("Resolved resource {}", type);
					}
					handle = MethodHandles.insertArguments(handle, index, resourceOptional.orElse(null));
				}
			}
			index++;
		}
		return handle;
	}

}
