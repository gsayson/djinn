package foo.bar;

import bz.gsn.djinn.core.build.BuildEnvironment;
import bz.gsn.djinn.core.module.Runtime;
import bz.gsn.djinn.core.resource.ResourceRegistry;
import foo.bar.annotations.RequestHandler;
import io.undertow.Undertow;
import io.undertow.server.handlers.CanonicalPathHandler;
import io.undertow.util.Headers;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class TestRuntime extends Runtime {

	private final Map<String, MethodHandle> handles;
	private final Map<String, String> contentTypes;

	public TestRuntime(RequestHandler.Detector detector) {
		this.handles = detector.getMethodHandles();
		this.contentTypes = detector.getContentTypes();
	}

	@Override
	public void run(ResourceRegistry resourceRegistry) {
		log.info("I'm from the TestRuntime.");
		System.out.println("Build variables are " + BuildEnvironment.getBuildEnvironment().getProperties());
		log.info("Running Undertow on port 8080");
		Undertow.builder()
				.addHttpListener(8080, "localhost")
				.setHandler(new CanonicalPathHandler(
						exchange -> {
							var log = LoggerFactory.getLogger("path-handler");
							var path = exchange.getRelativePath();
							if(!path.endsWith("/")) path += "/";
							log.info("Requested for {}", path);
							try {
								if(handles.containsKey(path)) {
									exchange.setStatusCode(200);
									log.info("Invoking path handler for {}", path);
									exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, contentTypes.get(path));
									exchange.getResponseSender().send(handles.get(path).invoke().toString()); // static method
								} else {
									exchange.setStatusCode(404);
									exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
									exchange.getResponseSender().send("404!", StandardCharsets.UTF_8);
								}
							} catch(Throwable e) {
								throw new RuntimeException(e);
							}
						}
				))
				.build()
				.start();
	}

}
