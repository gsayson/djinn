package bz.gsn.djinn.core.build;

import bz.gsn.djinn.core.app.BuildEnvironmentImpl;
import org.jetbrains.annotations.*;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * The build-time environment.
 */
@ApiStatus.Experimental
public abstract sealed class BuildEnvironment permits BuildEnvironmentImpl {

	@SuppressWarnings("unused") // filled in by reflection
	private static BuildEnvironment buildEnvironment;

	/**
	 * Returns the {@link BuildEnvironment} which contains properties of the compiled objects.
	 * @return the build-time environment.
	 */
	@NotNull
	@SuppressWarnings("unused")
	public static BuildEnvironment getBuildEnvironment() {
		return Objects.requireNonNull(buildEnvironment);
	}

	/**
	 * Returns the properties of the build-time environment. The following properties
	 * are included by default (unless excluded by the user):
	 * <ul>
	 *     <li>{@code djinn.core.build-time} returns a {@linkplain java.text.SimpleDateFormat#format(Date) ISO-formatted} date.</li>
	 * </ul>
	 * @return the build-time environment properties.
	 */
	@Unmodifiable
	public abstract Map<@NotNull String, @NotNull String> getProperties();

	/**
	 * Retrieves a property denoted by the given {@link String} key.
	 * @return the designated property, else {@code null} if it does not exist.
	 * @implNote The default implementation retrieves properties from {@link #getProperties()}.
	 */
	public @Nullable String getProperty(String key) {
		return getProperties().get(key);
	}

	@Override
	public final String toString() {
		return getProperties().toString();
	}
}
