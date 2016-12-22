#!/bin/bash
#
#   Licensed to the Apache Software Foundation (ASF) under one or more
#   contributor license agreements.  See the NOTICE file distributed with
#   this work for additional information regarding copyright ownership.
#   The ASF licenses this file to You under the Apache License, Version 2.0
#   (the "License"); you may not use this file except in compliance with
#   the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.

if [ "${JAVA_HOME}" = "" ]
then
  echo "Please define the JAVA_HOME environment variable."
  exit
fi

if [ "${GERONIMO_HOME}" = "" ]
then
  echo "Please define the GERONIMO_HOME environment variable."
  exit
fi

export DERBY_PATH=${GERONIMO_HOME}/repository/org/apache/derby
export DERBY_VER=10.4.2.0
export CLASSPATH=${DERBY_PATH}/derby/${DERBY_VER}/derby-${DERBY_VER}.jar
export CLASSPATH=${CLASSPATH}:${DERBY_PATH}/derbynet/${DERBY_VER}/derbynet-${DERBY_VER}.jar
export CLASSPATH=${CLASSPATH}:${DERBY_PATH}/derbytools/${DERBY_VER}/derbytools-${DERBY_VER}.jar
export CLASSPATH=${CLASSPATH}:${DERBY_PATH}/derbyclient/${DERBY_VER}/derbyclient-${DERBY_VER}.jar

echo "Invoking IJ command line tool to create the database and tables...please wait"

${JAVA_HOME}/bin/java -Dij.driver=org.apache.derby.jdbc.ClientDriver -Dij.protocol=jdbc:derby://localhost:1527/ org.apache.derby.tools.ij < Table.ddl

# The following command launches the interactive ij command line utility
#${JAVA_HOME}/bin/java -Dij.driver=org.apache.derby.jdbc.ClientDriver -Dij.protocol=jdbc:derby://localhost:1527/ org.apache.derby.tools.ij 

echo "Table creation complete"
