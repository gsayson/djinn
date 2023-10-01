package bz.gsn.djinn.core.app;

import bz.gsn.djinn.core.build.BuildEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of {@link BuildEnvironment}.
 */
public final class BuildEnvironmentImpl extends BuildEnvironment {

	@Unmodifiable
	private final Map<String, String> propertiesMap;

	/**
	 * Constructor.
	 * @param pairings The pairings, in the form {@code key=value}. {@code key} and {@code value} will be trimmed.
	 *                 The key and value will be split on the <em>first</em> {@code '='} character.
	 */
	BuildEnvironmentImpl(@NotNull String @NotNull [] pairings) {
		var propertiesMap = new HashMap<String, String>();
		for(var pair : pairings) {
			var split = pair.split("=", 2);
			propertiesMap.put(split[0].trim(), split[1].trim());
		}
		this.propertiesMap = Collections.unmodifiableMap(propertiesMap);
	}

	@Override
	public @Unmodifiable Map<@NotNull String, @NotNull String> getProperties() {
		return this.propertiesMap;
	}

}
