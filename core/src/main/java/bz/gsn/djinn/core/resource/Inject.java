package bz.gsn.djinn.core.resource;

import java.lang.annotation.*;

/**
 * Applied on {@link Resource} parameters to tell Djinn to automatically fill them in.
 * <p>
 * For example:
 * {@snippet :
 * public void x(FooResource xyz, NotAResource abc) {}
 * }
 * where {@code FooResource extends Resource} and {@code NotAResource extends Object},
 * Djinn would show the same method handle:
 * {@snippet :
 * public void x(FooResource xyz, NotAResource abc) {}
 * }
 * The {@code FooResource} parameter would normally not be filled in. Applying {@code @Inject} changes that:
 * {@snippet :
 * public void x(@Inject FooResource xyz, NotAResource abc) {}
 * }
 * Now, Djinn would show the method handle for this method as:
 * {@snippet :
 * public void x(NotAResource abc);
 * }
 * <p>
 * Annotating {@code @Inject} on non(concrete)-{@link Resource} parameters has no effect.
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {
}
