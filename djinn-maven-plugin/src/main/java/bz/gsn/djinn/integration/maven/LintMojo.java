package bz.gsn.djinn.integration.maven;

import bz.gsn.djinn.compiler.DjinnCompiler;
import bz.gsn.djinn.compiler.lint.Diagnostic;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/**
 * Goal which merely lints bytecode.
 */
@Mojo(name = "lint", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class LintMojo extends AbstractMojo {

	private final Log log;
	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	public LintMojo() {
		this.log = getLog();
	}

    public void execute() {
		var deps = project.getArtifacts();
        log.info("Linting " + project.getId() + " with " + deps.size());
		var list = deps.parallelStream()
				.map(Artifact::getFile)
				.filter(File::isFile)
				.filter(f -> f.getName().endsWith(".jar"))
				.map(File::getAbsolutePath)
				.map(Path::of)
				.toList();
		DjinnCompiler compiler = DjinnCompiler.of(Path.of(project.getBuild().getDirectory()), list.toArray(Path[]::new));
		List<Diagnostic> diagnostics = compiler.lint();
		diagnostics.forEach(e -> {
			Consumer<String> diagnosticLogger = switch(e.level()) {
				case WARNING -> log::warn;
				case ERROR -> log::error;
			};
			diagnosticLogger.accept(e.level().name().toLowerCase() + " [" + String.format("%04d", e.code()) + "] " + e.message());
			for(String note : e.notes()) diagnosticLogger.accept("note: " + note);
		});
		long errors = diagnostics.stream().filter(e -> e.level() == Diagnostic.Level.ERROR).count();
		log.info("Completed lint with " + (diagnostics.size() - errors) + " warnings and " + errors + " errors");
	}

}
