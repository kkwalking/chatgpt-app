package top.kelton.chatgpt.app.config;


import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.kelton.chatgpt.session.Factory;
import top.kelton.chatgpt.session.OpenAiSession;
import top.kelton.chatgpt.session.defaults.DefaultFactory;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description ChatGPTSDKConfig 工厂配置开启
 * @create 2023-07-16 08:07
 */
@Configuration
@EnableConfigurationProperties(ChatGPTSDKConfigProperties.class)
public class ChatGPTSDKConfig {

    @Bean(name = "chatGPTOpenAiSession")
    @ConditionalOnProperty(value = "chatgpt.sdk.config.enabled", havingValue = "true", matchIfMissing = false)
    public OpenAiSession openAiSession(ChatGPTSDKConfigProperties properties) {
        // 1. 配置文件
        top.kelton.chatgpt.session.Configuration configuration = new top.kelton.chatgpt.session.Configuration();
        configuration.setApiHost(properties.getApiHost());
        configuration.setApiKey(properties.getApiKey());

        // 2. 会话工厂
        Factory factory = new DefaultFactory(configuration);

        // 3. 开启会话
        return factory.openSession();
    }

}
