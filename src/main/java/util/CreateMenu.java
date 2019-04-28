package util;

import entity.Button;
import entity.ClickButton;
import entity.PhotoOrAlbumButton;
import entity.SubButton;
import entity.ViewButton;
import net.sf.json.JSONObject;
import service.WxService;

/**
 * 微信公众号 创建菜单menu
 * 
 * @author djk
 *
 */
public class CreateMenu {

	public static void main(String[] args) {
		// 菜单对象
		Button btn = new Button();

		btn.getButton().add(new ClickButton("健康生活", "1"));

		SubButton sb = new SubButton("我的医生");
		sb.getSub_button().add(new PhotoOrAlbumButton("传图", "31"));
		sb.getSub_button().add(new ClickButton("找医生", "32"));
		sb.getSub_button().add(new ViewButton("我的医生", "http://wx.ddzybj.com/doctor-pc/index.html#/login"));
		btn.getButton().add(sb);
		// 第三个一级菜单
		btn.getButton().add(new ViewButton("关于我们", "http://www.baidu.com"));
		// 转为json
		JSONObject jsonObject = JSONObject.fromObject(btn);

		String url = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN";
		url = url.replace("ACCESS_TOKEN", WxService.getAccessToken());
		// 开始发送post请求
		String result = Util.post(url, jsonObject.toString());
		System.out.println(result);

	}

}
