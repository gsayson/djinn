package bz.gsn.tests.djinn;

import bz.gsn.djinn.core.Djinn;
import bz.gsn.djinn.core.app.AppImpl;
import bz.gsn.djinn.core.app.AppResourceRegistry;
import bz.gsn.djinn.core.module.AnnotationDetector;
import bz.gsn.djinn.core.module.MethodInfo;
import bz.gsn.djinn.core.resource.Inject;
import bz.gsn.djinn.core.resource.Resource;
import bz.gsn.djinn.core.resource.ResourceRegistry;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class AnnotationDetectorTest {

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
	public @interface TestAnnotation {
		String value();
	}

	@TestAnnotation("class")
	public static final class TestAnnotationHolder {
		@TestAnnotation("test1")
		public TestResource resource(@Inject TestResource testResource) {
			return testResource;
		}
	}

	public static final class TestResource extends Resource {}

	public static final class TestAnnotationDetector extends AnnotationDetector<TestAnnotation> {
		private final Map<MethodInfo, MethodHandle> handles;
		private final AtomicReference<Class<?>> clazz;

		public TestAnnotationDetector(AtomicReference<Class<?>> clazz) {
			this.clazz = clazz;
			this.handles = new HashMap<>();
		}
		@Override
		public void handleMethod(@NotNull TestAnnotation obj, @NotNull MethodHandle handle, @NotNull MethodInfo info, @NotNull ResourceRegistry resourceRegistry) {
			handles.put(info, handle);
		}
		@Override
		public <V> void handleType(@NotNull TestAnnotation obj, @NotNull Class<V> type, @NotNull ResourceRegistry resourceRegistry) {
			clazz.set(type);
		}
	}

	@Test
	@SuppressWarnings("OptionalGetWithoutIsPresent")
	public void adScanMethodWithResource() throws Throwable {
		var detector = new TestAnnotationDetector(new AtomicReference<>());
		var resourceRegistry = new AppResourceRegistry(Set.of(TestResource.class));
		AppImpl.runAnnotationDetectors(
				Djinn.module()
						.register(detector)
						.build(),
				resourceRegistry
		);
		var value = detector.handles.entrySet()
				.stream()
				.filter(e -> e.getKey().getName().equals("resource"))
				.findFirst()
				.get()
				.getValue();
		// note: first argument is known to be receiver argument
		var x = new TestAnnotationHolder(); // modules should choose which instantiation strategy to use
		Assertions.assertEquals((TestResource) value.invoke(x), resourceRegistry.getResource(TestResource.class).get());
	}

	@Test
	public void adScanClass() {
		var clazz = new AtomicReference<Class<?>>();
		AppImpl.runAnnotationDetectors(
				Djinn.module()
						.register(new TestAnnotationDetector(clazz))
						.build(),
				new AppResourceRegistry(Set.of(TestResource.class))
		);
		Assertions.assertEquals(clazz.getAcquire(), TestAnnotationHolder.class);
	}

}
