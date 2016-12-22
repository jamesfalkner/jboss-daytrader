/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.daytrader.javaee6.web;

import javax.servlet.*;

import javax.servlet.annotation.WebListener;

import org.apache.geronimo.daytrader.javaee6.core.direct.TradeJPADirect;
import org.apache.geronimo.daytrader.javaee6.utils.Log;

@WebListener
public class TradeJPAContextListener implements ServletContextListener 
{

    //receieve trade web app startup/shutown events to start(initialized)/stop TradeJPADirect
    public void contextInitialized(ServletContextEvent event)
    {
        Log.trace("TradeJPAContextListener:contextInitialized - initializing TradeJPADirect");
        TradeJPADirect.init();
    }
    public void contextDestroyed(ServletContextEvent event)
    {
        Log.trace("TradeJPAContextListener:contextDestroyed - calling TradeJPADirect:destroy()");        
        TradeJPADirect.destroy();
    }

}
