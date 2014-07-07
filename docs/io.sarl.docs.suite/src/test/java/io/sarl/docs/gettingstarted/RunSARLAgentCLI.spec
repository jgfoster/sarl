/*
 * Copyright 2014 Sebastian RODRIGUEZ, Nicolas GAUD, Stéphane GALLAND.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.sarl.docs.gettingstarted

import com.google.inject.Inject
import io.sarl.docs.utils.SARLParser
import io.sarl.docs.utils.SARLSpecCreator
import org.jnario.runner.CreateWith

/* For running an agent, you must launch this agent on the runtime 
 * environment.
 * This document explains how to launch an agent on
 * the [Janus platform](http://www.janusproject.io).
 */
@CreateWith(SARLSpecCreator)
describe "Run SARL Agent from the Command Line" {
	
	@Inject extension SARLParser

	/* The Janus platform provides a `Boot` class.
	 * For launching the platform, you must execute this
	 * boot class in a Java Virtual Machine.
	 * 
	 * The typical command line is:
	 * 
	 *     java -cp app.jar io.janusproject.Boot
	 * 
	 * 
	 * The option `-cp` specifies the Jar file that contains
	 * the compiled classes. The given `app.jar`
	 * file is a Jar file that is containing the Janus
	 * platform, the SARL libraries, and the application classes.
	 * The last parameter is the fully qualified name of
	 * the booting class of Janus: `io.janusproject.Boot`
	 */
	describe "Boot of Janus" { } 

	/* The example given in the previous section causes an error.
	 * Indeed, it is mandatory to specify the fully qualified name
	 * of the agent to launch:
	 * 
	 *     java -cp app.jar io.janusproject.Boot myapp.MyAgent
	 *
	 * 
	 * <span class="label label-danger">Important</span> The Janus
	 * platform allows to start only one agent from the command line.
	 * If you want to start a collection of agents, you must select
	 * one of the following approaches:
	 * 
	 *  * launch a separate Janus platform for each agent, or
	 *  * launch an agent that is spawning the other agents.
	 */
	describe "Specify the Agent to Launch" { } 
	
	/* It is possible to give parameters to the launched agent.
	 * Indeed, all the parameters given on the command line
	 * are put in the `parameters` attribute of the `Initialize` event.
	 * This event is fired when the launched agent is started.
	 */
	describe "Command Line Parameters" {

		/* The following example gives the values `FirstParam` and
		 * `SecondParam` to the launched agent:
		 * 
		 *     java -cp app.jar io.janusproject.Boot myapp.MyAgent FirstParam SecondParam
		 *
		 *
		 * @filter(.*) 
		 */		
		fact "Give Parameters to the Agent" {
			true
		}
		
		/* For retreiving the values passed on the command line,
		 * you must handle the `Initialize` event, as illustrated
		 * by the following example:
		 * 
		 * @filter(.* = '''|'''|.parsesSuccessfully.*) 
		 */
		fact "Retreive the Command Line Parameters in the Agent" {
			val model = '''
				agent MyAgent {
					on Initialize {
						println("Command line parameters: "
							+occurrence.parameters)
					}
				}
			'''.parsesSuccessfully(
				"package io.sarl.docs.gettingstarted.runsarlagent
				import io.sarl.core.Initialize",
				// TEXT
				""
			)
			model.mustHavePackage("io.sarl.docs.gettingstarted.runsarlagent")
			model.mustHaveImports(1)
			model.mustHaveImport(0, "io.sarl.core.Initialize", false, false, false)
			model.mustHaveTopElements(1)
			var a = model.elements.get(0).mustBeAgent("MyAgent", null).mustHaveFeatures(1)
			a.features.get(0).mustBeBehaviorUnit("io.sarl.core.Initialize", false)
		}

		/* The Janus platform provides a collection of command line options.
		 * For obtaining the list of these options, you should type:
		 * 
		 *     java -cp app.jar io.janusproject.Boot --help
		 *
		 *
		 * @filter(.*) 
		 */		
		fact "Janus Command Line Options" {
			true
		}

	} 

}