# Djinn

> Building today, tomorrow.

Djinn is a framework incorporating minimal *Core Reflection* or classpath searching at runtime.
Those processes are delegated to build scripts.

## Creating an *application*

To create an application which is runnable, you will need to include the dependencies
`bz.gsn.djinn:djinn-core` and `bz.gsn.djinn:djinn-hook`. Additionally, you will need to
install the Djinn CLI.

Once it's set up, let's create a module `FooModule` as follows:

```java
import bz.gsn.djinn.core.module.DjinnModule;

public final class FooModule extends DjinnModule {
	public FooModule() {
		// ...
	}
}
```

Inside, we can register things like `Runtime`s and `AnnotationDetector`s.

- `Runtime`s are classes which execute long-running processes such as web servers, in a platform thread.
- `AnnotationDetector`s are classes which are passed annotations scanned by Djinn.

Let's create an app that will detect a specific annotation on types. In order to do so,
we subclass `AnnotationDetector` like so:

```java
import bz.gsn.djinn.core.module.AnnotationDetector;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Test {
	class Detector extends AnnotationDetector<Test> {
		private FooRuntime rt;
		public AnnotationDetector(FooRuntime rt) {
			this.rt = rt;
        }
		public <V> void handleType(@NotNull Test obj, @NotNull Class<V> type, @NotNull ResourceRegistry resourceRegistry) {
            System.out.println(type);
        }
    }
}
```

Let's create a runtime first:

```java
import bz.gsn.djinn.core.module.ResourceRegistry;
import bz.gsn.djinn.core.module.Runtime;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class FooRuntime extends Runtime {
	public Queue<Class<?>> classes = new ArrayDeque<>();
	private AtomicBoolean atomicBoolean = new AtomicBoolean(true);

	public void run(ResourceRegistry ignore) {
		while(atomicBoolean.get()) System.out.println(classes.poll());
	}
	
	public void stop(ResourceRegistry ignore) {
		atomicBoolean.set(false);
    }
}
```

Now we can register our new `Runtime` like so:
```java
import bz.gsn.djinn.core.module.DjinnModule;

public final class FooModule extends DjinnModule {
	public FooModule() {
		var fr = new FooRuntime();
		this.register(new Test.Detector(fr));
		this.register(fr);
	}
}
```

In order to compile this, we download the Djinn CLI (on the releases page, it's an uber-jar), which will
generate our bootstrapper and bundle our code as a JAR.

Alternatively, we can do `djinn-cli run --classpath=pathToJar,pathToClassDirectory,... -- appArgs` to run
our app.

## Djinn resources

Resources are singleton classes designed to be shared across runtimes and annotation detectors.
They are automatically detected in your project at build-time by the Djinn CLI, and specified
inside the generated bootstrapper.

Resources must extend `Resource` and should be declared `final`. They must also declare a public no-args
constructor, or the CLI will error out during compilation.