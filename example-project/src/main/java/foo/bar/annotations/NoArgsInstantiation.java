package foo.bar.annotations;

import bz.gsn.djinn.core.module.AnnotationDetector;
import bz.gsn.djinn.core.resource.ResourceRegistry;
import foo.bar.resource.SingletonResource;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Instantiates the annotated type only once, and stores it inside a {@link bz.gsn.djinn.core.resource.Resource Resource}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NoArgsInstantiation {

	class Detector extends AnnotationDetector<NoArgsInstantiation> {
		@Override
		public <V> void handleType(@NotNull NoArgsInstantiation obj, @NotNull Class<V> type, @NotNull ResourceRegistry resourceRegistry) {
			var r = resourceRegistry.getResource(SingletonResource.class);
			assert r.isPresent();
			var k = r.get();
			try {
				var x = MethodHandles.publicLookup().findConstructor(type, MethodType.methodType(void.class));
				k.put(type, type.cast(x.invoke()));
			} catch(NoSuchMethodException | IllegalAccessException e) {
				throw new RuntimeException("Unable to access public no-args constructor; is there one?", e);
			} catch(Throwable e) {
				throw new RuntimeException(e);
			}
		}
	}

}
