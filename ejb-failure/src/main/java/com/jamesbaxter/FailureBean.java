package com.jamesbaxter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Startup
@Singleton
public class FailureBean {

    private static final Logger log = LoggerFactory.getLogger(FailureBean.class);

    @PostConstruct
    public void postConstruct() {
        log.info("Getting constructed");
        if (true)
            throw new RuntimeException("Failing post construct!");
    }

//    @Schedule(hour = "*", minute = "*", second = "0/10", persistent = false)
//    public void schedule() {
//        log.info("Schedule executed");
//    }
}
