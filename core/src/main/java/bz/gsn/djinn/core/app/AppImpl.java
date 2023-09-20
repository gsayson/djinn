package bz.gsn.djinn.core.app;

import bz.gsn.djinn.core.Djinn;
import bz.gsn.djinn.core.module.DjinnModule;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class AppImpl extends Djinn {

	private final Collection<DjinnModule> modules;

	public AppImpl(Collection<DjinnModule> modules) {
		this.modules = modules;
	}

	/**
	 * Runs the Djinn app.
	 */
	@Override
	public void run() {
		modules.parallelStream().forEach(AppImpl::handle);
	}

	private static void handle(@NotNull DjinnModule module) {
		module.getAnnotationDetectors()
				.parallelStream();
	}

}
