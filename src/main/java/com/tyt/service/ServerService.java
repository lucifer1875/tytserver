package com.tyt.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lucifer on 2017/12/14.
 */
public class ServerService implements Runnable{

    protected static Logger logger4J = Logger.getLogger("ServerService");
    private static HashMap<String, String> map = new HashMap<>();
    private static Map<String, String> dic = new HashMap<>();
    private Socket socket;
    private List<Socket> sockets;
    private InputStream inputStream;
    private OutputStream outputStream;
    private boolean isConnect = false;

    public ServerService(Socket socket, List<Socket> sockets) {
        this.sockets = sockets;
        this.socket = socket;
        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            isConnect = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(this).start();
    }

    static{
        //开关灯：54 59 54 00 02 01 00 2d（3c）
        //开关窗帘：54 59 54 00 02 01 05 2d（3c）

        //头部指令
        map.put("head", "545954000201");

        //设备编号
        map.put("light", "00");
        map.put("curtain", "05");

        //操作指令
        map.put("turnOn", "2D");
        map.put("turnOff", "3C");


        //初始化设备字典
        dic.put("light", "灯");
        dic.put("curtain", "窗帘");
    }



    public void run() {
        logger4J.info("server thread is running !");
        while (isConnect){
            try {
                String result = readProcess(socket);
                logger4J.info("get info is ：" + result);
                if (null == result){
                    return ;
                }

                if (-1 != socket.getInetAddress().toString().indexOf("39.106.166.24")){
                    sendProcess(result, socket);
                }
                for (int i = 0; i < sockets.size(); i++) {
                    sendProcess(result, sockets.get(i));
                }

                break;
            } catch (Exception e) {
                e.printStackTrace();
            }finally {

            }
        }
    }


    /**
     * 读取信息
     */
    public String readProcess(Socket socket){
        try {
            String info = "";
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer buffer = new StringBuffer();
            while(null != (info = br.readLine())){
                buffer.append(info);
            }

            return buffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 发送指令
     */
    public void sendProcess(String s, Socket socket){

        if (null == s || "".equals(s)){
            return ;
        }

        PrintWriter printWriter = null;

        try {
            String resInfo = "好的，已经通知小泰管家";
            if (-1 == socket.getInetAddress().toString().indexOf("39.106.166.24")){
                JsonParser jsonParser = new JsonParser();
                JsonObject orderObj = (JsonObject) jsonParser.parse(s);

                String device = orderObj.get("device").getAsString();
                String order = orderObj.get("order").getAsString();
                String d = "";
                for (String k : dic.keySet()){
                    if (-1 != device.indexOf(dic.get(k))){
                        d = k;
                        break;
                    }
                }

                StringBuffer orderbuffer = new StringBuffer();
                String o = orderbuffer.append(map.get("head")).append(map.get(d)).append(map.get(order)).toString();

                logger4J.info("writing order is : " + o);
                socket.getOutputStream().write(hex2byte(o));
            }else {
                printWriter = new PrintWriter(socket.getOutputStream());
                printWriter.write(resInfo);
                printWriter.flush();

            }

            //如果是web服务，返回消息后关闭socket连接
            if (-1 != socket.getInetAddress().toString().indexOf("39.106.166.24")){
                socket.shutdownOutput();
                socket.close();
                printWriter.close();
            }
            logger4J.info("writing is over!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] hex2byte(String hex) {
        String digital = "0123456789ABCDEF";
        String hex1 = hex.replace(" ", "");
        char[] hex2char = hex1.toCharArray();
        byte[] bytes = new byte[hex1.length() / 2];
        byte temp;
        for (int p = 0; p < bytes.length; p++) {
            temp = (byte) (digital.indexOf(hex2char[2 * p]) * 16);
            temp += digital.indexOf(hex2char[2 * p + 1]);
            bytes[p] = (byte) (temp & 0xff);
        }
        return bytes;
    }

    public static void main(String[] args) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("1", "value1");
        map.put("2", "value2");
        map.put("3", "value3");

        //第一种：普遍使用，二次取值
        System.out.println("通过Map.keySet遍历key和value：");
        String k = "";
        String v = "value2";
        for (String key : map.keySet()) {
            System.out.println("key= "+ key + " and value= " + map.get(key));
            if (v.equals(map.get(key))){
                k = key;
            }
        }
        System.out.println("k is : " + k);
    }
}
