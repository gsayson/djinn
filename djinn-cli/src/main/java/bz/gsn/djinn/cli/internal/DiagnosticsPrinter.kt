package bz.gsn.djinn.cli.internal

import bz.gsn.djinn.compiler.lint.Diagnostic
import bz.gsn.djinn.compiler.lint.Diagnostic.Level
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson

private val moshi = Moshi.Builder().add(DiagnosticJSONAdapter()).build()

class DiagnosticJSONAdapter : JsonAdapter<Diagnostic>() {

    @FromJson
    override fun fromJson(ignore: JsonReader): Diagnostic? {
        return null
    }

    @ToJson
    override fun toJson(writer: JsonWriter, diagnostic: Diagnostic?) {
        if(diagnostic != null) {
            writer.beginObject()
                .name("code")
                .value(diagnostic.code())
                .name("level")
                .value(diagnostic.level().toString())
                .name("message")
                .value(diagnostic.message())
                .name("location")
                .value(diagnostic.location())
                .name("notes")
                .beginArray()
                .run {
                    diagnostic.notes().forEach { writer.value(it) }
                    this
                }
                .endArray()
                .endObject()
        } else {
            writer.nullValue()
        }
    }

}

object DiagnosticsPrinter {

    var json = false

    fun CliktCommand.print(diagnostic: Diagnostic) {
        if(json) {
            echo(moshi.adapter(Diagnostic::class.java).toJson(diagnostic))
        } else {
            echo()
            val ls = diagnostic.level().toString()
            val style = if(diagnostic.level() == Level.WARNING) TextColors.brightYellow else TextColors.red
            echo("""
                ${(TextStyles.bold + style)(ls.lowercase())} [${TextStyles.bold(ls[0].uppercase())}${TextStyles.bold(diagnostic.code().toString().padStart(4, '0'))}] ${diagnostic.message()}
                ${TextColors.gray("--> at ${diagnostic.location()}")}
            """.trimIndent())
            for(note in diagnostic.notes()) {
                echo("${(TextStyles.bold + TextColors.brightMagenta)("note")}: $note")
            }
            echo()
        }
    }

}