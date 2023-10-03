package foo.bar.rest;

import bz.gsn.djinn.core.build.BuildEnvironment;
import bz.gsn.djinn.core.resource.Inject;
import foo.bar.TestResource;
import foo.bar.TestRuntime;
import foo.bar.annotations.RequestHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An example REST API.
 */
public class MyRestAPI {

	private static final Logger log = LoggerFactory.getLogger(TestRuntime.class);

	private MyRestAPI() {
		//no instance
	}

	@RequestHandler("/hello/")
	public static @NotNull String test(@Inject TestResource testResource) {
		log.info("I have a TestResource: {}", testResource);
		return "hello";
	}

	@RequestHandler("/btv/")
	@RequestHandler.ContentType("text/plain")
	public static @NotNull String btv() {
		return BuildEnvironment.getBuildEnvironment().getProperties().toString();
	}

}
