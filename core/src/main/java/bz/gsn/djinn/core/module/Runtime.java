package bz.gsn.djinn.core.module;

/**
 * The base class for all runtimes. Runtimes allow processes to run asynchronously in threads.
 * <p>
 * For example, let's say that we want to run a web server as part of a module.
 * We can write:
 * {@snippet :
 * public class WebModule {
 * 	public class WebRuntime extends Runtime {
 *		@Override
 *		public void run() {
 *			getDjinn();
 * 		}
 * 	}
 * 	public @interface Get {
 * 		String value(); // the path of the method
 * 	}
 * 	public static Module webModule() {
 * 		var rt = new WebRuntime();
 * 		return Djinn.module()
 * 			.register(rt);
 * 	}
 * }
 * }
 */
public abstract class Runtime implements Runnable {

	public abstract void run();

}
