Migrating DayTrader to Red Hat JBoss EAP
========================================

Below are 3 typical migration tasks for migrating to JBoss EAP from other Java EE app servers such as Weblogic:

Lifecycle listeners
-------------------
WebLogic application lifecycle listener events provide handles on which developers can control behavior during deployment, undeployment, and redeployment. This is a WebLogic proprietary feature. `ApplicationLifecycleEvent` classes can be replaced with CDI `@ApplicationScoped` beans or EJB 3.1 `@Startup` beans.

An example weblogic-application.xml:
```
<listener>
    <listener-class>org.apache.geronimo.daytrader.javaee7.AppListener</listener-class>
</listener>
```

Example AppListener.java:

```
import weblogic.application.ApplicationLifecycleListener;
import weblogic.application.ApplicationLifecycleEvent;

public class MyListener extends ApplicationLifecycleListener {

    public void postStart(ApplicationLifecycleEvent evt) {
        System.out.println("AppListener(postStart)");
    }

    public void preStop(ApplicationLifecycleEvent evt) {
        System.out.println("AppListener(preStop)");
    }
}
```

To fix this, remove the listener from `weblogic-application.xml` and add a CDI bean whose methods have the same content:

```
@ApplicationScoped
public class StartupBean
{
    @PostConstruct
    void startup() {
        System.out.println("AppListener(postStart)");
    }

    @PreDestroy
    void shutdown() {
        System.out.println("AppListener(preStop)");
    }
}
```

Relevant docs:

* [Migrate WebLogic ApplicationLifecycleEvent to standard EJB with JBoss EAP](https://access.redhat.com/articles/1326703)
* [Java EE ServletContextEvent JavaDoc](http://docs.oracle.com/javaee/7/api/javax/servlet/ServletContextEvent.html)
* [WebLogic custom ApplicationLifecycleEvent Documentation](https://docs.oracle.com/cd/E13222_01/wls/docs90/programming/lifecycle.html)

Lazy DB writes for EJB transactions
-----------------------------------
The WebLogic `<delay-updates-until-end-of-tx>` configuration element, which defaults to true, is used for performance reasons to delay updates to the persistent store of all beans until the end of the transaction. When set to false, updates are sent to the database after each method invocation, but are not committed until the end of the transaction. This allows other processes to access the persisted data while the transaction is waiting to be completed. In JBoss EAP 6, the same behavior can be achieved by specifying the `<sync-on-commit-only>` in the `jbosscmp-jdbc.xml` file.

An example: weblogic-ejb-jar.xml:

```
<entity-descriptor>
     <persistence>
     ...
     ...
        <delay-updates-until-end-of-tx>false</delay-updates-until-end-of-tx>
     </persistence>
</entity-descriptor>
```
To fix:

Remove element from `weblogic-ejb-jar.xml` and add to `jbosscmp-jdbc.xml`:

```
<jbosscmp-jdbc>
...
    <entity>
        ...
        <!-- elements removed for readability -->
        <sync-on-commit-only>false</sync-on-commit-only>
    </entity>
</jbosscmp-jdbc>
```

Docs:

* [Migrate the Oracle WebLogic Server weblogic-ejb-jar.xml Deployment Descriptor to Red Hat JBoss Enterprise Application Platform 6 or 7](https://access.redhat.com/articles/1326823)

Portable JNDI namespace
-----------------------
Red Hat JBoss Enterprise Application Platform 6 uses standardized portable EJB JNDI namespaces. Applications created on other platforms, for example WebLogic or Websphere, that contain EJBs that use JNDI must be changed to follow the standardized JNDI namespace convention.

```
Hashtable<String, String> env = new Hashtable<String, String>();
env.put( Context.PROVIDER_URL, "t3://localhost:7001" );
env.put( Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory" );
env.put( Context.SECURITY_PRINCIPAL, "weblogic" );
env.put( Context.SECURITY_CREDENTIALS, "weblogic" );
Context context = new InitialContext( env );
Service service = (Service)context.lookup( "sample.Service#" + Service.class.getName() );
```
Becomes
```
Context context = new InitialContext();
Service service = (Service) context.lookup( "java:app/service/" + ServiceImpl.class.getSimpleName() );
```

Docs:

* [Migrate Applications From Other Platforms to Use Portable JNDI Syntax in Red Hat JBoss Enterprise Application Platform](https://access.redhat.com/articles/1496973)
