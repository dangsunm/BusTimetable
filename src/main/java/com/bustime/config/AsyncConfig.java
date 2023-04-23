package com.bustime.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int processors = Runtime.getRuntime().availableProcessors(); // 프로세스 개수
        log.info("processors count {}", processors);

        //기본값 설정
        executor.setCorePoolSize(processors); //active thread
        executor.setMaxPoolSize(processors * 2); //최대 thread size, 꽉찼는데 더 수요가 있으면 maxpoolsize까지 스래드 만들어 채워 넣음.
        executor.setQueueCapacity(50); // 액티브 쓰레드가 꽉차면 50개까지 줄을 세움, 꽉차면 수행 불가
        executor.setKeepAliveSeconds(60); //맥스 개수를 넘기면 태스크를 처리하지 못한다.
        executor.setThreadNamePrefix("AsyncExecutor-");
        executor.initialize(); //초기화

        return executor;
    }
}
