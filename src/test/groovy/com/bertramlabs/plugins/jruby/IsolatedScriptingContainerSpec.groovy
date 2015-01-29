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

import spock.lang.Specification
import org.jruby.RubyInstanceConfig.CompileMode
/**
 * @author David Estes
 */
class IsolatedScriptingContainerSpec extends Specification {

    void "should install a gem file successfully"() {
    	given:
    		def scriptingContainer = new IsolatedScriptingContainer('test');
            //scriptingContainer.setCompileMode(CompileMode.OFF)
    		def gemList = [compass: '1.0.1']
    	when:
    		def result = scriptingContainer.installGemDependencies(gemList);
    	then:
	    	result == true
    }

    void "should be able to execute compass after installed"() {
    	given:
    		def scriptingContainer = new IsolatedScriptingContainer('test');
    		def gemList = [compass: '1.0.1']
    	when:
    		// scriptingContainer.installGemDependencies(gemList);
    		def result = scriptingContainer.runBinScript("compass",['compile','compass-test-project'] as String[])
    	then:
	    	result != null
    }
}