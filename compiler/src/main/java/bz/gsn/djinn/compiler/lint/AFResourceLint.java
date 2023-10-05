package bz.gsn.djinn.compiler.lint;

import io.github.classgraph.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * A warning lint to make sure if all {@code Resource}s are either {@code final} or {@code abstract}.
 * Only {@code final} resources can be queried.
 */
public class AFResourceLint implements Lint {

	@Override
	public void lint(@NotNull ScanResult scanResult, @NotNull DiagnosticEmitter diagnosticEmitter, @Unmodifiable @NotNull List<String> btv) {
		scanResult.getSubclasses("bz.gsn.djinn.core.resource.Resource")
				.parallelStream()
				.filter(e -> !e.isAnonymousInnerClass())
				.filter(e -> !e.isFinal() && !e.isAbstract())
				.forEach(e -> diagnosticEmitter.warning(
						1,
						"Resources should be marked as abstract or final",
						"class " + e.getName(),
						new String[] {
								"Only final classes extending Resource are registered",
								"Try marking " + e.getSimpleName() + " as abstract or final to remove this warning"
						}
				));
	}

}
