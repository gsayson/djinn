package bz.gsn.djinn.compiler;

import bz.gsn.djinn.compiler.lint.Diagnostic;
import bz.gsn.djinn.compiler.lint.DiagnosticEmitter;
import bz.gsn.djinn.compiler.lint.FinalResourceLint;
import bz.gsn.djinn.compiler.lint.ResourceConstructorLint;
import bz.gsn.djinn.hook.Hook;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The Djinn compiler & analyzer.
 */
public final class DjinnCompiler {

	private static final Random random = new SecureRandom();

	private final ScanResult classpath;
	private final ArrayList<String> buildTimeVariables = new ArrayList<>();
	private final String bootstrapName = "bz.gsn.djinn.bootstrap.Bootstrapper";
	private final Path primary;
	private final Path[] paths;

	private DjinnCompiler(Path... paths) {
		this.primary = paths[0]; // guaranteed to exist; see .of(...)
		this.paths = paths;
		this.classpath = new ClassGraph()
				.enableAllInfo()
				.ignoreMethodVisibility()
				.overrideClasspath((Object[]) paths)
				.scan();
	}

	/**
	 * The number of classes.
	 * @return The number of classes.
	 */
	public int classCount() {
		return classpath.getAllClasses().size();
	}

	/**
	 * Returns the qualified class name used for bootstrapping.
	 * It is generated at instantiation-time.
	 * @return the bootstrapper class name.
	 */
	public String getBootstrapName() {
		return bootstrapName;
	}

	/**
	 * Registers a build-time variable. The key must match the regex {@code [a-zA-Z0-9_\\-]+(\\.*[a-zA-Z0-9_-]+)*},
	 * and it will be checked at lint-time.
	 * @param btv The build time variable to register, in the form {@code key=value}.
	 */
	public void registerBTV(@NotNull String btv) {
		this.buildTimeVariables.add(btv);
	}

	/**
	 * Returns a new {@link DjinnCompiler} with the given path.
	 * @param primary The primary classpath to scan.
	 * @param paths The other classpaths of the project to scan.
	 * @return a new {@link DjinnCompiler}.
	 */
	@Contract(value = "_, _ -> new", pure = true)
	public static @NotNull DjinnCompiler of(Path primary, Path... paths) {
		ArrayList<Path> al = new ArrayList<>();
		al.add(primary);
		al.addAll(List.of(paths));
		return new DjinnCompiler(al.toArray(Path[]::new));
	}

	@SuppressWarnings("SameParameterValue")
	public List<String> findClassesExtending(@NotNull String superclass) {
		return classpath.getSubclasses(superclass).directOnly().parallelStream().map(ClassInfo::getName).toList();
	}

