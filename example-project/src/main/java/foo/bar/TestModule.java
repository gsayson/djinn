package foo.bar;

import bz.gsn.djinn.core.module.DjinnModule;

@SuppressWarnings("unused") // Djinn CLI will detect this module
public class TestModule extends DjinnModule {

	public TestModule() {
		this.register(new TestRuntime());
	}

}
