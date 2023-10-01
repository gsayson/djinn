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

Let's create a runtime first:
```java
import bz.gsn.djinn.core.module.ResourceRegistry;
import bz.gsn.djinn.core.module.Runtime;

public class FooRuntime extends Runtime {
	public void run(ResourceRegistry resourceRegistry) {
		this.wait(5000); // wait 5ms
        // in actuality, most useful Runtimes don't terminate
	}
}
```

Now we can register our new `Runtime` like so:
```java
import bz.gsn.djinn.core.module.DjinnModule;

public final class FooModule extends DjinnModule {
	public FooModule() {
		this.register(new FooRuntime());
	}
}
```

**TODO**: Specify how to build Djinn apps.

## Djinn resources

Resources are singleton classes designed to be shared across `Runtime`s and `AnnotationDetector`s.
They are automatically detected in your project at build-time by the Djinn CLI, and specified
inside the generated bootstrapper.

Note that currently, resources' constructor handles are still looked up. In the future, instantiation
may take place directly inside the bootstrapper class, without any lookups.

Resources must extend `Resource` and should be declared `final`. They must also declare a public no-args
constructor, or the CLI will error out during compilation.