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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.junit.After;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Alternative Base class to {@link BaseUnitTest} for component tests.
 * 
 * This version doesn't load the container until the first request for a
 * component. This allows the tester to populate the configurationFileName and
 * roleFileName, possible one per test.
 * 
 * JUnit 4 Version of BaseUnitTest class.
 * 
 * @see BaseUnitTest
 *
 * @author <a href="mailto:epugh@upstate.com">Eric Pugh</a>
 * @author <a href="mailto:quintonm@bellsouth.net">Quinton McCombs</a>
 * @version $Id$
 */

public class BaseUnit4Test {
	public static final String CONTAINER_ECM = "CONTAINER_ECM";
	public static final String CONTAINER_YAAFI = "CONTAINER_YAAFI";

	/** Key used in the context for defining the application root */
	public static final String COMPONENT_APP_ROOT = Container.COMPONENT_APP_ROOT;

	/** Pick the default container to be YAAFI **/
	private String containerType = CONTAINER_YAAFI;

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

	/** Hash map to store attributes for the test **/
	public Map<String, Object> attributes = new HashMap<String, Object>();

	/** set the Max inactive interval **/
	public int maxInactiveInterval = 0;

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
	 */
	public BaseUnit4Test() 
	{
	}

	/**
	 * Clean up after each test is run.
	 */
	@After
	public void tearDown() 
	{
		if (container != null) {
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
		if (container == null) 
		{
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

	/**
	 * Get a mock requestion
	 *
	 * @return HttpServletRequest a mock servlet request
	 */
	protected HttpServletRequest getMockRequest() 
	{
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpSession session = mock(HttpSession.class);

		doAnswer(new Answer<Object>() 
		{
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable 
			{
				String key = (String) invocation.getArguments()[0];
				return attributes.get(key);
			}
		}).when(session).getAttribute(anyString());

		doAnswer(new Answer<Object>() 
		{
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable 
			{
				String key = (String) invocation.getArguments()[0];
				Object value = invocation.getArguments()[1];
				attributes.put(key, value);
				return null;
			}
		}).when(session).setAttribute(anyString(), any());

		when(session.getMaxInactiveInterval()).thenReturn(maxInactiveInterval);

		doAnswer(new Answer<Integer>() 
		{
			@Override
			public Integer answer(InvocationOnMock invocation) throws Throwable {
				return Integer.valueOf(maxInactiveInterval);
			}
		}).when(session).getMaxInactiveInterval();

		doAnswer(new Answer<Object>() 
		{
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable 
			{
				Integer value = (Integer) invocation.getArguments()[0];
				maxInactiveInterval = value.intValue();
				return null;
			}
		}).when(session).setMaxInactiveInterval(anyInt());

		when(session.isNew()).thenReturn(true);
		when(request.getSession()).thenReturn(session);

		when(request.getServerName()).thenReturn("bob");
		when(request.getProtocol()).thenReturn("http");
		when(request.getScheme()).thenReturn("scheme");
		when(request.getPathInfo()).thenReturn("damn");
		when(request.getServletPath()).thenReturn("damn2");
		when(request.getContextPath()).thenReturn("wow");
		when(request.getContentType()).thenReturn("text/html");

		when(request.getCharacterEncoding()).thenReturn("US-ASCII");
		when(request.getServerPort()).thenReturn(8080);
		when(request.getLocale()).thenReturn(Locale.US);

		when(request.getHeader("Content-type")).thenReturn("text/html");
		when(request.getHeader("Accept-Language")).thenReturn("en-US");

		Vector<String> v = new Vector<String>();
		when(request.getParameterNames()).thenReturn(v.elements());
		return request;
	}

	public String getContainerType() 
	{
		return containerType;
	}

	public void setContainerType(String containerType) 
	{
		this.containerType = containerType;
	}
}
