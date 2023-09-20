module djinn.core {

	exports bz.gsn.djinn.core;
	exports bz.gsn.djinn.core.resource;
	exports bz.gsn.djinn.core.module;

	exports bz.gsn.djinn.core.app to tests.djinn.core;

	requires transitive io.github.classgraph;

	requires static org.jetbrains.annotations;
	requires static lombok;

}