package com.bcorpse.quartzdemo.service;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;

public interface SchedulerService {

    void register(JobDetail jobDetail, CronTrigger cronTrigger);

    void reschedule(CronTrigger cronTrigger);

    void pause(CronTrigger cronTrigger);

    void resume(CronTrigger cronTrigger);


}
