module djinn.compiler {
	requires org.objectweb.asm;
	requires org.objectweb.asm.commons;
	requires io.github.classgraph;
	requires djinn.hook;
	requires static org.jetbrains.annotations;
	exports bz.gsn.djinn.compiler;
	exports bz.gsn.djinn.compiler.lint;
}