package bz.gsn.djinn.compiler.lint;

import io.github.classgraph.ScanResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * A warning lint to ensure that all {@code DjinnModule}s are {@code final}.
 */
public class FinalModuleLint implements Lint {

	@Override
	public void lint(@NotNull ScanResult scanResult, @NotNull DiagnosticEmitter diagnosticEmitter, @Unmodifiable @NotNull List<String> btv) {
		scanResult.getSubclasses("bz.gsn.djinn.core.module.DjinnModule")
				.parallelStream()
				.filter(e -> !e.isFinal())
				.forEach(e -> diagnosticEmitter.warning(
						1,
						"Modules should be marked as final",
						"class " + e.getName(),
						new String[] {
								"Only final classes directly extending DjinnModule are registered",
								"Try marking " + e.getSimpleName() + " as final to remove this warning"
						}
				));
	}

}
