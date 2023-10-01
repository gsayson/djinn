package bz.gsn.djinn.compiler.lint;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a diagnostic.
 */
public interface Diagnostic {

	/**
	 * The level of the diagnostic.
	 */
	enum Level {
		WARNING,
		ERROR
	}

	/**
	 * Returns the level of the diagnostic.
	 * @return the diagnostic's level.
	 */
	@NotNull Level level();

	/**
	 * Returns the diagnostic message.
	 * @return the message of the diagnostic.
	 */
	@NotNull String message();

	/**
	 * Returns the diagnostic location.
	 * @return the location of the diagnostic.
	 */
	@NotNull String location();

	/**
	 * Returns the diagnostic notes.
	 * @return the notes of the diagnostic.
	 */
	@NotNull String @NotNull [] notes();

	/**
	 * Returns the diagnostic code.
	 * @return the diagnostic code.
	 */
	int code();

}
