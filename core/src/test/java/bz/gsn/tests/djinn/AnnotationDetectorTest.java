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
	@Target({ElementType.FIELD, ElementType.METHOD})
	public @interface TestAnnotation {
		String value();
	}

	public static final class TestAnnotationHolder {
		@TestAnnotation("test")
		public void xyz() {}
		@TestAnnotation("test1")
		public TestResource resource(@Inject TestResource testResource) {
			return testResource;
		}
	}

	public static final class TestResource extends Resource {}

	public static final class TestAnnotationDetector extends AnnotationDetector<TestAnnotation> {
		private final AtomicReference<String> value;
		private final Map<MethodInfo, MethodHandle> handles;

		public TestAnnotationDetector(AtomicReference<String> value) {
			this.value = value;
			this.handles = new HashMap<>();
		}
		@Override
		public void handleMethod(@NotNull TestAnnotation obj, @NotNull MethodHandle handle, @NotNull MethodInfo info, @NotNull ResourceRegistry resourceRegistry) {
			value.set(obj.value());
			handles.put(info, handle);
		}
	}

	@Test
	public void adScanMethod() {
		AtomicReference<String> value = new AtomicReference<>();
		AppImpl.runAnnotationDetectors(
				Djinn.module()
						.register(new TestAnnotationDetector(value))
						.build(),
				new AppResourceRegistry(Collections.emptySet())
		);
		Assertions.assertEquals(value.get(), "test");
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

}
