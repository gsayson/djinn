package foo.bar.annotations;

import bz.gsn.djinn.core.module.AnnotationDetector;
import bz.gsn.djinn.core.module.MethodInfo;
import bz.gsn.djinn.core.resource.ResourceRegistry;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;
import java.lang.invoke.MethodHandle;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A bare-bones request handler annotation.
 * I'm lazy, so this only works on static methods.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestHandler {

	/**
	 * The path of the handler.
	 */
	String value();

	/**
	 * An annotation describing the content type of the handler.
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@interface ContentType {
		/**
		 * The content type of the handler.
		 */
		String value();
	}

	@Getter
	@Slf4j
	final class Detector extends AnnotationDetector<RequestHandler> {
		private final Map<String, MethodHandle> methodHandles = new HashMap<>(); // we will only just expect a zero-found-args method
		private final Map<String, String> contentTypes = new HashMap<>();
		@SneakyThrows
		@Override
		public void handleMethod(@NotNull RequestHandler obj, @NotNull MethodHandle handle, @NotNull MethodInfo info, @NotNull ResourceRegistry resourceRegistry) {
			ContentType contentType = (ContentType) Arrays.stream(info.getAnnotations())
					.filter(e -> e.annotationType() == ContentType.class)
					.findFirst()
					.orElse(new ContentType() {
						@Override
						public String value() {
							return "text/plain";
						}
						@Override
						public Class<? extends Annotation> annotationType() {
							return ContentType.class;
						}
					});
			log.info("Found method handle with info {}", info);
			var val = new URI(obj.value()).normalize().getPath();
			if(!val.endsWith("/")) val += "/";
			this.methodHandles.put(val, handle);
			this.contentTypes.put(val, contentType.value());
		}
	}

}
