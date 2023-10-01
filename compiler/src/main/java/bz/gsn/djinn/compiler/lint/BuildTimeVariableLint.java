package bz.gsn.djinn.compiler.lint;

import io.github.classgraph.ScanResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * A lint to ensure that:
 * <ul>
 *     <li>build-time variable keys follow the regex {@code [a-zA-Z0-9_-]+(\\.*[a-zA-Z0-9_-]+)*}</li>
 *     <b>build-time variable strings are key-value strings</b>
 * </ul>
 */
public class BuildTimeVariableLint implements Lint {

	@Override
	public void lint(@NotNull ScanResult scanResult, @NotNull DiagnosticEmitter diagnosticEmitter, @Unmodifiable @NotNull List<String> btv) {
		var elements = new HashSet<String>();
		boolean errored = true;
		for(var bt : btv) {
			var split = bt.split("=", 2);
			var loc = "'" + bt.trim() + "'";
			if(split.length != 2) {
				diagnosticEmitter.error(2, "Keys must be followed by a `=` character, then a value", loc, new String[] {"Add a `=` character to separate the key and the value"});
			} else if(!split[0].matches("[a-zA-Z0-9_-]+(\\.*[a-zA-Z0-9_-]+)*")) {
				diagnosticEmitter.error(3, "Keys must match the regex [a-zA-Z0-9_-]+(\\.*[a-zA-Z0-9_-]+)*", loc, new String[] {
						"Modify the key to match the regex",
						"The regex is similar to Java qualifiers",
						"Hyphens and leading digits are also permitted"
				});
			} else {
				errored = false;
			}
		}
		if(!errored) {
			for(var dup : btv.stream().filter(n -> !elements.add(n)).toList()) {
				// now, we detect duplicates; it would be very bad if "" kept appearing!
				diagnosticEmitter.error(4, "Duplicate key " + dup.split("=", 2)[0], "'" + dup.trim() + "'", new String[] {"Use a different key"});
			}
		}
	}

}
