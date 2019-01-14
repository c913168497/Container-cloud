package org.application.common.utils;


import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import lombok.extern.slf4j.Slf4j;
import org.application.common.listener.ShellEventListener;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * shell命令执行工具类
 */
@Slf4j
public class ShellUtils {


    /**
     * 根据git地址获取git分支信息
     *
     * @return
     */
    public static boolean run(String[] commands,ShellEventListener<?,?> listener) {
        Connection conn =  null;
        BufferedReader bufrIn = null;
        BufferedReader bufrError = null;
        Session ssh = null;
        try {
            conn = getConnection();
            ssh = conn.openSession();

            for(String cmd: commands){
                ssh.execCommand(cmd);
            }

            bufrIn = new BufferedReader(new InputStreamReader(ssh.getStdout(),"utf-8"));
            bufrError = new BufferedReader(new InputStreamReader(ssh.getStdout(),"utf-8"));

            String line = null;
            while ((line = bufrIn.readLine()) != null) {
                log.info(line);
                if(Optional.ofNullable(listener).isPresent()) listener.output(line);
            }

            while ((line = bufrError.readLine()) != null) {
                log.error(line);
                if(Optional.ofNullable(listener).isPresent())  listener.onError(line);
            }

            return true ;
        } catch (IOException e) {
            log.error("远程执行sh命令异常,{}", e.getMessage());
            listener.onError(e.getMessage());
            return false;
        } catch (Exception e) {
            listener.onError(e.getMessage());
            log.error("远程执行sh命令异常,{}", e.getMessage());
            return false;
        } finally {
            //连接的Session和Connection对象都需要关闭
            if (ssh != null)
                ssh.close();
            if (conn != null)
                conn.close();
        }
    }



    /**
     * 根据git地址获取git分支信息
     *
     * @return
     */
    public static boolean run(String  command,ShellEventListener<?,?> listener) {
         return run(new String[]{command},listener) ;
    }



    /**
     * 执行shell命令
     * @param command 命令
     */
    public static boolean  run(String command ,String[] params ,ShellEventListener<?,?> listener) {
        BufferedReader bufrIn = null;
        BufferedReader bufrError = null;
        Process process  = null ;
        StringBuffer commandBuffer = new StringBuffer(command);
        try {
            Runtime rt = Runtime.getRuntime();
            Optional.ofNullable(params).ifPresent( ps -> {
                for (String p : ps) {
                	commandBuffer.append(" ").append(p) ;
                }
            });
            log.info("开始执行命令:{}", commandBuffer.toString());
            process  = rt.exec(commandBuffer.toString());
            
            
            bufrIn = new BufferedReader(new InputStreamReader(process.getInputStream(),"utf-8"));
            bufrError = new BufferedReader(new InputStreamReader(process.getErrorStream(),"utf-8"));

            String line = null;
            while ((line = bufrIn.readLine()) != null) {
                log.info(line);
                if(Optional.ofNullable(listener).isPresent()) listener.output(line);
            }

            while ((line = bufrError.readLine()) != null) {
                //log.error(line);
            	log.info("bufrError.readLine()：" + line);
                if(Optional.ofNullable(listener).isPresent())  listener.onError(line);
                return false ;
            }
            process.waitFor();  //等待子进程完成再往下执行。
            //接收执行完毕的返回值
            return  process.exitValue() == 0 ? true : false;
        } catch (Exception e) {
            log.error("执行命令失败:{}",e.getMessage());
            if(Optional.ofNullable(listener).isPresent())  listener.onError(e.getMessage());
            return false ;
        }finally {
            closeStream(bufrIn);
            closeStream(bufrError);
            Optional.ofNullable(process).ifPresent(a -> a.destroy());  //销毁子进程
        }
    }


    /**
     * 执行shell命令
     * @param command 命令
     */
    public static boolean syncRun(String command ,String[] params ,ShellEventListener<?,?> listener) {
        BufferedReader bufrIn = null;
        BufferedReader bufrError = null;
        Process process  = null ;
        try {

            List<String> cmds = new ArrayList<String>();
            cmds.add(command);
            Runtime rt = Runtime.getRuntime();
            Optional.ofNullable(params).ifPresent( ps -> {
                for (String p : ps) {
                    cmds.add(p);
                }
            });
            log.info("开始执行命令:{}", cmds);
            process  = rt.exec(cmds.toArray(new String[cmds.size()]));

            bufrIn = new BufferedReader(new InputStreamReader(process.getInputStream(),"utf-8"));
            bufrError = new BufferedReader(new InputStreamReader(process.getErrorStream(),"utf-8"));

            String line = null;
            while ((line = bufrIn.readLine()) != null) {
                log.info(line);
                if(Optional.ofNullable(listener).isPresent()) listener.output(line);
            }

            while ((line = bufrError.readLine()) != null) {
                log.error(line);
                if(Optional.ofNullable(listener).isPresent())  listener.onError(line);
                return false ;
            }
            process.waitFor();  //等待子进程完成再往下执行。
            //接收执行完毕的返回值
            return  process.exitValue() == 0 ? true : false;
        } catch (Exception e) {
            log.error("执行命令失败:{}",e.getMessage());
            if(Optional.ofNullable(listener).isPresent())  listener.onError(e.getMessage());
            return false ;
        }finally {
            closeStream(bufrIn);
            closeStream(bufrError);
            process.destroy();  //销毁子进程
        }
    }

    /**
     * 获取连接
     *
     * @return
     */
    public static Connection getConnection() throws Exception {
        Connection conn = new Connection("127.0.0.1",22);
        try {
            //连接到主机
            conn.connect();
            //使用用户名和密码校验
            boolean isconn = conn.authenticateWithPublicKey("root", new File("/root/.ssh/id_rsa"),null);
            if (!isconn)
                throw new Exception("获取链接失败");
            return conn;
        } catch (IOException e) {
            log.info("获取连接失败,{}", e.getMessage());
            throw new Exception("获取链接失败");
        }
    }


    private static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

}
