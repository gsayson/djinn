package bz.gsn.djinn.core.resource;

import bz.gsn.djinn.core.module.AnnotationDetector;

/**
 * A singleton resource. Types extending this class will be known to the {@link ResourceRegistry} through classpath scanning.
 * <p>
 * <ul>
 *     <li>
 *         Types extending this class must have a no-args constructor of <em>any visibility</em>. Djinn keeps track of a singleton resource throughout the
 *         execution of the Djinn application. If the aforementioned criteria is not fulfilled, Djinn will crash.
 *     </li>
 *     <li>
 *         Parameters of {@code Resource}s must be concrete. For example, {@code Resource} alone cannot be resolved to any valid resource,
 *         but a non-abstract class extending {@code Resource} can easily be resolved by the {@link ResourceRegistry}.
 *     </li>
 *     <li>
 *         {@code Resource}s must be either {@code abstract} or {@code final}. Currently, this is linted by the
 *         Djinn compiler as a warning; in the future, it may become a hard error!
 *     </li>
 * </ul>
 * <p>
 * {@linkplain AnnotationDetector Annotation detectors} acting on methods will not be able to see {@link Inject @Inject}-ed parameters whose
 * types extend this class.
 * <p>
 * {@code Resource}s may be accessed concurrently. It is up to classes to implement thread-safe functionality, for example
 * using classes in {@link java.util.concurrent}.
 * <p>
 * {@snippet :
 * public final class TestResource extends Resource {
 * 	// public TestResource() {} is implicit, unless the line below is uncommented.
 * 	// public TestResource(String s) {} will crash, if the above line is not uncommented!
 * 	void doSomething() {}
 * }
 *
 * @SomeAnnotationThatCallsThisMethod
 * public void foo(@Inject TestResource resource) {
 * 	// method handlers cannot see the above parameter, it is filled in by Djinn.
 * }
 * }
 * @see Inject
 */
public abstract class Resource {
}
