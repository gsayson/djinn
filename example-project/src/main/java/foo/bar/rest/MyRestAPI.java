package foo.bar.rest;

import bz.gsn.djinn.core.build.BuildEnvironment;
import bz.gsn.djinn.core.resource.Inject;
import foo.bar.TestResource;
import foo.bar.annotations.RequestHandler;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * An example REST API.
 */
@Slf4j
public class MyRestAPI {

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
