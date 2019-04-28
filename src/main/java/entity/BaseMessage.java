package entity;

import java.util.Map;

// 响应发送者的消息封装 注意要将此时此景的 ToUserName换成 FromUserName
public class BaseMessage {
	
	/**
	 * 	ToUserName	是	接收方帐号（收到的OpenID）-requestMap中的fromUserName
		FromUserName	是	开发者微信号
		CreateTime	是	消息创建时间 （整型）
		MsgType	是	消息类型，文本为text
		Content	是	回复的消息内容（换行：在content中能够换行，微信客户端就支持换行显示）
	 */
	private String ToUserName;
	private String FromUserName;
	private String MsgType;
	private String CreateTime;
	
	
	
	
	// 构造方法，供子类调用
	public BaseMessage(Map<String,String> requestMap) {
		/**
		 * 注意 这里的 toUserName 要换成 消息来自于哪个username 否则消息发送失败
		 * 
		 */
		this.ToUserName = requestMap.get("FromUserName");
		this.FromUserName = requestMap.get("ToUserName");
		this.CreateTime = System.currentTimeMillis()/1000+"";
	}
	
	
	public String getToUserName() {
		return ToUserName;
	}
	public void setToUserName(String toUserName) {
		ToUserName = toUserName;
	}
	public String getFromUserName() {
		return FromUserName;
	}
	public void setFromUserName(String fromUserName) {
		FromUserName = fromUserName;
	}
	public String getMsgType() {
		return MsgType;
	}
	public void setMsgType(String msgType) {
		MsgType = msgType;
	}
	public String getCreateTime() {
		return CreateTime;
	}
	public void setCreateTime(String createTime) {
		CreateTime = createTime;
	}
	
	
	
	
	
}
