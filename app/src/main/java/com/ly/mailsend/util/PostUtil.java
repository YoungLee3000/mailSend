package com.ly.mailsend.util;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.http.HttpResponseCache;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ly.mailsend.Address;


import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;

public class PostUtil
{

    /**
     * 发送请求
     * @param url
     * @param params
     * @param encode
     * @return
     */
    public static String sendPost(String url, Map<String,String> params, String encode)
    {
    String data = getRequestData(params, encode).toString();//获得请求体
    //System.out.print(data);
    PrintWriter out = null;
    BufferedReader in = null;
    String result = "";
    try
    {

        URL realUrl = new URL(url);
        // 打开和URL之间的连接
        URLConnection conn = realUrl.openConnection();
        // 设置通用的请求属性
        conn.setRequestProperty("accept", "*/*");
        conn.setRequestProperty("connection", "Keep-Alive");
        conn.setRequestProperty("user-agent",
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
        // 发送POST请求必须设置如下两行
        conn.setDoOutput(true);
        conn.setDoInput(true);
        // 获取URLConnection对象对应的输出流
        out = new PrintWriter(conn.getOutputStream());
        // 发送请求参数
        out.print(data);  // 向服务端输出参数
        // flush输出流的缓冲
        out.flush();
        // 定义BufferedReader输入流来读取URL的响应
        in = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = in.readLine()) != null)
        {
            result += "\n" + line;
        }
    }
    catch (Exception e)
    {
        System.out.println("发送POST请求出现异常！" + e);
        e.printStackTrace();
    }
    // 使用finally块来关闭输出流、输入流
    finally
    {
        try
        {
            if (out != null)
            {
                out.close();
            }
            if (in != null)
            {
                in.close();
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    return result;
    }


    /**
     * 获得请求体
     * @param params
     * @param encode
     * @return
     */
    private static StringBuffer getRequestData(Map<String, String> params, String encode)
    {
      StringBuffer stringBuffer = new StringBuffer();        //存储封装好的请求体信息
      try {
          for(Map.Entry<String, String> entry : params.entrySet()) {
              stringBuffer.append(entry.getKey())
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), encode))
                    .append("&");
          }
          stringBuffer.deleteCharAt(stringBuffer.length() - 1);    //删除最后的一个"&"
      } catch (Exception e) {

        e.printStackTrace();
      }
      return stringBuffer;
    }


    /**
     * 解析json数据
     * @param result
     * @return
     */
    public static List<Address> parseJson(String result)
    {

        JSONArray jsonArray = JSON.parseArray(result);
        //JSONArray jsonArray1 = JSONArray.parseArray(JSON_ARRAY_STR);//因为JSONArray继承了JSON，所以这样也是可以的


        //遍历方式2
        List<Address> resultList = new ArrayList<>();
        try {
            for (Object obj : jsonArray) {
                JSONObject jsonObject = (JSONObject) obj;
                Address address = new Address();
                address.setName(jsonObject.getString("name"));
                address.setPhoneNumber(jsonObject.getString("phoneNumber"));
                address.setProvince(jsonObject.getString("province"));
                address.setCity(jsonObject.getString("city"));
                address.setCounty(jsonObject.getString("county"));
                address.setStreet(jsonObject.getString("street"));
                address.setDetail(jsonObject.getString("detail"));
                resultList.add(address);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }



        return resultList;
    }





    /**
     * 下载网络文件
     * @param urlStr  请求的文件链接
     * @param cachePath 保存的路径
     * @return 返回文件的位置path
     */
    public static String getDownloadFile2Cache(String urlStr,String cachePath)
    {

        OutputStream output=null;
        String [] strArray = cachePath.split("/");
        String fileName = strArray[strArray.length - 1];

        String pathName = "";

        try {
            /*
             * 通过URL取得HttpURLConnection
             * 要网络连接成功，需在AndroidMainfest.xml中进行权限配置
             * <uses-permission android:name="android.permission.INTERNET" />
             */
            URL url=new URL(urlStr);
            HttpURLConnection conn=(HttpURLConnection)url.openConnection();
            //取得inputStream，并将流中的信息写入SDCard

            /*
             * 写前准备
             * 1.在AndroidMainfest.xml中进行权限配置
             * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
             * 取得写入SDCard的权限
             * 2.取得SDCard的路径： Environment.getExternalStorageDirectory()
             * 3.检查要保存的文件上是否已经存在
             * 4.不存在，新建文件夹，新建文件
             * 5.将input流中的信息写入SDCard
             * 6.关闭流
             */
            String SDCard= Environment.getExternalStorageDirectory()+"";
            pathName=SDCard+"/"+cachePath+"/"+fileName;//文件存储路径

            File file=new File(pathName);
            InputStream input=conn.getInputStream();
            if(file.exists()){
                Log.d(Constants.TAG,"exits");
            }else{
                String dir=SDCard+"/"+cachePath;
                new File(dir).mkdir();//新建文件夹
                file.createNewFile();//新建文件
                output=new FileOutputStream(file);
                //读取大文件
                byte[] buffer=new byte[4*1024];
                while(input.read(buffer)!=-1){
                    output.write(buffer);
                }
                output.flush();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            closeStream(output);
        }

        return pathName;
    }


    /**
     * 往服务器上上传文本  比如log日志
     * @param urlstr        请求的url
     * @param uploadFile    log日志的路径
     * @param newName    log日志的名字 LOG.log
     * @return
     */
    public static void httpPostFile(Activity activity, String urlstr, String uploadFile, String newName) {
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";//边界标识
        int TIME_OUT = 10*1000;   //超时时间
        HttpURLConnection con = null;
        DataOutputStream ds = null;
        InputStream is = null;
        try {
            URL url = new URL(urlstr);
            con = (HttpURLConnection) url.openConnection();
            con.setReadTimeout(TIME_OUT);
            con.setConnectTimeout(TIME_OUT);
            /* 允许Input、Output，不使用Cache */
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);

            // 设置http连接属性
            con.setRequestMethod("POST");//请求方式
            con.setRequestProperty("Connection", "Keep-Alive");//在一次TCP连接中可以持续发送多份数据而不会断开连接
            con.setRequestProperty("Charset", "UTF-8");//设置编码
            con.setRequestProperty("Content-Type",//multipart/form-data能上传文件的编码格式
                    "multipart/form-data;boundary=" + boundary);

            ds = new DataOutputStream(con.getOutputStream());
            ds.writeBytes(twoHyphens + boundary + end);
            ds.writeBytes("Content-Disposition: form-data; "
                    + "name=\"stblog\";filename=\"" + newName + "\"" + end);
            ds.writeBytes(end);

            // 取得文件的FileInputStream
            FileInputStream fStream = new FileInputStream(uploadFile);
            /* 设置每次写入1024bytes */
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int length = -1;
            /* 从文件读取数据至缓冲区 */
            while ((length = fStream.read(buffer)) != -1) {
                /* 将资料写入DataOutputStream中 */
                ds.write(buffer, 0, length);
            }
            ds.writeBytes(end);
            ds.writeBytes(twoHyphens + boundary + twoHyphens + end);//结束

            fStream.close();
            ds.flush();
            /* 取得Response内容 */
            is = con.getInputStream();
            int ch;
            StringBuffer b = new StringBuffer();
            while ((ch = is.read()) != -1) {
                b.append((char) ch);
            }
            /* 将Response显示于Dialog */
            showDialog(activity,true,uploadFile,"上传成功" + b.toString().trim());
        } catch (Exception e) {
            showDialog(activity,false,uploadFile,"上传失败" + e);
        }finally {
            /* 关闭DataOutputStream */
            closeStream(ds);
            closeStream(is);
            if (con != null) {
                con.disconnect();
            }
        }
    }


    /* 显示Dialog的method */
    private static void showDialog(final Activity activity,final Boolean isSuccess,final String uploadFile,final String mess) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(activity).setTitle("Message")
                        .setMessage(mess)
                        .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                File file = new File(uploadFile);
                                if(file.exists()&&isSuccess){//日志文件存在且上传日志成功
                                    file.delete();
                                    Toast.makeText(activity, "log日志已删除", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).show();
            }
        });

    }




    /**
     * 关闭流
     * @param closeable
     */
    private static void closeStream(Closeable closeable){
        if (closeable != null){
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}


