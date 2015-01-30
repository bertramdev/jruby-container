/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bertramlabs.plugins.jruby;
import org.jruby.embed.ScriptingContainer;
import org.jruby.embed.PathType;
import java.util.HashMap;
import java.util.Set;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.ArrayList;

/**
* An extension of JRuby's ScriptingContainer that allows the container to run in an isolated gem directory.
* Essentially, provides gemset capabilities to the Jruby runtime in a folder `.jruby-container`
*
* @author David Estes
*/

public class IsolatedScriptingContainer extends org.jruby.embed.ScriptingContainer {
	private String name;
	private String containerPath;
	private File gemDir;

	/**
	* Constructs an IsolatedScriptingContainer for use.
	* @param name The name of the container. Used to isolate gems in a gemset
	*/
	public IsolatedScriptingContainer(String name)  throws IOException  {
		super();
		this.name = name;
		this.initializeEnvironment();
		//TODO: Set environment to point to custom GEM_HOME and JRUBY_HOME
	}

	/**
	* Initializes the environment variables pointing to the container path given by the global config
	*/
	public void initializeEnvironment() throws IOException {
		initializeEnvironment(JrubyContainerConfig.getContainerPath());
	}

	public String getContainerPath() {
		return this.containerPath;
	}

	/**
	* Initializes the environment allowing a custom container path to be specified
	* @param containerPath The path to the folder in which the gemsets should be stored. If the directory does not exist, it will be created.
	*
	*/
	public void initializeEnvironment(String containerPath) throws IOException {
		this.containerPath = containerPath;
		File containerDir = new File(containerPath, this.name);
		if(!containerDir.exists()) {
			containerDir.mkdirs();
		}

		gemDir = new File(containerDir.getCanonicalPath(), "gems");
		if(!gemDir.exists()) {
			gemDir.mkdirs();
		}

		HashMap<String,String> environmentMap = new HashMap();

		environmentMap.put("GEM_HOME", gemDir.getCanonicalPath());

		//TODO: Do we need to actually do this or keep it at META-INF/jruby.home
		// this.setHomeDirectory(containerDir.getCanonicalPath());

		this.setEnvironment(environmentMap);
	}

	/**
	* Installs gems from rubygems into the containerPath.
	* @param gemList Map of gems to install with the key being the gem name and the value being the version (leave blank if you dont want to scope to a specific version)
	*/
	public Boolean installGemDependencies(Map<String,String> gemList) throws IOException  {
		Set<String> gemSet = gemList.keySet();

		ArrayList<String> argList = new ArrayList<String>();
		for(String gemName : gemSet) {
			String gemVersion = gemList.get(gemName);
			if(!isGemInstalled(gemName,gemVersion)) {
				if(gemVersion != null && gemVersion.length() > 0) {
					argList.add(gemName + ":" + gemVersion);
				} else {
					argList.add(gemName);
				}
			}
		}
		if(argList.size() == 0) {
			return true;
		}
		argList.add(0,"install");
		argList.add("--no-ri");
		argList.add("--no-rdoc");
		IsolatedScriptingContainer gemInstall = new IsolatedScriptingContainer(name);
	//	gemInstall.setCompileMode(org.jruby.RubyInstanceConfig.CompileMode.OFF);
		gemInstall.put("ARGV", argList.toArray(new String[argList.size()]));
		gemInstall.initializeEnvironment(containerPath);
		gemInstall.runScriptlet(PathType.CLASSPATH, "META-INF/jruby.home/bin/jgem");
		gemInstall.terminate();
		return true;
	}

	/**
	* Executes a bin script in the GEM_HOME from this container
	* @param path - Binary name of script to be executed
	* @param argv - Arguments that you wish to pass to this runtime script.
	*/
	public Object runBinScript(String path, String[] argv) throws IOException {

		String binPath = new File(gemDir.getCanonicalPath(),"bin").getCanonicalPath();
		String binExec = new File(binPath,path).getCanonicalPath();
		this.put("ARGV", argv);
		
		return this.runScriptlet(PathType.ABSOLUTE, binExec);
		
	}


	/**
	* Detects if a gem is installed in the current container or not.
	* @param gemName - The Name of the gem we are looking for
	* @param version - The specific version, if left blank we ignore.
	*/
	public Boolean isGemInstalled(String gemName, String version) throws IOException {
		File specifications = new File(gemDir.getCanonicalPath(),"specifications");
		if(!specifications.exists()) {
			return false;
		}

		for(File spec : specifications.listFiles()) {
			String name = spec.getName();
			String nameWithoutExtension = name.replace(".gemspec","");
			int splitPosition = nameWithoutExtension.lastIndexOf('-');
			String specGemName = nameWithoutExtension.substring(0,splitPosition);
			String specVersion = nameWithoutExtension.substring(splitPosition+1);
			if(gemName.equals(specGemName)) {
				if(version == null || version.length() == 0 || version.equals(specVersion)) {
					return true;
				}
			}
		}
		return false;
	}
}