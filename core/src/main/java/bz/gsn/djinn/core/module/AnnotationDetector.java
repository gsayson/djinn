package bz.gsn.djinn.core.module;

import bz.gsn.djinn.core.resource.ResourceRegistry;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.ParameterizedType;

/**
 * The base class for all <em>annotation detectors</em>.
 * An annotation detector's methods are called when Djinn recursively
 * goes through every annotation declaration, <em>including those in
 * the same module</em>.
 * <p>
 * Let us look at an example:
 * {@snippet :
 * import java.lang.annotation.ElementType;
 * import java.lang.annotation.Target;
 * import java.util.HashSet;
 * import java.util.Set;
 *
 * @Target(ElementType.METHOD)
 * public @interface Foo {
 *	 class FooDetector extends AnnotationDetector<Foo> {
 *		private final Set<MethodHandle> methods = new HashSet<>();
 *		@Override
 *		public void handleMethod(@NotNull Foo obj, @NotNull MethodHandle handle) {
 *			methods.add(handle);
 *		}
 *	 }
 * }
 * }
 * We can then use this annotation detector in building a {@link DjinnModule} as such:
 * {@snippet :
 * public void buildingAModule() {
 * 	Djinn.module()
 * 		.register(new Foo.FooDetector());
 * }
 * }
 * <p/>
 * Note that the annotation <b>must</b> have a {@linkplain java.lang.annotation.RetentionPolicy#RUNTIME runtime retention policy}.
 */
public abstract class AnnotationDetector<T extends Annotation> {

	/**
	 * The class of the annotation that this {@link AnnotationDetector} detects.
	 */
	protected final Class<T> type;

	@SuppressWarnings("unchecked")
	public AnnotationDetector() {
		ParameterizedType superClass = (ParameterizedType) getClass().getGenericSuperclass();
		this.type = (Class<T>) superClass.getActualTypeArguments()[0];
	}

	/**
	 * Handles a detection of an annotation on <em>methods</em>.
	 * This method may be used to receive a method handle, and possibly
	 * save it for later invocation.
	 * @param obj The instance of the detected annotation.
	 * @param handle The method handle of the annotated method.
	 * @implNote The default implementation does nothing.
	 */
	public void handleMethod(@NotNull T obj, @NotNull MethodHandle handle, @NotNull MethodInfo info, @NotNull ResourceRegistry resourceRegistry) {}

}