	/**
	 * Generates the bootstrap class holding the {@code main} method
	 * for Djinn. The bootstrap class name is taken from {@link #getBootstrapName()}.
	 * @return a byte array containing the classfile of the bootstrap class.
	 */
	private byte @NotNull [] generateBootstrapper() {
		var hn = Type.getInternalName(Hook.class);
		var modules = findClassesExtending("bz.gsn.djinn.core.module.DjinnModule");
		var resources = findClassesExtending("bz.gsn.djinn.core.resource.Resource");
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		cw.newClass(hn);
		cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, this.bootstrapName.replace('.', '/'), null, "java/lang/Object", null);
		MethodVisitor main = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		main.visitCode();
		generateArray(main, modules, String.class, 1);
		generateArray(main, resources, String.class, 2);
		generateArray(main, buildTimeVariables, String.class, 3);
		main.visitVarInsn(Opcodes.ALOAD, 1);
		main.visitVarInsn(Opcodes.ALOAD, 2);
		main.visitVarInsn(Opcodes.ALOAD, 3);
		main.visitMethodInsn(Opcodes.INVOKESTATIC, hn, "standardMain", "([Ljava/lang/String;[Ljava/lang/String;[Ljava/lang/String;)V", false);
		main.visitInsn(Opcodes.RETURN);
		main.visitMaxs(3, 3);
		main.visitEnd();
		return cw.toByteArray();
	}

	/**
	 * Creates a bootstrapped executable JAR file.
	 * The JAR file will be created at the given path.
	 * Note that it will only include everything from the primary classpath passed as the first
	 * non-variadic parameter to {@link #of(Path, Path...)}.
	 * @param path Where the JAR file should be created. If the path doesn't exist, it will be created.
	 *             If the path already exists, it will be deleted and recreated.
	 */
	public void createJAR(@NotNull Path path) throws IOException {
		Files.deleteIfExists(path);
		Files.createFile(path);
		if(!Files.isRegularFile(path)) throw new IllegalArgumentException("Path must point to a file");
		var manifest = new Manifest();
		var mf = manifest.getMainAttributes();
		mf.put(Attributes.Name.MANIFEST_VERSION, "1.0");
		try(var outputStream = new JarOutputStream(Files.newOutputStream(path), manifest); var primaryStream = Files.list(primary)) {
			var bootstrap = Files.write(Path.of(primary.resolve(this.bootstrapName.replace('.', '/')) + ".class"), generateBootstrapper());
			bootstrap.toFile().deleteOnExit();
			primaryStream.forEach(filePath -> {
				try {
					addFileToJAR(filePath, outputStream);
				} catch(IOException e) {
					throw new UncheckedIOException(e);
				}
			});
			Files.delete(bootstrap);
		}
	}

	private void addFileToJAR(Path filePath, JarOutputStream target) throws IOException {
		String name = primary.relativize(filePath).toString().replace("\\", "/");
		var lmt = Files.getLastModifiedTime(filePath).toMillis();
		if(Files.isDirectory(filePath)) {
			if(!name.endsWith("/")) {
				name += "/";
			}
			JarEntry entry = new JarEntry(name);
			entry.setTime(lmt);
			target.putNextEntry(entry);
			target.closeEntry();
			try(var inner = Files.list(filePath)) {
				inner.forEach(p -> {
					try {
						addFileToJAR(p, target);
					} catch(IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			}
		} else {
			JarEntry entry = new JarEntry(name);
			entry.setTime(lmt);
			target.putNextEntry(entry);
			try(var in = Files.newInputStream(filePath)) {
				byte[] buffer = new byte[1024];
				while(true) {
					int count = in.read(buffer);
					if(count == -1) break;
					target.write(buffer, 0, count);
				}
				target.closeEntry();
			}
		}
	}

	@SuppressWarnings("SameParameterValue")
	private static <T> void generateArray(@NotNull MethodVisitor main, @NotNull List<T> strings, Class<T> clazz, int index) {
		main.visitIntInsn(Opcodes.BIPUSH, strings.size());
		main.visitTypeInsn(Opcodes.ANEWARRAY, Type.getInternalName(clazz));
		main.visitVarInsn(Opcodes.ASTORE, index);
		for(int i = 0; i < strings.size(); i++) {
			main.visitVarInsn(Opcodes.ALOAD, index);
			main.visitIntInsn(Opcodes.BIPUSH, i);
			main.visitLdcInsn(strings.get(i));
			main.visitInsn(Opcodes.AASTORE);
		}
	}

	/**
	 * Lints the classes. This should be called after registering all information
	 * such as {@linkplain #registerBTV(String) build-time variables}.
	 * @return a list of {@link Diagnostic}s emitted by the lints.
	 */
	public @NotNull List<@NotNull Diagnostic> lint() {
		var dg = new DCDiagnosticEmitter();
		List.of(
				new FinalResourceLint(), // W0001
				new ResourceConstructorLint() // E0001
		).parallelStream().forEach(e -> e.lint(this.classpath, dg, buildTimeVariables));
		return dg.diagnostics;
	}

	private static final class DCDiagnosticEmitter extends DiagnosticEmitter {

		private final List<Diagnostic> diagnostics = new ArrayList<>();

		private record SimpleDiagnostic(@NotNull Level level, int code, @NotNull String message, @NotNull String location, @NotNull String @NotNull [] notes) implements Diagnostic {}

		@Override
		public void warning(int code, @NotNull String info, @NotNull String location, @NotNull String @NotNull [] notes) {
			this.diagnostics.add(new SimpleDiagnostic(Diagnostic.Level.WARNING, code, info, location, notes));
		}

		@Override
		public void error(int code, @NotNull String info, @NotNull String location, @NotNull String @NotNull [] notes) {
			this.diagnostics.add(new SimpleDiagnostic(Diagnostic.Level.ERROR, code, info, location, notes));
		}

	}

}
