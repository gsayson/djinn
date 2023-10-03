package foo.bar;

import bz.gsn.djinn.core.module.DjinnModule;
import foo.bar.annotations.NoArgsInstantiation;
import foo.bar.annotations.RequestHandler;

@SuppressWarnings("unused") // Djinn CLI will detect this module
public class TestModule extends DjinnModule {

	public TestModule() {
		var detector = new RequestHandler.Detector();
		this.register(detector, new NoArgsInstantiation.Detector());
		this.register(new TestRuntime(detector));
	}

}
