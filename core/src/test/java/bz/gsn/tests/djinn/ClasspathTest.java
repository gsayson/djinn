package bz.gsn.tests.djinn;

import bz.gsn.djinn.core.app.Classpath;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

public class ClasspathTest {

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface TestMethodAnnotation {}

	public static class TestClass {
		@TestMethodAnnotation
		public void test() {}
	}

	@Test
	@SneakyThrows
	public void classpathScanMethodAnnotation() {
		Assertions.assertEquals(Classpath.annotatedMethods(TestMethodAnnotation.class), Set.of(TestClass.class.getMethod("test")));
	}

}
