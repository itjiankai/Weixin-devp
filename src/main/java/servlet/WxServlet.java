package servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Map;

import javax.jws.WebService;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import service.WxService;

@WebServlet("/wx")
public class WxServlet extends HttpServlet {

	
	public WxServlet() {
		super();
	}

	/**
	 * get方法在此时担任的角色是 验证公众号接入等操作
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//signature	微信加密签名，signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。
		String signature = request.getParameter("signature");
		//timestamp	时间戳
		String timestamp = request.getParameter("timestamp");
		//nonce	随机数
		String nonce = request.getParameter("nonce");
		//echostr	随机字符串
		String echostr = request.getParameter("echostr");
		
		// 校验验证请求，将微信服务器与自己的地址成功接入起来
		if (WxService.check(timestamp,nonce,signature)) {
			// 若确认此次GET请求来自微信服务器，
			// 请原样返回 echostr 参数内容，则接入生效，成为开发者成功，否则接入失败
			PrintWriter printWriter =  response.getWriter();
			printWriter.print(echostr);
			printWriter.flush();
			printWriter.close();
		} else {
			System.out.println("接入微信公众号失败");
		}
	}
	
	/**
	 * 接收消息和事件推送
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// 设置编码格式
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		
		// 解析微信发送的xml格式    将xml文件转换为Map对象     微信发送到本地服务器的xml数据我们需要转换为我们方便操作的 map对象格式
		Map<String, String> requestMap = WxService.parseRequest(request.getInputStream());
		System.out.println(requestMap);
		// 笨重方式给发送者返回消息内容-respXML
		// String respXML = "<xml><ToUserName><![CDATA["+requestMap.get("FromUserName")+"]]></ToUserName><FromUserName><![CDATA["+requestMap.get("ToUserName")+"]]></FromUserName><CreateTime>"+System.currentTimeMillis()/1000+"</CreateTime><MsgType><![CDATA[text]]></MsgType><Content><![CDATA[丁见恺]]></Content></xml>";
		// ---返回消息内容封装结束
		/**
		 * 为什么要将xml的数据转换为map结构，然后再将map结构的数据转换为xml格式呢？
		 * 因为我们从公众号的角度看，回复消息的时候，ToUserName正是 map中的FromUserName 我们需要调换ToUserName和FromUserName 并且 自由的设置返回的聊天内容
		 */
		// 准备回复的xml数据包-respXML
		String respXML = WxService.getResponse(requestMap);
		System.out.println(respXML);
		PrintWriter writer = response.getWriter();
		writer.print(respXML);
		writer.flush();
		writer.close();
	}


}
