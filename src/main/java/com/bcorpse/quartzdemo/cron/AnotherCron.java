package com.bcorpse.quartzdemo.cron;

import com.bcorpse.quartzdemo.service.AnotherService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

import javax.annotation.PostConstruct;

@Configuration
public class AnotherCron extends AbsMyCronImpl {

    private static final Logger logger = LoggerFactory.getLogger(AnotherCron.class);

    @PostConstruct
    void init() {
        cronTriggerFactoryBean = anotherCronTriggerFactoryBean();
        jobDetailFactoryBean = anotherJobDetailFactory();
        register();

    }

    @Bean
    public JobDetailFactoryBean anotherJobDetailFactory() {
        JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();

        jobDetailFactory.setJobClass(AnotherJob.class);
        jobDetailFactory.setName("AnotherCronJob");
        jobDetailFactory.setGroup("JinJob");
        return jobDetailFactory;

    }

    @Bean
    public CronTriggerFactoryBean anotherCronTriggerFactoryBean() {
        CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();

        cronTriggerFactoryBean.setJobDetail(anotherJobDetailFactory().getObject());
        cronTriggerFactoryBean.setCronExpression("0 0/2 * * * ?");
        cronTriggerFactoryBean.setName("AnotherCronTrigger");
        cronTriggerFactoryBean.setGroup("TestJob");
        return cronTriggerFactoryBean;
    }

    public static class AnotherJob implements Job {

        @Autowired
        public AnotherService anotherService;

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            try{
                logger.info("Running the Job " );
                anotherService.anotherTest();
            }catch(Exception e){
                logger.error("Error when running the Job, the error is " + e.getMessage(), e);
            }
        }
    }

}
