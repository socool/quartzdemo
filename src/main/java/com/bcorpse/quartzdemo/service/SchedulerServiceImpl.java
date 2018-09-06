package com.bcorpse.quartzdemo.service;

import com.bcorpse.quartzdemo.helper.AutowiringSpringBeanJobFactory;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Properties;

@Configuration
public class SchedulerServiceImpl implements SchedulerService{

    private static final Logger logger = LoggerFactory.getLogger(SchedulerServiceImpl.class);

    private SchedulerFactoryBean scheduler;

    @Autowired
    AutowiringSpringBeanJobFactory autowiringSpringBeanJobFactory;

    @PostConstruct
    void init() {
        scheduler = quartzScheduler();
    }

    @Bean
    public SchedulerFactoryBean quartzScheduler() {

        try {
            SchedulerFactoryBean quartzScheduler = new SchedulerFactoryBean();
            quartzScheduler.setOverwriteExistingJobs(true);
            quartzScheduler.setSchedulerName("wms-quartz-scheduler");
            quartzScheduler.setJobFactory(autowiringSpringBeanJobFactory);
            quartzScheduler.setQuartzProperties(quartzProperties());
            logger.info("Quartz Scheduler initialized");
            return quartzScheduler;
        } catch ( Exception e ) {
            logger.error("Quartz Scheduler can not be initialized, the error is " + e.getMessage());
            return null;
        }
    }

    @PreDestroy
    void destroy() {
        try {
            scheduler.destroy();
        } catch ( Exception e ) {
            logger.error("Quartz Scheduler can not be shutdown, the error is " + e.getMessage(), e);
        }
    }


    @Bean
    public Properties quartzProperties() {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
        Properties properties = null;
        try {
            propertiesFactoryBean.afterPropertiesSet();
            properties = propertiesFactoryBean.getObject();

        } catch (IOException e) {
            logger.error("Quartz Scheduler can not read properties file, the error is " + e.getMessage());
        }

        return properties;
    }

    @Override
    public void register(JobDetail jobDetail, CronTrigger cronTrigger) {

        try {
            /*
             * We have 3 ways to restart the application
             * 1. Reschedule the job to it's original schedule.
             * 2. Resume the job to keep the latest schedule. It will also restart the job that been paused.
             * 3. Do nothing, the jobs will remain the same as they are before the restart.
             *
             * Here we choose option 2. The benefit is in the future we can use the reschedule function to manage the schedule in DB.
             * We can rely on the persistent of the schedule in DB.
             */

            if(scheduler.getScheduler().checkExists(cronTrigger.getKey())){
                // Option one:
                //scheduler.getScheduler().rescheduleJob(cronTrigger.getKey(), cronTrigger);
                //logger.info("Quartz Scheduler reschedule trigger " + cronTrigger.getKey());

                //Option two:
                //scheduler.getScheduler().resumeTrigger(cronTrigger.getKey());
                //logger.info("Quartz Scheduler resume trigger " + cronTrigger.getKey());

                //Option three:
                logger.info("Quartz Scheduler keep the trigger status " + cronTrigger.getKey());

            }else{
                scheduler.getScheduler().scheduleJob(jobDetail, cronTrigger);
                logger.info("Quartz Scheduler register new trigger " +  cronTrigger.getKey());
            }
        } catch (SchedulerException e) {
            logger.error("Quartz Scheduler can not register trigger " +  cronTrigger.getKey() + ". The error is " + e.getMessage(), e);
        }
    }

    @Override
    public void reschedule(CronTrigger cronTrigger) {
        try {
            logger.info("Reschedule trigger " + cronTrigger.getKey());
            logger.info("The new schedule is " +cronTrigger.getCronExpression());

            scheduler.getScheduler().rescheduleJob(cronTrigger.getKey(), cronTrigger);

        } catch (SchedulerException e) {
            logger.error("Quartz Scheduler can not reschedule trigger " +  cronTrigger.getKey() + ". the error is " + e.getMessage(), e);
        }

    }

    @Override
    public void pause(CronTrigger cronTrigger) {
        try{
            logger.info("Pause trigger " + cronTrigger.getKey());
            scheduler.getScheduler().pauseTrigger(cronTrigger.getKey());

        } catch (Exception e) {
            logger.error("Quartz Scheduler can not pause trigger " +  cronTrigger.getKey() + ". the error is " + e.getMessage(), e);
        }

    }

    @Override
    public void resume(CronTrigger cronTrigger) {
        try{
            logger.info("Pause trigger " + cronTrigger.getKey());
            scheduler.getScheduler().resumeTrigger(cronTrigger.getKey());

        } catch (Exception e) {
            logger.error("Quartz Scheduler can not resume trigger " +  cronTrigger.getKey() + ". the error is " + e.getMessage(), e);
        }


    }

}
