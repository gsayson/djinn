package bz.gsn.djinn.tests.compiler;

import bz.gsn.djinn.compiler.DjinnCompiler;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class CompilerTest {

	@Test
	public void x() throws IOException {
		DjinnCompiler.of(
				Path.of("D:\\java\\djinn\\example-project\\target\\classes\\")
		).generateBootstrapper("bz/gsn/djinn/bootstrap/Bootstrapper");
	}

}
