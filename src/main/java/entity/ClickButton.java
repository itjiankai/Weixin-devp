package entity;

public class ClickButton extends AbstractButton{
	/**
	 * 	  "type":"click",
          "name":"今日歌曲",
          "key":"V1001_TODAY_MUSIC"
	 */
	private String type = "click";// 固定的
	private String key;
	
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	public ClickButton(String name, String key) {
		super(name);
		this.key = key;
	}
	
	
	
	
}
