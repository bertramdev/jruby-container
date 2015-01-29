# Jruby Container

The Jruby Container is an extension on top of the jruby-complete library. It enables Different scripting containers to run in isolated home/gemset directories given a container name. (i.e. a container initialized with name `test` would store its files in `.jruby-container/test`).

The Jruby Container also provides additional convenience methods for loading dependencies. It is able to fetch gems from rubygems.org via the embedded rubygems library.

##Usage

```groovy
	IsolatedScriptingContainer scriptingContainer = new IsolatedScriptingContainer('test');
	scriptingContainer.installGemDependencies([compass:'1.0.1', sass:''])

	//Then we can execute stuff with these gems
	scriptingContainer.runBinScript('compass',['compile','/path/to/project'] as String[])

```

Gem dependencies are persisted in a container directory (defaulting to `./.jruby-container`). However this can be changed globally via the `JrubyContainerConfig` or on a container level...

```groovy
import com.bertramlabs.plugins.jruby.*;
//globally
JrubyContainerConfig.config.containerPath = "/.my-container-path"

//or at container level
IsolatedScriptingContainer scriptingContainer = new IsolatedScriptingContainer('test');
scriptingContainer.initializeEnvironment("/.my-container-path")
```

## Work To Be Done

* Make sure we only try to install gems to the container that are not already installed
* Add Convenience methods to clean the container directory for a clean slate scenario