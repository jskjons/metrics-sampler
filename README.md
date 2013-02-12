Overview
========
metrics-sampler is a java program which regularly queries metrics from a configured set of inputs, selects and renames them using regular expressions and sends them to a configured set of outputs. It supports JMX and JDBC as inputs and Graphite as output out of the box. Writing new extensions containing new inputs, outputs, samplers and selectors is pretty straight-forward.

Example Configuration
---------------------
Check out the following configuration as a quick-start:

	<configuration>
		<!-- here we include all XML files in the selectors/ directory (relative to the location of the configuration file). Includes can be used in any file (so an
		     included file may include further files itself. Typically included files are just fragments of the whole configuration - they are just parsed and then merged
		     into one XML representation in memory which is then evaluated. As a result you can refer to elements defined in other files. -->
		<includes>
			<!-- the location is actually a glob pattern so you can use ** to denote any number of directories, e.g. fragments/**/*.xml
			<include location="selectors/*.xml" />
		</includes>
	
		<!-- here we define stuff that is shared between many consumers like pools -->
		<shared-resources>
			<!-- this is the thread pool used by all samplers by default (unless you explicitly specify a thread pool in the sampler) --> 
			<thread-pool name="samplers" size="10" />
			<!-- this is a custom thread pool that some of the samplers will use -->
			<thread-pool name="custom.samplers" size="2" />
			<!-- specify a jdbc connection pool of 1 to 5 connections to an oracle DB -->
			<jdbc-connection-pool name="oracle01" url="jdbc:oracle:thin:@//oracle1.metrics-sampler.org:1521/EXAMPLE" username="user" password="password" driver="oracle.jdbc.OracleDriver" min-size="1" max-size="5" />
		</shared-resources>
		<inputs>
			<!-- this is an example of a template - its attributes and children will be copied to any input that references it 
				 using the "template" attribute. It is important to set template=true so that it can never be used in a sampler 
				 and is not forced to define all mandatory fields -->
			<jmx name="wls-template" template="true" username="admin" password="weblogic1" provider-packages="weblogic.management.remote" persistent-connection="true">
				<!-- we can choose to ignore certain object names using a list of regular expressions -->
				<ignore-object-names>
					<ignore-object-name regexp="^com\.oracle\.jrockit:type=Flight.+" />
				</ignore-object-names>
				<!-- a map of properties to pass to the JMX connector factory. You usually do not need this.
				<connection-properties>
					<entry key="jmx.remote.x.request.waiting.timeout" value="100" />
				</connection-properties>
				<!-- Using the socket-options you can configure some low level socket options for the RMI connections - 
				     most notably the SO_TIMEOUT (in ms) and the socket connection timeout (in ms) -->
				<socket-options connect-timeout="100" so-timeout="200" keep-alive="false" send-buffer-size="16384" receive-buffer-size="16384" />
				<!-- You can also define variables in the inputs (or their templates) -->
				<variables>
					<string name="tomcat.port" value="8080" /> 
				</variables>
			</jmx>
			
			<!-- WebLogic JMX server. These inputs inherit all properties (username, password, variables, ignores, etc.)
		         from the template input named "wls-template" -->
			<jmx name="wls01" url="service:jmx:t3://weblogic1.metrics-sampler.org:6001/jndi/weblogic.management.mbeanservers.runtime" parent="wls-template" />
			<jmx name="wls02" url="service:jmx:t3://weblogic2.metrics-sampler.org:6001/jndi/weblogic.management.mbeanservers.runtime" parent="wls-template" />
			
			<!-- Tomcat JMX server -->
			<jmx name="tomcat01" url="service:jmx:rmi:///jndi/rmi://tomcat.metrics-sampler.org:7001/jmxrmi" persistent-connection="true" />

			<!-- Execute the given query(ies) over JDBC and use the first column as metric name, the second as metric value and the 
				 third one as timestamp. You will need to have the JDBC drivers in the lib/ directory -->
			<jdbc name="oracle01" pool="oracle01">
				<query>select replace(T2.host_name||'.'||T2.instance_name||'.'||replace(replace(replace(replace(metric_name,'/',''),'%','Perc'),'(',''),')',''),' ','_') as metric, value, (25200 + round((end_time - to_date('01-JAN-1970','DD-MON-YYYY')) * (86400),0))*1000 as dt from gv$sysmetric T1, gv$instance T2 where T1.intsize_csec between 1400 and 1600 and T1.inst_id = T2.INST_ID</query>
			</jdbc>
			
			<!-- Apache mod_status page -->
			<apache-status name="apache01" url="http://apache1.metrics-sampler.org:80/qos-viewer?auto" username="user" password="pass" />
			
			<!-- use metrics from the "info" command on the given redis instance. also count the number of elements in the hash with key "hash1" and the size of the list with key "list1" -->
			<redis name="redis" host="redis.metrics-sampler.org" port="6379">
				<redis-hlen database="0" key="hash1" />
				<redis-llen key="list1" />
			</redis>

			<!-- fetch metrics from the perfmaps of the given oracle NoSQL hosts -->			
			<oracle-nosql name="oracle-nosql" hosts="kv1.metrics-sampler.org:5000 kv2.metrics-sampler.org:5000 kv3.metrics-sampler.org:5000 kv4.metrics-sampler.org:5000" />
			
			<!-- self-monitoring of the metrics-sampler. you can configure what you want as usual in the sampler and send the data the any output -->
			<self name="self" />
		</inputs>
		<outputs>
			<!-- Write to the standard output -->
			<console name="console" />
			<!-- Send to graphite running on port 2003. This is the default output - if no outputs are given in the samplers, all outputs marked as default will be used -->
			<graphite name="graphite" host="graphite.metrics-sampler.org" port="2003" default="true" />
		</outputs>
		
		<!-- we can also define some global variables that will be available in all samplers (unless overridden) -->
		<variables>
			<string name="tomcat.port" value="8080" />
		</variables>
		
		<!-- we define some regular expressions in groups so that we can reuse them later in the samplers -->
		<selector-groups>
			<selector-group name="wls">
				<!-- from-name is a regular expression that is matched against e.g. the JMX Metric Name (consisting of canonical 
					 object name # attribute name). The string can also contain references to variables in the form ${name}. 
				     to-name is an expression (not a regular expression) that can use variables for things like captured groups 
				     from the name's regular expression. -->
				<regexp from-name="com\.bea:Name=DataSource_(.+),ServerRuntime=.+,Type=JDBCOracleDataSourceRuntime\.(ActiveConnectionsAverageCount|ActiveConnectionsCurrentCount|ActiveConnectionsHighCount|ConnectionDelayTime|ConnectionsTotalCount|CurrCapacity|CurrCapacityHighCount|FailuresToReconnectCount|HighestNumAvailable|HighestNumUnavailable|LeakedConnectionCount|NumAvailable|NumUnavailable|ReserveRequestCountWaitSecondsHighCount|WaitingForConnection.*)" to-name="${prefix}.jdbc.${name[1]}.${name[2]}" />
				<regexp from-name="com\.bea:Name=JTARuntime,ServerRuntime=.*,Type=JTARuntime\.(.*TotalCount)" to-name="${prefix}.jta.${name[1]}" />
				<regexp from-name="com\.bea:Name=ThreadPoolRuntime,ServerRuntime=.*,Type=ThreadPoolRuntime\.(CompletedRequestCount|ExecuteThreadIdleCount|ExecuteThreadTotalCount|HoggingThreadCount|MinThreadsConstraintsCompleted|MinThreadsConstraintsPending|PendingUserRequestCount|QueueLength|SharedCapacityForWorkManagers|StandbyThreadCount|Throughput)" to-name="${prefix}.threads.${name[1]}"/>
				<regexp from-name="com\.bea:Name=.*,ServerRuntime=.*,Type=JRockitRuntime\.(JvmProcessorLoad|TotalGarbageCollectionCount|TotalGarbageCollectionTime|FreePhysicalMemory|UsedPhysicalMemory|Uptime)" to-name="${prefix}.jrockit.${name[1]}" />
			</selector-group>
			<selector-group name="tomcat">
				<!-- note that you can use variables in the from-name too (in this case tomcat.port). These must be explicitly 
					 defined in the sampler (or in the input / global context) -->
				<regexp from-name="Catalina:type=GlobalRequestProcessor,name=http-${tomcat.port}.\.(requestCount|bytesSent|bytesReceived)" to-name="${prefix}.http.${name[1]}"/>
			</selector-group>
			<selector-group name="mod_qos">
				<regexp from-name=".*,metric=([^,]+),path=/([^.]+)\.(current|limit)" to-name="${prefix}.${name[2]}.${name[1]}.${name[3]}"/>
				<regexp from-name=".*,metric=([^,]+)$" to-name="${prefix}.${name[1]}"/>
				<regexp from-name=".*,metric=([^,]+)\.(current|limit)" to-name="${prefix}.${name[1]}.${name[2]}"/>
			</selector-group>
		</selector-groups>
		
		<!-- These are the actual active runtime components that sample the date from the given input, use the given selectors to 
		     determine which metrics are relevant (and rename them) and to send them to the given outputs. An input/output without a 
		     sampler does not do anything or consume any resources. The samplers are scheduled at a constant rate (with the given 
		     interval) to a thread pool of the size defined above. -->
		<samplers>
			<!-- template defining common values for weblogic samplers. If you define any of the attributes / child elements in the 
				 samplers that use this template, these values here will be lost (not appended to).
				 The reset-timeout is an optional attribute. If set to any positive value (the time unit is seconds) it will cause the
				 sampler to keep track of the number of selected metrics after each reconnect of its input. If this number of selected 
				 metrics differs, a reset of the input and the selectors will be reset after a random timeout in the range 
				 between 0.8*reset-timeout and 1.2*reset-timeout. This is useful if you are sampling e.g. a JMX input which takes long to
				 startup and exposes a partial number of matching JMX beans during startup.   
			 -->
			<sampler name="wls" template="true" interval="10" reset-timeout="60">
				<selectors>
					<use-group name="wls" />
				</selectors>
				<!-- these variables can override the ones defined in the global context and the ones defined in the input -->
				<variables>
					<string name="prefix" value="backend.${input.name}" />
				</variables>
			</sampler>
			
			<!-- fetch data from wls01 input, use the regular expressions in a group named "wls" to select and rename metrics and 
				 send them to graphite every 10 seconds. If you specify a child element (e.g. selectors) its value will replace
			     the values from the template - lists of selectors will not be merged but rather replaced -->
			<sampler input="wls01" parent="wls" />
			<sampler input="wls02" parent="wls" />
			
			<sampler input="tomcat01" interval="10">
				<variables>
					<string name="prefix" value="frontend.${input.name}" />
					<!-- we override the global variable here -->
					<string name="tomcat.port" value="9080" />
				</variables>
				<selectors>
					<use-group name="tomcat" />
				</selectors>
			</sampler>

			<!-- setting quiet to true causes the sampler to log connection problems using debug level - thus preventing the problem 
				 to be logged in the standard configuration. This is useful if the input is a source that is not always available but 
				 you want to still get metrics when it is available while not flooding your logs with exceptions. -->
			<sampler input="apache01" interval="10" quiet="true">
				<variables>
					<string name="prefix" value="frontend.${input.name}" />
				</variables>
				<selectors>
					<use-group name="mod_qos" />
				</selectors>
			</sampler>

			<!-- you can use ignored="true" to completely skip a sampler without removing / commenting it out. Note that it still needs to be valid. this sampler also uses a custom thread pool named "custom.samplers" -->
			<sampler input="oracle01" interval="10" ignored="true" pool="custom.samplers">
				<variables>
					<string name="prefix" value="database.${input.name}" />
				</variables>
				<selectors>
					<!-- we can of course specify regular expressions directly here too. -->
					<regexp from-name="(.*)" to-name="${name[1]}"/>
				</selectors>
			</sampler>
			
			<sampler input="redis" interval="10">
	 			<selectors>
	 				<regexp from-name="(.*)" to-name="${name[1]}" />
	 			</selectors>
			</sampler>
			
			<sampler input="oracle-nosql" interval="10">
	 			<selectors>
	 				<regexp from-name="(.*)" to-name="${name[1]}" />
	 			</selectors>
			</sampler>
		</samplers>
	</configuration>

Shared Resources
----------------
* JDBC connection pools to use with e.g. the jdbc input. Utilizes c3p0 under the hood.
* Thread pools for the samplers that make it possible to distribute the samplers on different thread pools. You will need to define at least one called "samplers".

Supported Inputs
-----------------
* Java Management Extensions (JMX) - queries object names and attributes from a remote JMX server. The reader caches all meta-data until a reconnect. The name of the metrics consist of the canonicalized object name + '#' + attribute name.
* JDBC - sequentially execute a list of SQL queries and interpret the returned rows as metrics. The reader currently does not reuse the data-base connection between samplings. Queries must return either two or three columns - the first one is the metric's name and the second one is its value. The optional third one is a timestamp (in milliseconds since epoch start).
* apache-status - parses the output of the apache and mod_qos status page (with option ?auto) and exposes the values in a more usable format. The reader uses non-persistent HTTP connection and queries both metadata and data when opened.
* oracle-nosql - fetches the perfmap from a list of hosts/ports running in an Oralce NoSQL (KVStore) cluster and exposes the values in a more usable format as metrics. The reader caches the RMI connections and only reloads them in case of failures.
* redis - executes the info command using jedis and exposes the parsed values as metrics. Keeps the connection until a failure is detected.
* self - expose metrics on the samplers and the input readers

Supported Selectors
-------------------
* Regular expressions selector
Matches metrics by their names using regular expressions. Each metric can then be renamed using expressions which can refer to the input's name and the matching groups of the regular expressions.

Supported Outputs
-----------------
* Console (STDOUT)
* Graphite [http://graphite.wikidot.com]

Variables
---------
Variables can be defined in the global context, in the inputs and in the samplers. Additionally there are some variables that are automatically generated by the inputs like input.name. If a variable with the same name is defined in multiple contexts, its value will be taken from the definition in the most specific context - global variables will be overridden by variables defined in the inputs and in the samplers. Variables defined in an input will be overridden by variables defined in the samplers.  

Quick start
===========
1. Download the metrics-sampler-distribution-<version>-all.tar.gz
2. Unpack it into a directory of your choice, e.g. metrics-sampler-<version>
3. Create a configuration in config/config.xml using config/config.xml.example and this readme as starting point and reference
4. If you want to list all the metrics from your configured inputs you can call "bin/metrics-sampler.sh metadata". This will output all names and descriptions of the available metrics for each input. You can use those to define your regexp selectors. 
5. If you have to tune some startup parameter create the file bin/local.sh and override the environment variables there (e.g. JAVA or JAVA_OPTS)
6. Run "bin/metrics-sampler.sh check" to verify that each selector of each enabled sampler matches at least one metric. You can also run the script with "test" to fetch just one sample.
7. Start the daemon using "bin/metrics-sampler.sh start". Logs are located in logs/metrics-sampler.log and in logs/console.out
8. To check whether the daemon is running execute "bin/metrics-sampler.sh status". The output should be clear enough. If you want to process the result - exit code 0 means running, anything else means stopped.
9. You can stop the daemon using "bin/metrics-sampler.sh stop"
10. Additional configuration
* if you want to use a JVM that is not on the path and/or change the startup parameters you can create an executable bash script in bin/local.sh and set the JAVA and JAVA_OPTS variables. This file (if existing and executable) automatically gets sources into the startup script. Using this will help you keep customizations and default startup separate and thus ease up the upgrade.
* if you need additional JARs on your classpath (e.g. for JDBC drivers, T3 protocol, etc). you should create a directory lib.local and put them there. This way you can safely delete all JARs in lib before upgrading.
* if you want to tune the logging configuration then save it in the file config/logback.xml (config/logback-console.xml for the non-daemon commands like check, metadata, etc.)
 
Extensions
==========
It should be pretty easy to extend the program with new inputs, outputs, samplers and selectors. For this you will need to create a new module/project like this (you could also check out the extensions-* modules which use the same mechanism):
* Add metrics-sampler-core to the classpath of your program/module (e.g. maven dependency)
* Create the file "META-INF/services/org.metricssampler.service.Extension" in src/main/resources (or in any location that lands in your compiled JAR) containing the fully qualified class name of a class that implements org.metricssampler.service.Extension
* Your org.metricssampler.service.Extension implementation will return your custom XBeans (XML configuration beans) which are just normal POJOs with XStream annotations
* You will have to implement an org.metricssampler.service.LocalObjectFactory (e.g. by extending org.metricssampler.service.AbstractLocalObjectFactory) so that you can create the actual input readers, output writers etc. from their configurations
* Put the resulting JAR file on your classpath and you are ready to go (e.g. copy it to the lib/ directory of your installation)
* If you think the extension might be of any use to anyone else - please share it.

Internals
=========
* I chose to use slf4j in all classes with logback under the hood as it is pretty simple to configure
* The graphite writer currently disconnects on each sampling but could be improved to keep the connection (or even better let that be configurable)
* XStream is used to load the XML configuration. The XML is mapped to *XBean instances which are basically POJOs with the some added abilities like validating their data and converting themselves to the configuration format independent *Config POJOs. The *Config POJOs are value objects used by the rest of the system (e.g. samplers, readers, writers, selectors).
* You will need to install some artifacts in your maven repository to be able to build using maven because some of the required artifacts (e.g. the oracle nosql kvstore jars)
 
Compatibility
=============
* Tested with Hotspot/JRockit JVM 1.6
* Tested with Tomcat 7 and Weblogic Server 12c (provided that wlfullclient.jar (or the jmx client and t3 protocol JARs) is on the classpath)
* You might need to add -Dsun.lang.ClassLoader.allowArraySyntax=true as JVM parameter in the metrics-sampler.sh script if you are connecting using JVM 1.6 client to a JVM 1.5 server
