# Logging Capacity

[:Outline:]

The built-in capacity `[:loggingcap]{Logging}` provides tools for printing messages in the log associated to the agent.

		<!--- Test that all the documented functions are defined in the capacity, and no function is missed to be
		      documented --> 
		[:Fact:]{typeof(io.sarl.core.[:loggingcap!]).shouldHaveMethods(
			"[:fcterror](error)(java.lang.Object, java.lang.Throwable, java.lang.Object[])",
			"[:fcterror!](java.lang.Object, java.lang.Object[])",
			"[:fctwarning](warning)(java.lang.Object, java.lang.Throwable, java.lang.Object[])",
			"[:fctwarning!](java.lang.Object, java.lang.Object[])",
			"[:fctinfo](info)(java.lang.Object, java.lang.Object[])",
			"[:fctdebug](debug)(java.lang.Object, java.lang.Object[])",
			"[:fctgetloglevel](getLogLevel) : int",
			"[:fctsetloglevel](setLogLevel)(int)",
			"[:fctiserrorlogenabled](isErrorLogEnabled) : boolean",
			"[:fctiswarninglogenabled](isWarningLogEnabled) : boolean",
			"[:fctisinfologenabled](isInfoLogEnabled) : boolean",
			"[:fctisdebuglogenabled](isDebugLogEnabled) : boolean",
			"[:fctsetloggingname](setLoggingName)(java.lang.String)")
		}


## Print an error or a warning message

For printing an error or a warning message, the two following functions
are provided:

		[:Success:]
			package io.sarl.docs.reference.bic
			interface Tmp {
			[:On]
				def [:fcterror!]([:msgparam](message) : Object, [:exceptionparam](exception) : Throwable = null, [:arguments](arguments) : Object*)
				def [:fctwarning!](message : Object, exception : Throwable = null, arguments : Object*)
			[:Off]
			}
		[:End:]

The [:msgparam:] parameter is converted to a string for obtaining the message to output.
The message is built from the parsing of the message in which parameter constants aree replaced by the [:arguments:].
The [:exceptionparam:] parameter may be given for printing an exception that is the cause of the logging action.

Examples:

		[:Success:]
			package io.sarl.docs.reference.bic
			import io.sarl.core.Logging
			[:On]
			agent A {
				uses [:loggingcap!]
				def myaction {
					[:fcterror!]("mymessage")
					[:fcterror!]("mymessage", new Exception)
					[:fctwarning!]("mymessage")
					[:fctwarning!]("mymessage", new Exception)
				}
			}
		[:End:]


## Print an information message

For printing an information message, the following function is provided:

		[:Success:]
			package io.sarl.docs.reference.bic
			interface Tmp {
			[:On]
				def [:fctinfo!]([:msgparam!] : Object, [:arguments](arguments) : Object*)
			[:Off]
			}
		[:End:]

The [:msgparam:] parameter is converted to a string for obtaining the message to output.
The message is built from the parsing of the message in which parameter constants aree replaced by the [:arguments:].

Example:

		[:Success:]
			package io.sarl.docs.reference.bic
			import io.sarl.core.Logging
			[:On]
			agent A {
				uses [:loggingcap!]
				def myaction {
					[:fctinfo!]("mymessage")
				}
			}
		[:End:]


## Print a debugging message

For printing a debugging message, the following function is provided:

		[:Success:]
			package io.sarl.docs.reference.bic
			interface Tmp {
			[:On]
				def [:fctdebug!]([:msgparam!] : Object, arguments : Object*)
			[:Off]
			}
		[:End:]

The [:msgparam:] parameter is converted to a string for obtaining the message to output.
The message is built from the parsing of the message in which parameter constants aree replaced by the [:arguments:].

Example:

		[:Success:]
			package io.sarl.docs.reference.bic
			import io.sarl.core.Logging
			[:On]
			agent A {
				uses [:loggingcap!]
				def myaction {
					[:fctdebug!]("mymessage")
				}
			}
		[:End:]


## Retrieve and change the logging level

The printable messages are associated to a level of logging (error, warning, info, debug).
If a message is given to the logging system, and the current output level is lower
than the message's level, then the message is not output.

For retrieving the current logging level, the following function is provided:

		[:Success:]
			package io.sarl.docs.reference.bic
			interface Tmp {
			[:On]
				def [:fctgetloglevel!] : int
			[:Off]
			}
		[:End:]


The replied value is 0 when no message is printed, 1 if only error messages are printed, 2 for error and warning messages, etc.

For changing the current logging level, the following function is provided:

		[:Success:]
			package io.sarl.docs.reference.bic
			interface Tmp {
			[:On]
				def [:fctsetloglevel!](level : int)
			[:Off]
			}
		[:End:]

Example:

		[:Success:]
			package io.sarl.docs.reference.bic
			import io.sarl.core.Logging
			[:On]
			agent A {
				uses [:loggingcap!]
				def myaction {
					var l = [:fctgetloglevel!]
					[:fctsetloglevel!]( l + 1 )
				}
			}
		[:End:]


## Testing the logging level

The following functions permits testing if a specific logging level is enabled:

		[:Success:]
			package io.sarl.docs.reference.bic
			interface Tmp {
			[:On]
				def [:fctiserrorlogenabled!] : boolean
				def [:fctiswarninglogenabled!] : boolean
				def [:fctisinfologenabled!] : boolean
				def [:fctisdebuglogenabled!] : boolean
			[:Off]
			}
		[:End:]

Example:

		[:Success:]
			package io.sarl.docs.reference.bic
			import io.sarl.core.Logging
			[:On]
			agent A {
				uses [:loggingcap!]
				def myaction : boolean {
					   [:fctiserrorlogenabled!]
					|| [:fctiswarninglogenabled!]
					|| [:fctisinfologenabled!]
					|| [:fctisdebuglogenabled!]
				}
			}
		[:End:]


## Change the name of the logger

By default, the logging message contains the identifier of the agent associated to the [:loggingcap:] capacity.

Sometimes, it is helpful to change the printed name of the agent.
The following function gives the opportunity to change this name.

		[:Success:]
			package io.sarl.docs.reference.bic
			interface Tmp {
			[:On]
				def [:fctsetloggingname!](name : String)
			[:Off]
			}
		[:End:]

Example:

		[:Success:]
			package io.sarl.docs.reference.bic
			import io.sarl.core.Logging
			[:On]
			agent A {
				uses [:loggingcap!]
				def myaction {
					[:fctsetloggingname!]("the name of the agent")
				}
			}
		[:End:]



[:Include:](../../legal.inc)
