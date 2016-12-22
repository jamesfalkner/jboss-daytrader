@REM   Licensed to the Apache Software Foundation (ASF) under one or more
@REM   contributor license agreements.  See the NOTICE file distributed with
@REM   this work for additional information regarding copyright ownership.
@REM   The ASF licenses this file to You under the Apache License, Version 2.0
@REM   (the "License"); you may not use this file except in compliance with
@REM   the License.  You may obtain a copy of the License at
@REM
@REM      http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM   Unless required by applicable law or agreed to in writing, software
@REM   distributed under the License is distributed on an "AS IS" BASIS,
@REM   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM   See the License for the specific language governing permissions and
@REM   limitations under the License.

@echo off

if not "%JAVA_HOME%" == "" goto JAVA_OK
@echo Please define the JAVA_HOME environment variable.
@goto END
:JAVA_OK

if not "%GERONIMO_HOME%" == "" goto GERONIMO_OK
@echo Please define the GERONIMO_HOME environment variable.
@goto END
:GERONIMO_OK

set DERBY_PATH=%GERONIMO_HOME%\repository\org\apache\derby
set DERBY_VER=10.4.2.0
set CLASSPATH=%DERBY_PATH%\derby\%DERBY_VER%\derby-%DERBY_VER%.jar
set CLASSPATH=%CLASSPATH%;%DERBY_PATH%\derbynet\%DERBY_VER%\derbynet-%DERBY_VER%.jar
set CLASSPATH=%CLASSPATH%;%DERBY_PATH%\derbytools\%DERBY_VER%\derbytools-%DERBY_VER%.jar
set CLASSPATH=%CLASSPATH%;%DERBY_PATH%\derbyclient\%DERBY_VER%\derbyclient-%DERBY_VER%.jar

@echo "Invoking IJ command line tool to create the database and tables...please wait"

"%JAVA_HOME%\bin\java" -Dij.driver=org.apache.derby.jdbc.ClientDriver -Dij.protocol=jdbc:derby://localhost:1527/ org.apache.derby.tools.ij < Table.ddl

@REM The following command launches the interactive ij command line utility
@REM java -Dij.driver=org.apache.derby.jdbc.ClientDriver -Dij.protocol=jdbc:derby://localhost:1527/ org.apache.derby.tools.ij 

@echo Table creation complete
 
:END
