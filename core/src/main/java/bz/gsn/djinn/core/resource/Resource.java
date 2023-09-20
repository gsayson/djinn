package bz.gsn.djinn.core.resource;

import bz.gsn.djinn.core.AnnotationDetector;

import java.lang.annotation.*;

/**
 * A singleton resource.
 * <p>
 * Types annotated with this interface must strictly be classes
 * with a default constructor. Djinn keeps track of a singleton resource throughout the
 * execution of the Djinn application. If the aforementioned criteria is not fulfilled,
 * Djinn will crash.
 * <p>
 * In methods, parameters may be annotated with {@code @Resource}, and Djinn will ensure that
 * those parameters are passed properly (and safely, with mutexes). {@linkplain AnnotationDetector Annotation detectors}
 * acting on methods will not be able to see parameters annotated with {@code @Resource}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PARAMETER})
public @interface Resource {
}
