package bz.gsn.djinn.cli.internal

import bz.gsn.djinn.cli.action
import bz.gsn.djinn.cli.internal.DiagnosticsPrinter.print
import bz.gsn.djinn.compiler.DjinnCompiler
import bz.gsn.djinn.compiler.lint.Diagnostic
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.time.measureTime

/**
 * A compiler task.
 */
internal class CompileTask : CliktCommand(name = "compile", help = "Compiles a Djinn application to a JAR.") {

    private val bootstrap: Boolean by option("-k", "--keep-bootstrap")
        .help("Whether or not the bootstrap class should be regenerated during compilation.")
        .flag()
        .convert { !it }

    private val external: Set<Path> by option("-e", "--external")
        .help("A semicolon-separated set of paths to directories of other classes or JAR files.")
        .path(mustExist = true, mustBeReadable = true, mustBeWritable = true, canBeFile = true, canBeDir = true)
        .split(";")
        .transformAll { it.flatten() }
        .unique()

    private val buildVars: List<String> by option("-B", "--build-var")
        .help("Sets a build variable.")
        .multiple()

    private val outputJar: Path by option("-o", "--output")
        .path(mustExist = false)
        .help("Where the output JAR should be outputted.")
        .required()

    private val primary: Path by argument("primary", "The primary classpath of your project, where the bootstrapper will be created.")
        .path(mustExist = true, mustBeReadable = true, mustBeWritable = true, canBeFile = false, canBeDir = true)
        .default(Path("").toAbsolutePath())

    private val disableWarnings: Set<Int> by option("-a", "--allow")
        .int(false)
        .multiple()
        .unique()

    override fun run() {
        val compiler = DjinnCompiler.of(primary, *external.toTypedArray())
        action("Compiling", "Djinn application at $primary with ${external.size} external classpaths")
        var warnings = 0
        var errors = 0
        val duration = measureTime {
            action("Located", "${compiler.findClassesExtending("bz.gsn.djinn.core.module.DjinnModule", true).size} module(s) in total")
            action("Located", "${compiler.findClassesExtending("bz.gsn.djinn.core.resource.Resource", false).size} resource(s) in total")
            action("Linting", "${compiler.classCount()} classes")
            val diagnostics = compiler.lint().filter { if(it.level() == Diagnostic.Level.WARNING) { !disableWarnings.contains(it.code()) } else true }
            warnings = diagnostics.count { it.level() == Diagnostic.Level.WARNING }
            errors = diagnostics.count { it.level() == Diagnostic.Level.ERROR }
            diagnostics.forEach { print(it) }
            if(bootstrap && errors == 0) {
                buildVars.forEach(compiler::registerBTV)
                action("Embedded", "${buildVars.size} build-time variable(s)")
                val time = measureTime {
                    compiler.createJAR(outputJar)
                }
                action("Bootstrapped", "application in $time using standard hook")
            }
        }
        if(errors == 0) action("Compiled", "Djinn application in $duration with $warnings warning(s) to $outputJar")
        else echo("${(TextStyles.bold + TextColors.red)("Failed")} to compile Djinn application with $warnings warning(s) and $errors error(s)")
    }


}