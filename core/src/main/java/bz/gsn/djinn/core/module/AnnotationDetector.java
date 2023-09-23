package bz.gsn.djinn.core.module;

import bz.gsn.djinn.core.resource.ResourceRegistry;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
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
 * public DjinnModule buildingAModule() {
 * 	return Djinn.module()
 * 		.register(new Foo.FooDetector())
 * 		.build();
 * }
 * }
 * <p/>
 * Note that the annotation <b>must</b> have a {@linkplain java.lang.annotation.RetentionPolicy#RUNTIME runtime retention policy}.
 */
public abstract class AnnotationDetector<T extends Annotation> {

	private static final Logger logger = LoggerFactory.getLogger(AnnotationDetector.class);

	/**
	 * The class of the annotation that this {@link AnnotationDetector} detects.
	 */
	protected final Class<T> type;

	@SuppressWarnings("unchecked")
	public AnnotationDetector() {
		ParameterizedType superClass = (ParameterizedType) getClass().getGenericSuperclass();
		this.type = (Class<T>) superClass.getActualTypeArguments()[0];
		var retention = this.type.getAnnotation(Retention.class);
		if(retention == null || retention.value() != RetentionPolicy.RUNTIME) {
			logger.warn("{} does not have a RUNTIME retention policy. It will not be detected.", this.type.getName());
		}
	}

	private void printStub(String entityName) {
		logger.warn("The default {} annotation handler was called. If @{} targets {}s, please override this method.", entityName, this.type.getSimpleName(), entityName);
	}

	/**
	 * Handles a detection of an annotation on <em>methods</em>.
	 * This method may be used to receive a {@link MethodHandle}, and possibly
	 * save it for later invocation. This will only detect {@code public} methods which
	 * are accessible from Djinn.
	 * <p>
	 * The passed {@link MethodHandle} will have {@linkplain java.lang.invoke.MethodHandles#insertArguments(MethodHandle, int, Object...) filled-in}
	 * {@link bz.gsn.djinn.core.resource.Inject @Inject}ed {@link bz.gsn.djinn.core.resource.Resource Resource} parameters. I.e, it is <b>not</b> a <b>direct method handle</b>.
	 * @param obj The instance of the detected annotation.
	 * @param handle The {@link MethodHandle} of the annotated method.
	 * @param info The {@link MethodInfo} of the method handle.
	 * @param resourceRegistry Access to the {@link ResourceRegistry}.
	 * @implNote The default implementation issues a warning.
	 */
	public void handleMethod(@NotNull T obj, @NotNull MethodHandle handle, @NotNull MethodInfo info, @NotNull ResourceRegistry resourceRegistry) {
		printStub("method");
	}

	/**
	 * Handles a detection of an annotation on <em>fields</em>.
	 * This method may be used to receive a {@link VarHandle}, and possibly
	 * save it for later reading and modification.
	 * Fields of any visibility will be detected, in contrast to {@link #handleMethod(Annotation, MethodHandle, MethodInfo, ResourceRegistry) handleMethod}
	 * which only detects {@code public} methods.
	 * @param obj The instance of the detected annotation.
	 * @param varHandle The {@link VarHandle} of the annotated field.
	 * @param resourceRegistry Access to the {@link ResourceRegistry}.
	 * @implNote The default implementation issues a warning.
	 */
	public void handleField(@NotNull T obj, @NotNull VarHandle varHandle, @NotNull ResourceRegistry resourceRegistry) {
		printStub("field");
	}

	/**
	 * Handles a detection of an annotation on <em>types</em>.
	 * This method may be used to receive a {@link Class}, and possibly
	 * save it for later reading.
	 * @param obj The instance of the detected annotation.
	 * @param type The {@link Class} of the annotated type.
	 * @param resourceRegistry Access to the {@link ResourceRegistry}.
	 * @implNote The default implementation issues a warning.
	 */
	public void handleType(@NotNull T obj, @NotNull Class<?> type, @NotNull ResourceRegistry resourceRegistry) {
		printStub("type");
	}

}
