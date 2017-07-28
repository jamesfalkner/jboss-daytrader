DayTrader on Red Hat JBoss EAP
==============================
This project is a port of the Apache Geronimo project's DayTrader application to run on [JBoss EAP](https://developers.redhat.com/products/eap/overview/) 6 (Java EE 6)
and JBoss EAP 7 (Java EE 7). It can be built and deployed to a standalone JBoss EAP instance or deployed to
[Red Hat OpenShift](https://access.redhat.com/documentation/en/openshift-container-platform/) using the supplied OpenShift template.

The original project's goal was to benchmark Java EE performance across different vendor implementations by
featuring code built on Java EE technologies like Java Servlets and JavaServer Pages (JSPs) for the presentation
layer and Java database connectivity (JDBC), Java Message Service (JMS), Enterprise JavaBeans (EJBs)
and Message-Driven Beans (MDBs) for the back-end business logic and persistence layer.

This project specifically targets Red Hat JBoss EAP and OpenShift, and does not build other optional components for other
app servers.



Deploying to OpenShift
----------------------
The easiest way to deploy this app is using Red Hat OpenShift. It is assumed you have access to an OpenShift instance,
and have basic familiarity with the `oc` command line tool. To deploy to OpenShift, follow these steps:

1. Clone this repo
```
git clone https://github.com/jamesfalkner/jboss-daytrader
```

- Login and create a new project in OpenShift to hold your app
```
oc login -u <username>
oc new-project daytrader
```
- Ensure you have the JBoss imagestreams defined in the `openshift` namespace (assuming you have the need and permission to do so)
```
oc replace -n openshift --force -f https://raw.githubusercontent.com/openshift/openshift-ansible/master/roles/openshift_examples/files/examples/v1.4/xpaas-streams/jboss-image-streams.json
```
- Deploy the app using the template
```
cd jboss-daytrader
oc process -f openshift-template.yaml | oc create -f -
```
- Ensure the app has cluster `view` access so that JBoss EAP can properly form a cluster when scaled to more than 1 instance
```
oc policy add-role-to-user view system:serviceaccount:$(oc project -q):eap-service-account -n $(oc project -q)
```
This will deploy DayTrader using the containerized JBoss EAP 7 xPaaS image, backed by an ephemeral MySQL instance.

If you wish to switch to using JBoss EAP 6.4, simply update the BuildConfig to build with EAP 6.4:

    oc patch bc/web -p '{"spec":{"strategy":{"sourceStrategy":{"from":{"name":"jboss-eap64-openshift:1.4"}}}}}'

This will cause a new build and eventually new deployment to be kicked off. If it does not, you can force it with `oc start-build web`.
To switch back, just roll back to the previous deployment (e.g. `oc rollback web`).

Once the app is deployed, you can access it via the deployed OpenShift Route, with `/daytrader` appended (this is the
context to which the app is deployed in JBoss EAP). For example, if your project is named `daytrader` and your OpenShift
default routing suffix is `foo.com` then you could access DayTrader using `http://web-daytrader.foo.com/daytrader`.

Builds can take a long time due to the copious amounts of Maven dependencies that must be downloaded. If you wish to save build time, you can setup a Maven Mirror (e.g. using Sonatype's Nexus Repository or JFrog's Artifactory) and point at it
using the `MAVEN_MIRROR_URL` OpenShift template parameter. For example:
```
oc process -f openshift-template.yaml MAVEN_MIRROR_URL=http://nexus.ci:8081/repositories/maven-all | oc create -f -
```


Initialize DayTrader
--------------------
Before using the app, you must create and populate the Database using these steps.

1. Navigate to the DayTrader app in browser. You should see the welcome screen.
2. Navigate to *Configuration* and click *(Re)-create DayTrader Database Tables and Indexes* to create the tables.
3. Scale the `web` pod count to 0 and back to 1 to reboot EAP (e.g. `oc scale --replicas=0 dc/web && oc scale --replicas=1 dc/web`)
5. Navigate to *Configuration* and click *(Re)-populate DayTrader Database*

Then you can go to *Trading & Portfolios*, login with the default user ( username: `uid:0` password: `xxx`), click on ticker symbols (such as `s:130`) and click *buy*, or head to *Portfolio* and sell stocks.

There are other things you can do too, like switch it from using direct JDBC to using (stateless) EJBs, and change some other parameters via the *Configuration* -> *Configure DayTrader run-time parameters*.

Consult the [original DayTrader docs](http://geronimo.apache.org/GMOxDOC22/daytrader-a-more-complex-application.html) for more detail on what you can do!

Installing to standalone JBoss EAP
==================================
If you don't want to (or cannot) use OpenShift, you can manually install to any JBoss EAP instance using these steps:

1. Configure JBoss EAP
2. Build and deploy the app to JBoss EAP

Configure JBoss EAP
---------------------
It is assumed you have some knowledge of JBoss EAP administration tasks. Follow these steps to configure JBoss EAP:

JBoss EAP must be run using the *Full* profile (not the *Web* profile, which is the default). The Full profile includes
support for all of Java EE, on which this demo depends.

In the following steps, if you are manually editing JBoss EAP configuration, be sure to make your changes in the
`standalone/configuration/standalone-full.xml` file.

1. [Install a JDBC driver](https://access.redhat.com/documentation/en/red-hat-jboss-enterprise-application-platform/7.0/single/configuration-guide/#jdbc_drivers) for your database. JBoss EAP ships
with a JDBC driver for the H2 database, which can be used if desired. An example entry for MySQL in JBoss EAP's configuration file:
```
<driver name="mysql" module="com.mysql">
     <driver-class>com.mysql.jdbc.Driver</driver-class>
     <xa-datasource-class>com.mysql.jdbc.jdbc2.optional.MysqlXADataSource</xa-datasource-class>
</driver>
```
- [Create a DataSource](https://access.redhat.com/documentation/en/red-hat-jboss-enterprise-application-platform/7.0/single/configuration-guide/#adding_datasources). The data source must be discoverable via a JNDI name of `java:/jdbc/TradeDataSource` (these align with the mappings in `javaee6/modules/web/src/main/webapp/WEB-INF/web.xml`) Connection parameters must be defined to point at your database via the JDBC driver, with appropriate credentials and database name. An example entry for MySQL in JBoss EAP's configuration file:
```
<datasource jndi-name="java:/jdbc/TradeDataSource" pool-name="trader" enabled="true">
     <connection-url>jdbc:mysql://localhost:3306/tradedb</connection-url>
     <driver>mysql</driver>
     <security>
         <user-name>daytrader</user-name>
         <password>daytrader</password>
     </security>
     <validation>
         <exception-sorter class-name="org.jboss.jca.adapters.jdbc.extensions.mysql.MySQLExceptionSorter"/>
     </validation>
</datasource>
```
If you are using the supplied OpenShift template, then the database, users and permissions will be setup with MySQL for you. Otherwise you must ensure the database is created before starting the app, and a user exists with appropriate DB permissions and a password according to your datasource definition.

- [Create a JMS Queue and Topic](https://access.redhat.com/documentation/en/red-hat-jboss-enterprise-application-platform/7.0/single/configuring-messaging/#configure_destinations_artemis) The JMS Queue must be discoverable via a JNDI name of `java:/queue/TradeBrokerQueue` and the JMS Topic must be discoverable via a JNDI name of `java:/topic/TradeStreamerTopic` (these align with the mappings in `javaee6/modules/web/src/main/webapp/WEB-INF/web.xml`) Example entries for JBoss EAP configuration:
```
<jms-destinations>
    <jms-queue name="TradeBrokerQueue">
        <entry name="queue/TradeBrokerQueue"/>
        <entry name="java:jboss/queue/TradeBrokerQueue"/>
    </jms-queue>
    <jms-topic name="TradeStreamerTopic">
        <entry name="topic/TradeStreamerTopic"/>
        <entry name="java:jboss/topic/TradeStreamerTopic"/>
        <entry name="jms/TradeStreamerTopic"/>
    </jms-topic>
</jms-destinations>
```

Build and deploy the app to JBoss EAP
-------------------------------------
To build DayTrader, you need to have JDK 6 and [Maven 2.0.9](http://maven.apache.org) or later
installed.

Once installed you can build DayTrader by executing the following command
from the dayTrader root directory (the directory containing this README):

        mvn clean install

The result will be an EAR file located in the `javaee6/assemblies/daytrader-ear/target` directory which can
be copied to JBoss EAP's `standalone/deployments` directory.

Then, startup JBoss EAP using the *Full* profile, e.g.

    bin/standalone.sh -c standalone-full.xml

And access the app using `http://<hostname>/daytrader`, for example `http://localhost:8080/daytrader`

Then follow the instructions to _Initialize DayTrader_ as described earlier in this document.

© Copyright 2006,2008 The Apache Software Foundation.
----------------------------------------------------

The code in this directory contains the code for the benchmark sample called
Day Trader.  This sample was contributed by IBM to the Apache Geronimo project
under the ASF license to further functional and performance testing of Geronimo.  
