package foo.bar;

import bz.gsn.djinn.core.build.BuildEnvironment;
import bz.gsn.djinn.core.module.Runtime;
import bz.gsn.djinn.core.resource.ResourceRegistry;

public class TestRuntime extends Runtime {

	@Override
	public void run(ResourceRegistry resourceRegistry) {
		System.out.println("I'm from the TestRuntime.");
		System.out.println("Build variables are " + BuildEnvironment.getBuildEnvironment().getProperties());
	}

}
