module djinn.core {

	exports bz.gsn.djinn.core;
	exports bz.gsn.djinn.core.resource;
	exports bz.gsn.djinn.core.module;
	exports bz.gsn.djinn.core.build;

	exports bz.gsn.djinn.core.app to djinn.hook, tests.djinn.core;

	requires io.github.classgraph;
	requires transitive org.slf4j;

	requires static transitive org.jetbrains.annotations;

}