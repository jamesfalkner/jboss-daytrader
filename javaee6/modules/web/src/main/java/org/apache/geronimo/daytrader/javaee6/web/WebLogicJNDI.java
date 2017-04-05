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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Hashtable;

public class WebLogicJNDI {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws Exception, ServletException, IOException {
        response.setContentType("text/plain");
        ServletOutputStream out = response.getOutputStream();
        out.println("JNDI=" + lookupService());
    }

    private Service lookupService() throws Exception {
        try {
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put( Context.PROVIDER_URL, "t3://localhost:7001" );
            env.put( Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory" );
            env.put( Context.SECURITY_PRINCIPAL, "weblogic" );
            env.put( Context.SECURITY_CREDENTIALS, "weblogic" );
            Context context = new InitialContext( env );
            Service service = (Service)context.lookup("sample.Service#" + ServiceImpl.class.getName());
            return service;
        } catch (NamingException e) {
            return null;
        }
    }

}
