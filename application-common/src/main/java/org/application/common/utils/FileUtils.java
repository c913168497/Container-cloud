package org.application.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class FileUtils {
	
	private static final BigDecimal DEFAULT = new BigDecimal(1024);


    public static void createFile(String filePath, ClassLoader loader, String templateClassPath) {
        createFile(filePath, loader, templateClassPath, null);
    }


    public static void rm(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) file.delete();
    }

    /**
     * @param filePath
     * @param loader
     * @param templateClassPath
     * @param vars              可以替换参数 #{param}
     */
    public static void createFile(String filePath, ClassLoader loader, String templateClassPath, Map<String, String> vars) {
        OutputStream out = null;
        InputStream ins = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                //创建文件
                ins = loader.getResourceAsStream(templateClassPath);
                BufferedReader br = new BufferedReader(new InputStreamReader(ins, "UTF-8"));
                String lineTxt = null;
                out = new FileOutputStream(file);
                while ((lineTxt = br.readLine()) != null) {
                    lineTxt = lineTxt.concat("\n");
                    //替换参数
                    lineTxt = replaceVars(vars, lineTxt);
                    out.write(lineTxt.getBytes());
                }
                br.close();
                out.flush();
            }
        } catch (Exception e) {
            log.error("创建文件失败");
            e.printStackTrace();
        } finally {
            try {
                if (Optional.ofNullable(ins).isPresent()) {
                    ins.close();
                }
                if (Optional.ofNullable(out).isPresent()) {
                    out.close();
                }

            } catch (Exception e) {

            }
        }
    }


    private static String replaceVars(Map<String, String> vars, String line) {
        if (line.contains("#{")) {
            Pattern pattern = Pattern.compile("#\\{\\w*\\}");
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                String param = matcher.group();
                line = line.replace(param, vars.get(param.replace("#{", "").replace("}", "")));
            }
        }
        return line;
    }

    /**
     * 根据字符串生成文件
     *
     * @param filePath
     * @param content
     */
    public static void createFile(String filePath, String content) {
        FileWriter writer;
        try {
            writer = new FileWriter(filePath);
            writer.write(content);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            log.error("创建文件失败");
        }
    }

    /**
     * 根据文件路径读取文件内容
     *
     * @param fileName
     * @return
     */
    public static String readToString(String fileName) {
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
            return new String(filecontent, "UTF-8");
        } catch (Exception e) {
            log.error("读取文件失败");
            return null;
        }
    }

    /**
     * 查找
     *
     * @param filePath
     * @param indexStr
     * @return
     */
    public static String searchContent(String filePath, String indexStr) {
        String str = "";
        try {
            FileReader m = new FileReader(new File(filePath));
            BufferedReader reader = new BufferedReader(m);
            while (true) {
                String nextline = reader.readLine();
                if (nextline == null) break;
                if (nextline.contains(indexStr)) {
                    str = nextline.substring(indexStr.length());
                    break;
                }
            }
            reader.close();
        } catch (Exception e) {
            log.error("读取文件失败");
        }
        return str;
    }


    /**
     * 替换文件内容
     * @param path 文件路径
     * @param regexContents 需要替换的文件对照关系  k-v  k:正则表达式
     * @param nullAppend 找不到的数据是否新增
     */
    public static void replacTextContent(String path, Map<String,String> regexContents,boolean nullAppend) {
        boolean flag = false;
        try {
            File file = new File(path);
            if(!file.exists()) return ;
            FileReader in = new FileReader(file);
            BufferedReader bufIn = new BufferedReader(in);
            // 内存流, 作为临时流
            CharArrayWriter tempStream = new CharArrayWriter();
            // 替换
            String line = null;
            Set<String> appendContents = new HashSet<>(regexContents.values());
            while ((line = bufIn.readLine()) != null) {
                if (line.trim().length() == 0) continue;
                // 替换每行中, 符合条件的字符串
               /* if (line.contains(srcStr)) {
                    line = line.replaceAll(line, srcStr.concat(replaceStr));
                    flag = true;
                }
                if (line.contains("server_id=")) {
                    line = line.replaceAll(line, "server_id=".concat(String.valueOf(randomNum())));
                }*/

               for(String regex :regexContents.keySet()) {
                   if(line.matches(regex)) {
                       line = regexContents.get(regex) ;
                       appendContents.remove(regexContents.get(regex));
                   }
               }
                // 将该行写入内存
                tempStream.write(line);
                // 添加换行符
                tempStream.append(System.getProperty("line.separator"));
            }

            for(String append : appendContents){
                //将未修改的行数据添加进入
                tempStream.write(append);
                tempStream.append(System.getProperty("line.separator"));
            }
            // 关闭 输入流
            bufIn.close();
            // 将内存中的流 写入 文件
            FileWriter out = new FileWriter(file);
            tempStream.writeTo(out);
            out.close();
            appendContents.clear();
        } catch (Exception e) {
            log.error("替换文件内容失败");
        }
    }

    /**
     * 追加文件内容
     *
     * @param filePath
     * @param content
     */
    public static void appendFile(String filePath, String content) {
        FileWriter fw = null;
        try {
            //如果文件存在，则追加内容；如果文件不存在，则创建文件
            File f = new File(filePath);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }
            fw = new FileWriter(f, true);
            PrintWriter pw = new PrintWriter(fw);
            pw.println(content);
            pw.flush();
            fw.flush();
            pw.close();
            fw.close();
        } catch (IOException e) {
            log.error("追加文件失败");
        }
    }

    public static void copyFile(File source, File dest) {
        if (!dest.exists())
            dest.getParentFile().mkdirs();
        try {
            Files.copy(source.toPath(), dest.toPath());
        } catch (Exception e) {
            log.error("复制文件失败");
        } finally {
            if (source.getPath().contains("/tmp/")) {
                source.delete();
                source.getParentFile().delete();
            }
        }
    }
    
    public static String formatSize(String size) {
        BigDecimal value = new BigDecimal(size);
        //如果小于1024 直接1kb
        if (value.compareTo(DEFAULT) == -1)
            return "1KB";
        value = safeDivide(value, DEFAULT, BigDecimal.ZERO);
        //如果小于1024KB 返回KB
        if (value.compareTo(DEFAULT) == -1)
            return value.toString().concat("KB");
        value = safeDivide(value, DEFAULT, BigDecimal.ZERO);
        //如果小于1024m 返回KB
        if (value.compareTo(DEFAULT) == -1)
            return value.toString().concat("M");
        value = safeDivide(value, DEFAULT, BigDecimal.ZERO);
        //如果小于1024m 返回KB
        if (value.compareTo(DEFAULT) == -1)
            return value.toString().concat("G");
        return "";
    }
    
    public static <T extends Number> BigDecimal safeDivide(T b1, T b2, BigDecimal defaultValue) {
        if (null == b1 || null == b2) {
            return defaultValue;
        }
        try {
            return BigDecimal.valueOf(b1.doubleValue()).divide(BigDecimal.valueOf(b2.doubleValue()), 2, BigDecimal.ROUND_HALF_UP);
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @param dir 将要删除的文件目录
     * @return
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }
    
    /**
     * 文件是否存在
     * @param path
     * @return
     */
    public static boolean fileExists(String path){
		File file = new File(path);
		if(file.exists() && file.isFile()){
			return true;
		}else{
			return false;
		}
	}

}
