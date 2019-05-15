/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.boot.factory;

import com.sun.tools.attach.VirtualMachine;

import org.aspectj.weaver.loadtime.Agent;

import org.springframework.boot.system.ApplicationPid;

/**
 * @author Dave Syer
 *
 */
public class AgentInstaller {

	public static void install() {
		VirtualMachine vm;
		try {
			vm = VirtualMachine.attach(new ApplicationPid().toString());
			vm.loadAgent(findAgent());
			vm.detach();
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private static String findAgent() {
		String location = Agent.class.getProtectionDomain().getCodeSource().getLocation()
				.toString();
		if (location.startsWith("file:")) {
			location = location.substring("file:".length());
		}
		return location;
	}

}
