# ear-partial-deployment-failure

When copying the ear file to the standalone/deployments folder in Wildfly 17, the logs will indicate that the deployment has partially failed:

```
2020-09-28 15:34:03,450 INFO  [com.jamesbaxter.SuccessfulBean] (ServerService Thread Pool -- 78) Getting constructed
2020-09-28 15:34:03,487 ERROR [org.jboss.as.controller.management-operation] (Controller Boot Thread) WFLYCTL0013: Operation ("deploy") failed - address: ([("deployment" => "my_ear.ear")]) - failure description: {"WFLYCTL0080: Failed services" => {"jboss.deployment.subunit.\"my_ear.ear\".\"ejb-failure-1.0-SNAPSHOT.jar\".component.FailureBean.START" => "java.lang.IllegalStateException: WFLYEE0042: Failed to construct component instance
    Caused by: java.lang.IllegalStateException: WFLYEE0042: Failed to construct component instance
    Caused by: javax.ejb.EJBException: java.lang.RuntimeException: Failing post construct!
    Caused by: java.lang.RuntimeException: Failing post construct!"}}
2020-09-28 15:34:03,567 INFO  [org.jboss.as.server] (ServerService Thread Pool -- 44) WFLYSRV0010: Deployed "my_ear.ear" (runtime-name : "my_ear.ear")
2020-09-28 15:34:03,569 INFO  [org.jboss.as.controller] (Controller Boot Thread) WFLYCTL0183: Service status report
WFLYCTL0186:   Services which failed to start:      service jboss.deployment.subunit."my_ear.ear"."ejb-failure-1.0-SNAPSHOT.jar".component.FailureBean.START: java.lang.IllegalStateException: WFLYEE0042: Failed to construct component instance
WFLYCTL0448: 2 additional services are down due to their dependencies being missing or failed
2020-09-28 15:34:03,685 INFO  [org.jboss.as.server] (Controller Boot Thread) WFLYSRV0212: Resuming server
2020-09-28 15:34:03,687 INFO  [org.jboss.as] (Controller Boot Thread) WFLYSRV0060: Http management interface listening on http://127.0.0.1:9990/management
2020-09-28 15:34:03,688 INFO  [org.jboss.as] (Controller Boot Thread) WFLYSRV0051: Admin console listening on http://127.0.0.1:9990
2020-09-28 15:34:03,688 ERROR [org.jboss.as] (Controller Boot Thread) WFLYSRV0026: WildFly Full 17.0.1.Final (WildFly Core 9.0.2.Final) started (with errors) in 6899ms - Started 544 of 776 services (6 services failed or missing dependencies, 378 services are lazy, passive or on-demand)
```

And you can see the other EJB archive is still running by the scheduled code:

```
2020-09-28 15:34:10,015 INFO  [com.jamesbaxter.SuccessfulBean] (EJB default - 1) Schedule executed
2020-09-28 15:34:20,002 INFO  [com.jamesbaxter.SuccessfulBean] (EJB default - 2) Schedule executed
2020-09-28 15:34:30,002 INFO  [com.jamesbaxter.SuccessfulBean] (EJB default - 3) Schedule executed
```

You can change the behaviour so the whole ear file is undeployed if a single module fails at startup. Just go into the standalone.xml, locate the deployment-scanner subsystem and change the property `runtime-failure-causes-rollback` to true.

The CLI has a similar mechanism when using the `deploy` command. You can set a header like so: `--headers={rollback-on-runtime-failure=true}`, although the default is actually true so you get the atomic behaviour without changing anything.

If you use domain mode you may run some CLI commands in a batch to pre-configure a profile. Your script would start by embedding a host controller using embed-host-controller. In this case the `deploy` command completes instantly, and there is no possibility of 'runtime failure'.

Herein lays the problem - when the domain controller is started, and the applicable nodes join, the atomic behaviour is gone. Partial deployments are allowed, and there appears to be no way to change this behaviour.
