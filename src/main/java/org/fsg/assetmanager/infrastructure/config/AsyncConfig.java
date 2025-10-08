package org.fsg.assetmanager.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync(proxyTargetClass = true)
@org.springframework.context.annotation.EnableAspectJAutoProxy(exposeProxy = true)
public class AsyncConfig {
    // Configures a TaxExecutor using Virtual Threads.
    @Bean
    public TaskExecutor taskExecutor() {
        return new VirtualThreadTaskExecutor("virtual-thread-exec-");
    }
}
