package util;

import static org.hamcrest.CoreMatchers.nullValue;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import entity.NewsMessage;
import net.sf.json.JSONObject;
import service.WxService;

public class Util {

	/**
	 * 向指定url发送post请求，并携带data数据
	 * 
	 * @param url
	 * @param data
	 * @return
	 */
	public static String post(String url, String data) {
		try {
			URL urlObj = new URL(url);
			// 获取连接
			URLConnection connection = urlObj.openConnection();
			// 设置为可发送数据状态
			connection.setDoOutput(true);
			connection.setDoInput(true);
			// 获取输出流
			OutputStream os = connection.getOutputStream();
			// 往外写data
			os.write(data.getBytes());
			os.close();

			// 一次读取数组b个字节的输入流
			byte[] b = new byte[1024];
			// len是当读取的输入流不足b数组大小时，放到len当中
			int len;
			StringBuilder sb = new StringBuilder();
			// 获取输入流
			InputStream is = connection.getInputStream();
			while ((len = is.read(b)) != -1) {
				sb.append(new String(b, 0, len));
			}
			return sb.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 向指定地址发送get请求
	 * 
	 * @param url
	 * @return
	 */
	public static String get(String url) {

		try {
			URL urlObj = new URL(url);
			// 开连接
			URLConnection connection = urlObj.openConnection();
			// 获取调用这个url得到的输入流
			InputStream in = connection.getInputStream();
			byte[] b = new byte[1024];
			int len;
			StringBuilder sb = new StringBuilder();
			while ((len = in.read(b)) != -1) {
				sb.append(new String(b, 0, len));
			}
			return sb.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * 上床本地图片到网页上
	 * @throws IOException 
	 */
	public static void getGifTo(HttpServletResponse response) throws IOException{
		File file = new File("D://1.gif");
		// FileInputStream  inputStream = new FileInputStream(file);
		// OutputStream os =response.getOutputStream();
//		byte[] b = new byte[2048];
//		int len;
//		StringBuilder sb = new StringBuilder();
//		while ((len = inputStream.read(b)) != -1) {
//			sb.append(new String(b, 0,len));
//			// os.write(b, 0, len);
//			os.write(sb);
//		}
		/*int size = inputStream.available();
		
		//b数组用于存放图片字节数据  
		byte[] b = new byte[size];
		inputStream.read(b);
		// 关闭输入流
		inputStream.close();
		response.setContentType("gif/*");
		OutputStream os = response.getOutputStream();
		os.write(b);
		// 关闭响应输出流
		os.close();*/
		ServletOutputStream out = null;
        FileInputStream in = new FileInputStream(file);
        out = response.getOutputStream();
        byte[] bytes = new byte[1024 * 10];
        int len = 0;
        while ((len = in.read(bytes)) != -1) {
            out.write(bytes,0,len);
        }
        out.flush();	
		
	}
	
	
	/**
	 * 获取配置文件中的属性值
	 * @throws Exception
	 */
	public Map<String , String> getFileValue(){
	    Properties properties = new Properties();
		   
        try {
            // 1.加载conf.properties配置文件
            properties.load(Util.class.getClassLoader()
                    .getResourceAsStream("content.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String picPath = properties.getProperty("picPath");
        String gifPath = properties.getProperty("gifPath");
		
        Map<String, String> map = new HashMap<String, String>();
        map.put("picPath", picPath);
        map.put("gifPath", gifPath);
        return map;
        
	}
	
	
	public  void getGif() throws Exception {
        String filePath = getFileValue().get("picPath");
        File[] files = getFiles(filePath);
        List<File> fileList = new ArrayList<File>();
        // 二维码中间的个性化图片标识
        File logoFile = new File("D://wd.png");
        for (int i = 0; i < files.length; i++) {
        	System.out.println("i=" + i);
        	System.out.println(files[i]);
            String content = QrCodeUtils.decodeQrCode(files[i]);
            if (StringUtil.isNotEmpty(content)) {
            	fileList.add(QrCodeUtils.createQrCode2(content, logoFile));
            }
        }
        File[] files2 = new File[fileList.size()];
        fileList.toArray(files2);
		String newwPic = getFileValue().get("gifPath");
		int playTime = 1000;
		jpgToGif(files2,newwPic,playTime);
		System.out.println("ok---------");
	}

	/**  
	 * 把多张jpg图片合成一张  
	 * @param pic String[] 多个jpg文件名 包含路径  
	 * @param newPic String 生成的gif文件名 包含路径  
	 * @param playTime int 播放的延迟时间   
	 */  
	private synchronized static void jpgToGif(File pic[], String newPic, int playTime) {  
		try {  
			AnimatedGifEncoder e = new AnimatedGifEncoder(); 
			e.setRepeat(0);  
			e.start(newPic);  
//			e.sizeSet = true;
//			e.setSize(w, h)
			BufferedImage src[] = new BufferedImage[pic.length];  
			for (int i = 0; i < src.length; i++) {  
				e.setDelay(playTime); //设置播放的延迟时间  
				src[i] = ImageIO.read(pic[i]); // 读入需要播放的jpg文件  
				e.addFrame(src[i]);  //添加到帧中  
			}  
			e.finish();  
		} catch (Exception e) {  
			System.out.println( "jpgToGif Failed:");  
			e.printStackTrace();  
		}  
	}  

    /**
     * 读取某个文件夹下的所有文件
     */
    public static List<File> readfile(String filepath) throws FileNotFoundException, IOException {
            try {
            	 File file = new File(filepath);
            	 String[] filelist = file.list();
            	 List<File> result = new ArrayList<File>();
            	 for (int i = 0; i < filelist.length; i++) {
            		  File readfile = new File(filepath + "\\" + filelist[i]);
            		  result.add(readfile);
            	 }
            	return result;
    }catch (Exception e) {
      System.out.println("readfile()   Exception:" + e.getMessage());
      return null;
}
}
    
    public static File[] getFiles(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        return files;
    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
