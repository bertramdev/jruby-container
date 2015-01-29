package com.bertramlabs.plugins.jruby;
import java.util.Map;

class JrubyContainerConfig {
	static Map<String,Object> config;


	public static String getContainerPath() {
		if(config == null) {
			return ".jruby-container";
		}
		String configPath = (String)(JrubyContainerConfig.getConfig().get("containerPath"));
		if(configPath == null) {
			return ".jruby-container";
		} else {
			return configPath;
		}
	}

	public static Map<String,Object> getConfig() {
		return config;
	}
}