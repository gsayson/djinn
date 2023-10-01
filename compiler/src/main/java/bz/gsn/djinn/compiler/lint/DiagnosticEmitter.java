package bz.gsn.djinn.compiler.lint;

import org.jetbrains.annotations.NotNull;

/**
 * An emitter for diagnostics.
 */
public abstract class DiagnosticEmitter {

	/**
	 * Emits a warning.
	 * @param code The warning code.
	 * @param info The message of the warning.
	 * @param location The location of the warning. For example, {@code class foo.bar.Xyz} or {@code method foo.Bar#bar}.
	 * @param notes The notes of the warning.
	 */
	public abstract void warning(
			int code,
			@NotNull String info,
			@NotNull String location,
			@NotNull String @NotNull [] notes
	);

	/**
	 * Emits a warning.
	 * @param code The warning code.
	 * @param info The message of the warning.
	 * @param location The location of the warning. For example, {@code class foo.bar.Xyz} or {@code method foo.Bar#bar}.
	 * @param notes The notes of the warning.
	 */
	public abstract void error(
			int code,
			@NotNull String info,
			@NotNull String location,
			@NotNull String @NotNull [] notes
	);

}
