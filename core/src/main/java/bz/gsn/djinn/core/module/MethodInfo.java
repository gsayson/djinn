package bz.gsn.djinn.core.module;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Information on a method.
 */
public final class MethodInfo {

	private final String name;
	private final Class<?>[] thrown;
	private final Parameter[] parameters;
	private final Class<?> returnType;
	private final int modifiers;
	private final Annotation[] annotations;
	private final Class<?> enclosingClass;

	/**
	 * Creates a new {@link MethodInfo}.
	 * @param method The {@link Method} to read from.
	 * @param handle The {@link MethodHandle} to read from. The parameter list length must be
	 *               lesser than or equal to that of the passed {@link Method}.
	 */
	@Contract(pure = true)
	public MethodInfo(@NotNull Method method, @NotNull MethodHandle handle) {
		Parameter[] parameters1;
		this.name = method.getName();
		this.thrown = method.getExceptionTypes();
		parameters1 = method.getParameters();
		this.modifiers = method.getModifiers();
		this.returnType = handle.type().returnType();
		this.annotations = method.getAnnotations();
		this.enclosingClass = method.getDeclaringClass();
		List<Parameter> includedParameters = new ArrayList<>();
		Class<?>[] handleParameters = handle.type().parameterArray();
		int i = 0;
		if(isInstanceMethod()) {
			assert handleParameters.length - 1 <= parameters1.length;
			i = 1;
		} else {
			assert handleParameters.length <= parameters1.length;
		}
		for(; i < parameters1.length; i++) {
			try {
				if(parameters1[i].getType() == handleParameters[i]) {
					includedParameters.add(parameters1[i]);
				}
			} catch(ArrayIndexOutOfBoundsException ignored) {
				break;
			}
		}
		parameters1 = includedParameters.toArray(Parameter[]::new);
		this.parameters = parameters1;
	}

	/**
	 * Retrieves the name of the {@link Method}.
	 * @return the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the class which this {@link Method} is enclosed (declared) in.
	 * @return the enclosing class.
	 */
	public Class<?> getEnclosingClass() {
		return enclosingClass;
	}

	/**
	 * Retrieves the return type of this {@link Method}.
	 * @return the return type.
	 */
	public Class<?> getReturnType() {
		return returnType;
	}

	/**
	 * Retrieves the classes of the {@link Throwable}s declared
	 * by the {@link Method}.
	 * @return the classes of the declared {@link Throwable}s.
	 */
	public Class<?>[] getThrown() {
		return thrown.clone();
	}

	/**
	 * Returns the modifiers on this {@link Method}.
	 * Use {@link java.lang.reflect.Modifier Modifier} to check
	 * for modifiers.
	 * @return the modifiers, as an {@code int}.
	 */
	public int getModifiers() {
		return modifiers;
	}

	/**
	 * Retrieves the parameters of this {@link Method}.
	 * This will <em>not</em> include the receiver argument!
	 * <p>
	 * For example, in:
	 * {@snippet :
	 * class Foo {
	 * 	void bar() {}
	 * }
	 * }
	 * {@code bar} has actually one parameter of type {@code Foo} (referenced through {@code this}), which
	 * is known as the receiver argument. Such arguments are not returned in this parameter array.
	 * @return the parameters.
	 */
	public Parameter[] getParameters() {
		return parameters.clone();
	}

	/**
	 * Returns all the annotations declared on the {@link Method}.
	 * @return the declared annotations.
	 */
	public Annotation[] getAnnotations() {
		return annotations;
	}

	/**
	 * Returns whether this method is not {@code static}; i.e.,
	 * it has a receiver argument.
	 * <p>
	 * If this method returns {@code true}, then the accompanying {@link MethodHandle}'s
	 * first parameter is a receiver argument.
	 * @return whether this method has a receiver argument.
	 */
	public boolean isInstanceMethod() {
		return !Modifier.isStatic(modifiers);
	}
}
