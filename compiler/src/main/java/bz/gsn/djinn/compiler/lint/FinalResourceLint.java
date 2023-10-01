package bz.gsn.djinn.compiler.lint;

import io.github.classgraph.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * A warn lint to make sure if all {@code Resource}s are {@code final}.
 */
public class FinalResourceLint implements Lint {

	@Override
	public void lint(@NotNull ScanResult scanResult, @NotNull DiagnosticEmitter diagnosticEmitter, @Unmodifiable @NotNull List<String> btv) {
		scanResult.getSubclasses("bz.gsn.djinn.core.resource.Resource")
				.parallelStream()
				.filter(e -> !e.isFinal())
				.forEach(e -> diagnosticEmitter.warning(
						1,
						"Resources should be marked as final",
						"class " + e.getName(),
						new String[] {
								"Only classes directly extending Resource are registered",
								"Try marking " + e.getSimpleName() + " as final to remove this warning"
						}
				));
	}

}
