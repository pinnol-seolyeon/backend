package jpabasic.pinnolbe.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;


/**
 * 동시에 여러 스케줄러가 돌 때 블로킹되지 않도록 thread pool 지정
 */
@Configuration
@EnableScheduling
public class SchedulerConfig implements SchedulingConfigurer {


    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler(); //커스텀 쓰레드 풀에서 실행됨
        scheduler.setPoolSize(3); //최대 동시 실행 개수
        scheduler.setThreadNamePrefix("scheduler-"); //생성되는 스레드 이름 앞부분 지정
        scheduler.initialize(); //스케줄러를 초기화(스레드 풀 생성)
        taskRegistrar.setTaskScheduler(scheduler); //스케줄러 등록
    }
}
