package service;

import static org.hamcrest.CoreMatchers.nullValue;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletInputStream;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import org.junit.Test;

import com.baidu.aip.ocr.AipOcr;
import com.thoughtworks.xstream.XStream;

import entity.AccessToken;
import entity.Article;
import entity.BaseMessage;
import entity.NewsMessage;
import entity.TextMessage;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import util.Util;

public class WxService {
	private static final String TOKEN = "djkxgg";
	// 官方指定的获取token的地址 -通过访问这个地址，获取到我们自己公众号的token
	private static final String GET_TOKEN_URL="https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
	//微信公众号	
	private static final String APPID="wx103ffb0f59c66485";
	private static final String APPSECRET="caaee3ff8415040690b9ea9f8ff3afe3";
	// 存放token-token是有过期时间的
	private static AccessToken at;
	
	// 百度AI接口
	private static String APP_ID = "15984915"; 
	private static String API_KEY = "QDqKd6I5dFzV81l0mTgk3Brc";
	private static String SECRET_KEY = "ks5Wij9qXhiwrfWXxw8NhFCrfzVmldmQ";
	
	
	//创建菜单menu - 
	
	/**
	 * 获取token
	 */
	private static void getToken(){
		// 将APPID和APPSECRET替换为我们自己公众号的
		String url = GET_TOKEN_URL.replace("APPID", APPID).replace("APPSECRET", APPSECRET);
		// 得到的是xml格式的字符串
		String tokenStr = Util.get(url);
		JSONObject jsonObject = JSONObject.fromObject(tokenStr);
		String token = jsonObject.getString("access_token");
		String expiresIn = jsonObject.getString("expires_in");
		// System.out.println(tokenStr);
		// 给token 赋值
		at = new AccessToken(token, expiresIn);
	}
	
	/**
	 * 对外暴露获取token
	 * @return
	 */
	public static String getAccessToken(){
		if(at==null||at.isExpired()) {
			getToken();
		}
		return at.getAccessToken();
	}

	
	
	/**
	 * 微信开发绑定公众号
	 * @param timestamp
	 * @param nonce
	 * @param signature
	 * @return
	 */
	public static boolean check(String timestamp, String nonce, String signature) {
		String[] strs = new String[] { TOKEN, timestamp, nonce };
		// 1）将token、timestamp、nonce三个参数进行字典序排序
		Arrays.sort(strs);
		// 对数组拼接成一个字符串
		String str = strs[0] + strs[1] + strs[2];
		// 2）将三个参数字符串拼接成一个字符串进行sha1加密
		String mysig = sha1(str);
		// 3）开发者获得加密后的字符串可与signature对比，标识该请求来源于微信
		return signature.equals(mysig);
	}

