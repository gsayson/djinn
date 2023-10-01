package bz.gsn.djinn.compiler.lint;

import io.github.classgraph.MethodParameterInfo;
import io.github.classgraph.ScanResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A lint to make sure that all Resource classes have a public no-args constructor accessible by Djinn.
 */
public class ResourceConstructorLint implements Lint {

	@Override
	public void lint(@NotNull ScanResult scanResult, @NotNull DiagnosticEmitter diagnosticEmitter, @Unmodifiable @NotNull List<String> btv) {
		scanResult.getSubclasses("bz.gsn.djinn.core.resource.Resource")
				.parallelStream()
				.filter(
						e -> e.getDeclaredConstructorInfo()
								// check for existence of public constructor
								.filter(mi -> mi.getParameterInfo().length == 0 && mi.isPublic())
								.isEmpty() // if it's empty, it can't exist; hence we error out.
				)
				.forEach(e -> {
					var str = new ArrayList<>(List.of("Resources are instantiated by Djinn, so they must have public no-args constructors", "Try creating public " + e.getSimpleName() + "() { ... }"));
					e.getDeclaredConstructorInfo()
							.filter(mi -> mi.getParameterInfo().length != 0 || !mi.isPublic())
							.forEach(mi -> {
								var x = mi.getModifiersStr();
								str.add(
										(x.isBlank() ? "" : x)
												+ " "
												+ e.getSimpleName()
												+ "(" + String.join(", ", Arrays.stream(mi.getParameterInfo()).map(MethodParameterInfo::toString).toArray(String[]::new))
												+ ") cannot be used for resource instantiation"
								);
							});
					diagnosticEmitter.error(
							1,
							"Resources must have public no-args constructors",
							"class " + e.getName(),
							str.toArray(String[]::new)
					);
				});
	}

}
