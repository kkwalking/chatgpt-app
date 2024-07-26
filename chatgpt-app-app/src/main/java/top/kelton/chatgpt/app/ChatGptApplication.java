package top.kelton.chatgpt.app;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author zzk
 * @description
 * @created 2024/7/26
 */

@SpringBootApplication
@Configurable
public class ChatGptApplication {


    public static void main(String[] args) {
        SpringApplication.run(ChatGptApplication.class, args);
    }
}
