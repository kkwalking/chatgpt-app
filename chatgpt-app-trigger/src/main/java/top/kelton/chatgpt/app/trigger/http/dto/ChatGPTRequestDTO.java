package top.kelton.chatgpt.app.trigger.http.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.kelton.chatgpt.app.types.enums.ChatGPTModel;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatGPTRequestDTO {

    /** 默认模型 */
    private String model = ChatGPTModel.GPT_3_5_TURBO.getCode();

    /** 问题描述 */
    private List<MessageEntity> messages;

}