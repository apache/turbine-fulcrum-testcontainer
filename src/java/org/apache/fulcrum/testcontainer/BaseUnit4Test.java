package org.apache.fulcrum.testcontainer;

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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
import org.junit.After;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Alternative Base class to {@link BaseUnitTest} for component tests. 
 * 
 * This version doesn't load the container until the
 * first request for a component. This allows the tester to populate the configurationFileName and
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
public class BaseUnit4Test
{
    public static final String CONTAINER_ECM="CONTAINER_ECM";
    public static final String CONTAINER_YAAFI="CONTAINER_YAAFI";

    /** Key used in the context for defining the application root */
    public static String COMPONENT_APP_ROOT = Container.COMPONENT_APP_ROOT;

    /** Pick the default container to be YAAFI **/
    public static String containerType = CONTAINER_YAAFI;

    /** Use INFO for ConsoleLogger */
    public static int defaultLogLevel = ConsoleLogger.LEVEL_INFO;

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
     * Gets the configuration file name for the container should use for this test. By default it
     * is src/test/TestComponentConfig.
     */
    protected void setConfigurationFileName(String configurationFileName)
    {
        this.configurationFileName = configurationFileName;
    }

    /**
     * Override the role file name for the container should use for this test. By default it is
     * src/test/TestRoleConfig.
     */
    protected void setRoleFileName(String roleFileName)
    {
        this.roleFileName = roleFileName;
    }

    /**
     * Set the console logger level,
     */
    protected void setLogLevel(int logLevel) {
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
     * Returns an instance of the named component. Starts the container if it hasn't been started.
     *
     * @param roleName Name of the role the component fills.
     * @throws ComponentException generic exception
     */
    protected Object lookup(String roleName) throws ComponentException
    {
        if (container == null)
        {
            if(containerType.equals(CONTAINER_ECM)){
                container = new ECMContainer();
            }
            else {
                container = new YAAFIContainer(logLevel);
            }
            container.startup(getConfigurationFileName(), getRoleFileName(),getParameterFileName());
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
    
    public Map<String,Object> attributes = new HashMap<String,Object>();
    public int maxInactiveInterval = 0;

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
            public Integer answer(InvocationOnMock invocation) throws Throwable
            {
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
        when(request.getContentType()).thenReturn("html/text");

        when(request.getCharacterEncoding()).thenReturn("US-ASCII");
        when(request.getServerPort()).thenReturn(8080);
        when(request.getLocale()).thenReturn(Locale.US);

        when(request.getHeader("Content-type")).thenReturn("html/text");
        when(request.getHeader("Accept-Language")).thenReturn("en-US");

        Vector<String> v = new Vector<String>();
        when(request.getParameterNames()).thenReturn(v.elements());
        return request;
    }
}
