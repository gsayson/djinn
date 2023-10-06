package bz.gsn.djinn.compiler.lint;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * A lint to check for anonymous classes extending {@code Resource}.
 */
public class AnonymousResourceLint implements Lint {

	@Override
	public void lint(@NotNull ScanResult scanResult, @NotNull DiagnosticEmitter diagnosticEmitter, @Unmodifiable @NotNull List<String> btv) {
		scanResult.getSubclasses("bz.gsn.djinn.core.resource.Resource")
				.parallelStream()
				.filter(ClassInfo::isAnonymousInnerClass)
				.forEach(e -> diagnosticEmitter.warning(
						3,
						"Anonymous subclassing of resources",
						"class " + e.getName(),
						new String[] {
								"Only named non-anonymous classes extending Resource are registered"
						}
				));
	}

}
