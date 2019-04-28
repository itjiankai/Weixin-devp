package entity;

import java.util.Map;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("xml")
public class TextMessage extends BaseMessage{
	// 回复的消息内容（换行：在content中能够换行，微信客户端就支持换行显示）
	@XStreamAlias("Content")
	private String content;
	
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	// 构造方法
	public TextMessage(Map<String, String> requestMap,String content) {
		super(requestMap);
		// 设置文本消息的msgType 为  text
		this.setMsgType("text");
		this.content = content;	
	}
	

}
