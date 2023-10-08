@file:JvmName("Main")

package bz.gsn.djinn.cli

import bz.gsn.djinn.cli.internal.CompileTask
import bz.gsn.djinn.cli.internal.DiagnosticsPrinter
import bz.gsn.djinn.cli.internal.RunTask
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * The Djinn CLI.
 */
class DjinnCLI : CliktCommand(printHelpOnEmptyArgs = true, invokeWithoutSubcommand = false) {

    private val printJSON: Boolean by option("-j", "--json-diagnostics", help = "Whether to print diagnostics in JSON").flag()

    override fun run() {
        isJson = printJSON
        DiagnosticsPrinter.json = printJSON
    }

}

internal val moshi: Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
internal var isJson = false

@JsonClass(generateAdapter = true)
private data class ActionSerialized(val action: String, val appendix: String)

internal fun CliktCommand.action(action: String, appendix: String) {
    if(isJson) echo(moshi.adapter(ActionSerialized::class.java).toJson(ActionSerialized(action, appendix)))
    else echo("${(TextStyles.bold + TextColors.brightBlue)(action)} $appendix")
}

fun main(args: Array<String>) = DjinnCLI()
    .subcommands(
        CompileTask(),
        RunTask()
    )
    .main(args)