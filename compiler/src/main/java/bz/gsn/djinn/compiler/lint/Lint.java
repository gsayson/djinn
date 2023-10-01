package bz.gsn.djinn.compiler.lint;

import io.github.classgraph.ScanResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface Lint {

	/**
	 * Lints the given {@link ScanResult}.
	 * @param scanResult The {@link ScanResult} to look for items to lint.
	 * @param diagnosticEmitter The {@link DiagnosticEmitter}.
	 * @param btv The build-time variables.
	 */
	void lint(@NotNull ScanResult scanResult, @NotNull DiagnosticEmitter diagnosticEmitter, @Unmodifiable @NotNull List<String> btv);

}
