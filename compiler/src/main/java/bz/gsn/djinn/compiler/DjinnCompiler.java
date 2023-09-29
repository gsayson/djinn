package bz.gsn.djinn.compiler;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * The Djinn compiler & analyzer.
 */
public final class DjinnCompiler {

	private final Path[] paths;
	private final ScanResult classpath;

	private DjinnCompiler(Path... paths) {
		this.paths = paths;
		this.classpath = new ClassGraph()
				.enableAllInfo()
				.overrideClasspath((Object[]) paths)
				.scan();
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
	private List<String> findClassesExtending(String superclass) {
		return classpath.getSubclasses(superclass).directOnly().parallelStream().map(ClassInfo::getName).toList();
	}

	/**
	 * Generates the bootstrap class holding the {@code main} method
	 * for Djinn.
	 * @param internalName The {@linkplain Type#getInternalName() internal name} of the bootstrap class.
	 */
	public void generateBootstrapper(String internalName) throws IOException {
		var hn = Type.getInternalName(Hook.class);
		var modules = findClassesExtending("bz.gsn.djinn.core.module.DjinnModule");
		var resources = findClassesExtending("bz.gsn.djinn.core.resource.Resource");
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		cw.newClass(Type.getInternalName(Hook.class));
		cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, internalName, null, "java/lang/Object", null);
		MethodVisitor main = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		main.visitCode();
		generateArray(main, modules, String.class, 0);
		generateArray(main, resources, String.class, 1);
		main.visitVarInsn(Opcodes.ALOAD, 0);
		main.visitVarInsn(Opcodes.ALOAD, 1);
		main.visitMethodInsn(Opcodes.INVOKESTATIC, hn, "standardMain", "([Ljava/lang/String;[Ljava/lang/String;)V", false);
		main.visitInsn(Opcodes.RETURN);
		main.visitMaxs(2, 2);
		main.visitEnd();
		var path = this.paths[0].resolve(internalName + ".class");
		Files.createDirectories(path.getParent());
		Files.write(path, cw.toByteArray(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
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
}
