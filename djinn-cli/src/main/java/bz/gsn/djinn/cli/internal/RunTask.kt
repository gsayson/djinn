package bz.gsn.djinn.cli.internal

import bz.gsn.djinn.cli.action
import bz.gsn.djinn.cli.internal.DiagnosticsPrinter.print
import bz.gsn.djinn.compiler.DjinnCompiler
import bz.gsn.djinn.compiler.lint.Diagnostic
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.*
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.path
import java.net.URLClassLoader
import java.nio.file.Path

class RunTask : CliktCommand(name = "run", help = "Runs a Djinn application and compiles it on-the-fly") {

    private val classpath by option("-c", "--classpath", help = "The classpath, split with a comma ','; may include paths to JARs or directories of classes")
        .path(canBeFile = true, canBeDir = true, mustBeReadable = true, mustExist = true)
        .split(",")
        .default(emptyList())
    private val args by argument().multiple()
    private val buildVars: List<String> by option("-B", "--build-var")
        .help("Sets a build variable.")
        .multiple()

    override fun run() {
        action("Running", "project")
        val classloader = RunnerClassLoader(classpath.toTypedArray())
        val array = classpath.toTypedArray()
        val compiler = DjinnCompiler.of(array[0], *array.sliceArray(1..<array.size))
        buildVars.forEach(compiler::registerBTV)
        val diagnostics: List<Diagnostic> = compiler.lint()
        diagnostics.forEach { print(it) }
        val errors = diagnostics.count { it.level() == Diagnostic.Level.ERROR }
        echo("Completed compilation with $errors errors and ${diagnostics.count() - errors} warnings")
        echo()
        if(diagnostics.any { it.level() != Diagnostic.Level.ERROR }) classloader.bootstrap(compiler, args.toTypedArray())
    }

}

// restrict the application to strictly system classes; don't conflict with this current classpath
internal class RunnerClassLoader(urls: Array<Path>) : URLClassLoader(urls.map { it.toUri().toURL() }.toTypedArray(), getPlatformClassLoader()) {

    init {
        ClassLoader.registerAsParallelCapable()
    }

    /**
     * Creates a bootstrapper class and invokes its main method.
     */
    fun bootstrap(compiler: DjinnCompiler, args: Array<String>) {
        val bootstrap = compiler.generateBootstrapper()
        val clazz = this.defineClass("bz.gsn.djinn.bootstrap.Bootstrapper", bootstrap, 0, bootstrap.size)
        clazz.getDeclaredMethod("main", Array<String>::class.java).invoke(null, args)
    }

}