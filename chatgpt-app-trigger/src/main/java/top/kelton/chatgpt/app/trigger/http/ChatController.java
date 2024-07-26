package top.kelton.chatgpt.app.trigger.http;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import top.kelton.chatgpt.app.trigger.http.dto.ChatGPTRequestDTO;
import top.kelton.chatgpt.app.trigger.http.dto.MessageEntity;
import top.kelton.chatgpt.domain.ChatCompletionRequest;
import top.kelton.chatgpt.domain.ChatCompletionResponse;
import top.kelton.chatgpt.session.OpenAiSession;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;


/**
 * @author zzk
 * @description
 * @created 2024/7/26
 */
@Slf4j
@RestController
@RequestMapping("/api/${app.config.api-version}/chatgpt")
public class ChatController {

    @Resource
    private OpenAiSession openAiSession;



    @RequestMapping(value = "/chat/completions", method = RequestMethod.POST)
    public ResponseBodyEmitter completionsStream(@RequestBody ChatGPTRequestDTO request, HttpServletResponse response) {

        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");

        log.info("流式问答请求开始，使用模型：{} 请求信息：{}", request.getModel(), JSON.toJSONString(request.getMessages()));

        ResponseBodyEmitter emitter = new ResponseBodyEmitter(3 * 60 * 1000L);
        // 请求应答完成后的回调
        emitter.onCompletion(() -> {
            log.info("流式问答请求完成，使用模型：{}", request.getModel());
        });
        // 请求应答失败后的回调
        emitter.onError(throwable -> log.error("流式问答请求异常，使用模型：{}", request.getModel(), throwable));

        ArrayList<ChatCompletionRequest.Message> messages = new ArrayList<>();
        for (MessageEntity message : request.getMessages()) {
            messages.add(ChatCompletionRequest.Message.builder().role(message.getRole()).content(message.getContent()).build());
        }
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder().stream(true).model("gpt-4o").messages(messages).build();
        EventSourceListener sourceListener = new EventSourceListener() {

            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                log.info("chatgpt返回结果 {}", data);
                ChatCompletionResponse chatCompletionResponse = JSON.parseObject(data, ChatCompletionResponse.class);
                List<ChatCompletionResponse.Choice> choices = chatCompletionResponse.getChoices();
                for (ChatCompletionResponse.Choice chatChoice : choices) {
                    ChatCompletionResponse.Delta delta = chatChoice.getDelta();
                    // 应答完成
                    String finishReason = chatChoice.getFinishReason();
                    if (StringUtils.isNoneBlank(finishReason) && "stop".equals(finishReason)) {
                        // 后续还有关于usage 耗费额度的消息，暂时忽略
                        emitter.complete();
                        break;
                    }

                    // 发送信息
                    try {
                        emitter.send("data: "+delta.getContent());
                    } catch (Exception e) {
                        throw new RuntimeException(e.getMessage());
                    }
                }

            }
        };
        openAiSession.completions(chatCompletionRequest, sourceListener);
        return emitter;

    }

}
