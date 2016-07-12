package com.example.shanj.speedtest.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by shanj on 2016/6/23.
 */
public class RootUtils {
    private static boolean mHaveRoot = false;

    public static boolean haveRoot(){
        if (!mHaveRoot){
            int ret = execRootCmdSilent("echo test");//通过执行测试命令来检测
            if (ret != -1){//-1无法获取root权限
                mHaveRoot = true;
            }
        }
        return mHaveRoot;
    }


    public static int execRootCmdSilent(String cmd) {
        int result = -1;
        DataOutputStream dos = null;
        Process p = null;

        try {
            p = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(p.getOutputStream());

            dos.writeBytes(cmd + "\n");
            //            dos.flush();
            dos.writeBytes("exit\n");

            dos.flush();
            p.waitFor();
            result = p.exitValue();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                    dos = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (p != null) {
                p.destroy();
                p = null;
            }
        }
        return result;
    }

    // 执行命令并且输出结果
    public static String execRootCmd(String cmd) {
        String result = "";
        DataOutputStream dos = null;
        DataInputStream dis = null;
        Process p = null;
        try {
            p = Runtime.getRuntime().exec("su");// 经过Root处理的android系统即有su命令
            dos = new DataOutputStream(p.getOutputStream());
            dis = new DataInputStream(p.getInputStream());
            dos.writeBytes(cmd + "\n");
            //            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            String line = null;
            while ((line = dis.readLine()) != null) {
                // Log.d("result", line);
                if (result == "") {
                    result += line;
                } else {
                    result += "\n" + line;
                }
            }
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                    dos = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dis != null) {
                try {
                    dis.close();
                    dis = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (p != null) {
                p.destroy();
                p = null;
            }
        }
        return result;
    }
}
