package org.apache.fulcrum.testcontainer;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.fulcrum.yaafi.container.Container;

import junit.framework.TestCase;

/**
 * Base class for unit tests for components. This version doesn't load the
 * container until the first request for a component. This allows the tester to
 * populate the configurationFileName and roleFileName, possible one per test.
 * 
 * This class uses JUnit 3.
 * 
 * @see BaseUnit4Test
 *
 * @author <a href="mailto:epugh@upstate.com">Eric Pugh</a>
 * @author <a href="mailto:quintonm@bellsouth.net">Quinton McCombs</a>
 * @version $Id$
 */
public class BaseUnitTest extends TestCase {
	public static final String CONTAINER_ECM = "CONTAINER_ECM";
	public static final String CONTAINER_YAAFI = "CONTAINER_YAAFI";

	/** Key used in the context for defining the application root */
	public static final String COMPONENT_APP_ROOT = Container.COMPONENT_APP_ROOT;

	/** Pick the default container to be Yaafi **/
	public static final String containerType = CONTAINER_YAAFI;

	/** Use INFO for ConsoleLogger */
	public static final int defaultLogLevel = ConsoleLogger.LEVEL_INFO;

	/** Container for the components */
	private Container container;

	/** Setup our default configurationFileName */
	private String configurationFileName = "src/test/TestComponentConfig.xml";

	/** Setup our default roleFileName */
	private String roleFileName = "src/test/TestRoleConfig.xml";

	/** Setup our default parameterFileName */
	private String parameterFileName = null;

	/** Set the log level (only works for YAAFI container) */
	private int logLevel = defaultLogLevel;

	/**
	 * Gets the configuration file name for the container should use for this test.
	 * By default it is src/test/TestComponentConfig.
	 * 
	 * @param configurationFileName the location of the config file
	 */
	protected void setConfigurationFileName(String configurationFileName) 
	{
		this.configurationFileName = configurationFileName;
	}

	/**
	 * Override the role file name for the container should use for this test. By
	 * default it is src/test/TestRoleConfig.
	 * 
	 * @param roleFileName location of the role file
	 */
	protected void setRoleFileName(String roleFileName) 
	{
		this.roleFileName = roleFileName;
	}

	/**
	 * Set the console logger level
	 * 
	 * @see org.apache.avalon.framework.logger.ConsoleLogger for debugging levels
	 * @param logLevel set valid logging level
	 */
	protected void setLogLevel(int logLevel) 
	{
		this.logLevel = logLevel;
	}

	/**
	 * Constructor for test.
	 *
	 * @param testName name of the test being executed
	 */
	public BaseUnitTest(String testName) 
	{
		super(testName);
	}

	/**
	 * Clean up after each test is run.
	 */
	protected void tearDown() 
	{
		if (container != null) 
		{
			container.dispose();
		}
		container = null;
	}

	/**
	 * Gets the configuration file name for the container should use for this test.
	 *
	 * @return The filename of the configuration file
	 */
	protected String getConfigurationFileName() 
	{
		return configurationFileName;
	}

	/**
	 * Gets the role file name for the container should use for this test.
	 *
	 * @return The filename of the role configuration file
	 */
	protected String getRoleFileName() 
	{
		return roleFileName;
	}

	/**
	 * Gets the parameter file name for the container should use for this test.
	 *
	 * @return The filename of the role configuration file
	 */
	protected String getParameterFileName() 
	{
		return parameterFileName;
	}

	/**
	 * Returns an instance of the named component. This method will also start the
	 * container if it has not been started already
	 *
	 * @param roleName Name of the role the component fills.
	 * @return instance of the component
	 * @throws ComponentException generic exception
	 */
	protected Object lookup(String roleName) throws ComponentException 
	{
		if (container == null) {
			if (containerType.equals(CONTAINER_ECM)) 
			{
				container = new ECMContainer();
			} 
			else 
			{
				container = new YAAFIContainer(logLevel);
			}
			container.startup(getConfigurationFileName(), getRoleFileName(), getParameterFileName());
		}
		return container.lookup(roleName);
	}

	/**
	 * Helper method for converting to and from Merlin Unit TestCase.
	 * 
	 * @param roleName the role name to resolve
	 * @return the component matching the role
	 * @throws ComponentException generic exception
	 */
	protected Object resolve(String roleName) throws ComponentException 
	{
		return lookup(roleName);
	}

	/**
	 * Releases the component.
	 *
	 * @param component component to be released
	 */
	protected void release(Object component) 
	{
		if (container != null) 
		{
			container.release(component);
		}
	}
}
