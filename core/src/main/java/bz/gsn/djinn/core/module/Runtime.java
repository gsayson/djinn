package bz.gsn.djinn.core.module;

import bz.gsn.djinn.core.resource.ResourceRegistry;
import org.jetbrains.annotations.NonBlocking;

/**
 * The base class for all runtimes. Runtimes allow processes to run asynchronously in threads.
 * <p>
 * For example, let's say that we want to run a REST API as part of a module.
 * We can write:
 * {@snippet :
 * import bz.gsn.djinn.core.Djinn;
 * import java.lang.invoke.MethodHandle;
 *
 * public class WebModule {
 * 	public record MethodRecord(MethodHandle handle, MethodInfo info) {}
 * 	public class WebRuntime extends Runtime {
 * 		private final List<MethodRecord> handles;
 * 		public WebRuntime(List<MethodRecord> handles) {
 * 			 this.handles = handles;
 * 		}
 *		@Override
 *		public void run() {
 *			WebServer ws = new WebServer(8080);
 * 			ws.handleConnection(conn -> resolveConnection(handles)); // somehow resolve the path and invoke the corresponding method
 * 		}
 * 	}
 * 	public @interface Get {
 * 		String value(); // the path of the REST api
 * 	}
 * 	public static Module webModule() {
 * 		var rt = new WebRuntime();
 * 		return Djinn.module()
 * 			.register(rt);
 * 	}
 * }
 *}
 */
public abstract class Runtime {

	/**
	 * Runs the given {@link Runtime}. This method will be called on a new thread.
	 * @param resourceRegistry The resource registry.
	 * @implNote The {@link java.util.concurrent.ExecutorService ExecutorService} running
	 * this method uses platform threads.
	 */
	@NonBlocking
	public abstract void run(ResourceRegistry resourceRegistry);

}
