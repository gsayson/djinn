package foo.bar;

import bz.gsn.djinn.core.module.Runtime;
import bz.gsn.djinn.core.resource.ResourceRegistry;

public class TestRuntime extends Runtime {

	@Override
	public void run(ResourceRegistry resourceRegistry) {
		System.out.println("Hello, world!");
		// block here and do something, e.g. run a web server
		System.out.println("Exit 2");
	}

}