	/**
	 * 进行sha1加密
	 * 
	 * @param str
	 * @return
	 */
	private static String sha1(String str) {
		try {
			// 获取一个加密对象
			MessageDigest md = MessageDigest.getInstance("sha1");
			// 加密
			byte[] digest = md.digest(str.getBytes());
			char[] chars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
			// 处理加密结果
			StringBuilder sb = new StringBuilder();
			for (byte b : digest) {
				sb.append(chars[(b >> 4) & 15]);
				sb.append(chars[b & 15]);
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 将请求的xml文件转换为Map对象
	 * 
	 * @param inputStream
	 * @return
	 */
	public static Map<String, String> parseRequest(ServletInputStream inputStream) {

		Map<String, String> map = new HashMap<String, String>();
		SAXReader reader = new SAXReader();
		// 读取输入流，获取文档对象
		Document document;
		try {
			document = reader.read(inputStream);
			// 获取到根节点
			Element root = document.getRootElement();
			List<Element> list = root.elements();
			for (Element element : list) {
				map.put(element.getName(), element.getStringValue());
			}

		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return map;
	}

	/**
	 * 处理所有的事件和消息回复
	 * 
	 * @param requestMap
	 * @return 返回的是xml数据包
	 */

	public static String getResponse(Map<String, String> requestMap) {
		BaseMessage baseMsg = null;
		// 微信号发送给客户端唯一有用的消息就是MsgType
		String msgType = requestMap.get("MsgType");
		switch (msgType) {
		// 文本消息处理
		case "text":
			baseMsg = dealTextMessage(requestMap);
			break;
		// 如果发过来的是图片的话，我们可以选择识别图片中的文字作为回应
		case "image":
			baseMsg = dealImage(requestMap);
			break;
		// 其他消息类型处理
		default:
			break;
		}
		// 最后一步是把 得到的消息对象处理为xml数据包
		if (baseMsg != null) {
			System.out.println("123321123321");
			return beanToXml(baseMsg);
		}
		return null;
	}
	
	/**
	 * 处理图片，识别图片中的内容
	 * @param requestMap
	 * @return
	 */
	public static BaseMessage dealImage(Map<String, String> requestMap) {
		// 初始化一个AipOcr
        AipOcr client = new AipOcr(APP_ID, API_KEY, SECRET_KEY);
        // 获取到公众号上传图片的地址
        String path = requestMap.get("PicUrl");
        // 进行网络图片识别
        org.json.JSONObject res = client.generalUrl(path, new HashMap<String, String>());
        String json = res.toString();
        JSONObject jsonObject = JSONObject.fromObject(json);
        // 读取具体的文本内容
        JSONArray jsonArray = jsonObject.getJSONArray("words_result");
		Iterator<JSONObject> it = jsonArray.iterator();
		StringBuilder sb = new StringBuilder();
		while(it.hasNext()) {
			JSONObject next = it.next();
			sb.append(next.getString("words"));
		}
		return new TextMessage(requestMap, sb.toString());
	}

	// 处理文本消息 
	// 将map格式的对象 转换为 BaseMessage类对象
	private static BaseMessage dealTextMessage(Map<String, String> requestMap) {
		// 获取聊天内容
		String content = requestMap.get("Content");
		
		if (content.equals("图文")) {
			// 有一个专门的公众号被动回复消息的类型 - -图文消息类型
			List<Article> articles = new ArrayList<Article>();
			articles.add(new Article("四顿饭","一天吃四顿饭才是真的饿了", 
					"http://mmbiz.qpic.cn/mmbiz_jpg/Gl6kBCjuUqFZxlK6icMvzEzzK03lImc28FJibXVGPVDeBfpmVZK9JnSvLmiadKDJHRoNniaVibh43Bibs4loua4QEtxw/0",
					"www.baidu.com"));
			NewsMessage newsMessage = new NewsMessage(requestMap, articles);
			return newsMessage;
		}
		if (content.equals("登录")) {
			String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx103ffb0f59c66485&redirect_uri=http://alibaba7.free.idcfengye.com/weixin-devp/GetUserInfo&response_type=code&scope=snsapi_base&state=STATE#wechat_redirect";
			TextMessage tm = new TextMessage(requestMap, "点击<a herf =\""+url+"\">这里</a>登录");
														 // "点击<a href=\""+url+"\">这里</a>登录"
			return tm;
		}
		return null;
	}
	
	/**
	 * 调用图灵机器人聊天
	 * @param msg  发送的内容
	 * @return
	 */
	/*private static String chat(String msg) {
		 String result =null;
	     String url ="http://op.juhe.cn/robot/index";//请求接口地址
	     // 封装请求参数
	     Map params = new HashMap<>();
	     params.put("key",APPKEY);//您申请到的本接口专用的APPKEY
         params.put("info",msg);//要发送给机器人的内容，不要超过30个字符
         params.put("dtype","");//返回的数据的格式，json或xml，默认为json
         params.put("loc","");//地点，如北京中关村
         params.put("lon","");//经度，东经116.234632（小数点后保留6位），需要写为116234632
         params.put("lat","");//纬度，北纬40.234632（小数点后保留6位），需要写为40234632
         params.put("userid","");//1~32位，此userid针对您自己的每一个用户，用于上下文的关联
	     
         try {
        	 result = Util.net(url, params, "GET");// 发送请求
    	     JSONObject jsonObject = JSONObject.fromObject(result);
    	   //取出error_code
             int code = jsonObject.getInt("error_code");
             if(code!=0) {
             		return null;
             }
             //取出返回的消息的内容
             String resp = jsonObject.getJSONObject("result").getString("text");
             return resp;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}*/

	// 将BaseMessage类对象转换为xml文件格式
	private static String beanToXml(BaseMessage msg) {
		XStream stream = new XStream();
		//设置需要处理XStreamAlias("xml")注释的类
		stream.processAnnotations(TextMessage.class);
		stream.processAnnotations(NewsMessage.class);
		// ........
		String xml = stream.toXML(msg);
		return xml;
	}

	
	/**
	 * 获取带参数的二维码的过程包括两步，首先创建二维码ticket，
	 * 然后凭借ticket到指定URL(https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=TICKET)换取二维码。
	 * 生成带参数二维码ticket
	 */
	//生成带参数二维码ticket
	public static String getQrCodeTicket(){
		String at = WxService.getAccessToken();
		String url = " https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token="+at;
		String data="{\"expire_seconds\": 600, \"action_name\": \"QR_STR_SCENE\", \"action_info\": {\"scene\": {\"scene_str\": \"djkxgg\"}}}";
		String result = Util.post(url, data);
		// System.out.println(result);
		String ticket = JSONObject.fromObject(result).getString("ticket");
		return ticket;
	}
	
	
	/**
	 * 获取关注微信公众号的用户的信息
	 * @param user
	 * @return
	 */
	public static String getUserInfo(String user){
		
		String url = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN";
		url = url.replace("ACCESS_TOKEN", WxService.getAccessToken()).replace("OPENID", user);
		String result = Util.get(url);
		return result;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
