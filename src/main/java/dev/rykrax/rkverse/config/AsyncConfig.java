package dev.rykrax.rkverse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AsyncConfig {

    @Bean(name = "uploadCoverExecutor")
    public Executor uploadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);           // Duy trì 4 thread xử lý song song
        executor.setMaxPoolSize(8);            // Mở rộng tối đa 8 thread khi queue đầy
        executor.setQueueCapacity(100);        // Chứa tối đa 100 task chờ
        executor.setThreadNamePrefix("r2-upload-");

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }

    @Bean(name = "chapterUploadExecutor")
    public Executor chapterUploadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);          // Duy trì 2 luồng xử lý đồng thời
        executor.setMaxPoolSize(5);           // Tối đa 5 luồng khi quá tải
        executor.setQueueCapacity(50);        // Chứa tối đa 50 task chờ
        executor.setThreadNamePrefix("r2-upload-");
        executor.initialize();
        return executor;
    }
}