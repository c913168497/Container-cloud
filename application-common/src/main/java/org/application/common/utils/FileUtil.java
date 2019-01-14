package org.application.common.utils;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * @Author : CNC
 * @Description:
 * @Date: Created in 11:00 2017-10-24
 * @Modified By:
 */
@Slf4j
public class FileUtil {

    /**
     * 执行下载动作
     *
     * @param srcFileName 绝对路径，指向服务器上某个已存在的文件
     * @return
     * @throws UnsupportedEncodingException
     */
    public static void downloadFile(String srcFileName, HttpServletRequest request, HttpServletResponse response) {
        String filepath = srcFileName;
        FileInputStream inputFileStream = null;
        OutputStream outputStream = null;
        try {
            File file = new File(filepath);
            if (file.exists()) {
                long point = 0;
                long fileLength = file.length();
                long position_end = fileLength;

                inputFileStream = new FileInputStream(file);
                outputStream = response.getOutputStream();
                response.reset();

                //设置支持断点
                response.setHeader("Accept-Ranges", "bytes");
                //获取断点位置
                String Range = request.getHeader("Range");
                if (Range != null) {
                    response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                    String RangeFromTo = Range.replaceAll("bytes=", "");
                    log.info("threadName: " + Thread.currentThread().getId() + " Range : " + RangeFromTo);

                    String[] Ranges = RangeFromTo.split("-");
                    point = Long.parseLong(Ranges[0]);

                    if (Ranges.length == 2 && !Ranges[1].equals("")) {
                        position_end = Long.parseLong(Ranges[1]);
                    }
                }

                //写明要下载的文件的大小
                response.setHeader("Content-Length", new Long(position_end - point).toString());
                response.setContentLengthLong(position_end - point);
                log.info("threadName: " + Thread.currentThread().getId() + "point : " + point + " Content-Length : " + new Long(position_end - point).toString());

                //设置response的编码方式
                response.setContentType("application/octet-stream");

                //设置断点续传回应头
                String contentRange = new StringBuffer("bytes ")
                        .append(new Long(point).toString())
                        .append("-")
                        .append(new Long(fileLength - 1).toString())
                        .append("/")
                        .append(new Long(fileLength).toString())
                        .toString();

                if (point != 0) {
                    // 断点续传的回应头：告诉改块插入的位置和文件的总大小
                    // 格式：Content-Range: bytes [文件块的开始字节]-[文件块的结束字节 - 1]/[文件的总大小]
                    contentRange = new StringBuffer("bytes ")
                            .append(new Long(point).toString())
                            .append("-")
                            .append(new Long(position_end - 1).toString())//
                            .append("/")
                            .append(new Long(fileLength).toString())
                            .toString();
                    // 移动文件指针位置，断点处
                    inputFileStream.skip(point);
                }
                response.setHeader("Content-Range", contentRange);


                //设置附加文件名(解决中文乱码)
                String downloadname = file.getName();
                response.setHeader("Content-Disposition", "attachment;filename=" + new String(downloadname.getBytes("gbk"), "iso-8859-1"));

                long bytesWritten = 0;
                byte[] bytes = new byte[1024 * 2];
                int byteCount = 0;

                long NeedWriten = position_end - point + 1;

                while (NeedWriten >= bytesWritten && (byteCount = inputFileStream.read(bytes)) != -1) {
                    if (NeedWriten >= bytesWritten) {
                        long tTempWriten = (bytesWritten + byteCount) > NeedWriten ? (NeedWriten - bytesWritten) : byteCount;
                        outputStream.write(bytes, 0, (int) tTempWriten);
                        bytesWritten += tTempWriten;
                    }
                }
            } else {
                log.error("下载文件失败：" + filepath + " and authcode: 文件不存在");
                setHttpResposeCode(response, response.SC_NOT_FOUND, "download file failed ：" + filepath + " : file not exist");
            }

        } catch (FileNotFoundException e) {
            log.error("下载文件失败：" + filepath + " FileNotFoundException happen : " + e + " URL : " + request.getRequestURL().toString());
            setHttpResposeCode(response, response.SC_NOT_FOUND, "download file failed FileNotFoundException happen : " + e + " filepath :" + filepath + " : file not exist");
        } catch (IOException e) {
            e.printStackTrace();
            log.error("下载文件失败：" + filepath + " IOException hapepn : " + e + " URL : " + request.getRequestURL().toString());
            setHttpResposeCode(response, response.SC_INTERNAL_SERVER_ERROR, "download file failed IOException happen : " + e);
        } finally {
            try {
                if (null != inputFileStream) {
                    inputFileStream.close();
                }
                if (null != outputStream) {
                    outputStream.close();
                }
            } catch (IOException e) {
                log.error("下载文件失败：" + filepath + " EofException happen : " + e);
            }
        }
        log.info("下载文件成功：" + filepath);
    }

    /**
     * 设置http  返回码
     *
     * @param code
     * @param msg
     **/
    private static void setHttpResposeCode(HttpServletResponse response, int code, String msg) {
        try {
            response.reset();
            response.setCharacterEncoding("UTF-8");               //设置字符编码格式
            response.setContentType("text/html; charset=UTF-8"); //设置输出编码格式
            response.sendError(code, new String(msg.getBytes(), "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
            log.error("FileDownLoad setHttpResposeCode IOException happen : " + e);
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
     * 递归删除目录下所有文件夹
     *
     * @param dir 将要删除的文件目录
     * @return
     */
    public static void deleteAllDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                File file = new File(dir, children[i]);
                if (file.isDirectory()) {
                    deleteDir(file);
                }
            }
        }
    }

    /**
     * 判断文件夹路径是否存在不存在则创建
     *
     * @param file
     */
    public static void makeDirToFileAndParentFile(File file) {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 获取文件夹下所有文件大小
     *
     * @param file
     * @return
     */
    public  static  long getDirFileCountSize(File file) {

        if (!file.isDirectory()) {
            return 0;
        }
        File[] fileArray = file.listFiles();
        return Arrays.stream(fileArray).map(fileChunk -> fileChunk.length()).reduce((x, y) -> x + y).get();
    }



    public static File fileWriteByUploadFile(File newFile, int countIndex, String saveTmpDirectory, String uuid) {
        FileOutputStream outputStream = null;
        //文件追加写入
        try {
            outputStream = new FileOutputStream(newFile, true);

            byte[] byt = new byte[2 * 1024];
            int len;
            FileInputStream temp = null;
            for (int i = 0; i < countIndex; i++) {
                temp = new FileInputStream(new File(saveTmpDirectory, uuid + "_" + i));
                while ((len = temp.read(byt)) != -1) {
                    outputStream.write(byt, 0, len);
                }
                //关闭流
                temp.close();
            }
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return newFile;
    }
    /**
     * @param headers
     * @param response
     * @param fileType
     * @param fileName
     * @Title: setResponseHeader
     * @Description: 设置头
     */
    private static void setResponseHeader(HttpHeaders headers, HttpServletResponse response, String fileType, String fileName, long fileSize) {
        if (StringUtils.isEmpty(fileType)) {
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName);
            return;
        }
        response.reset();
        fileType = fileType.toLowerCase();
        boolean unRecognitionType = false;












        switch (fileType) {
            case "png":
                headers.setContentType(MediaType.IMAGE_PNG);
                break;
            case "jpg":
                headers.setContentType(MediaType.IMAGE_JPEG);
                break;
            case "jpeg":
                headers.setContentType(MediaType.IMAGE_JPEG);
                break;
            case "gif":
                headers.setContentType(MediaType.IMAGE_GIF);
                break;
            case "pdf":
                headers.setContentType(MediaType.valueOf("application/pdf"));
                break;
            case "txt":
                headers.setContentType(MediaType.TEXT_PLAIN);
                break;
            case "html":
                headers.setContentType(MediaType.TEXT_HTML);
                break;
            case "htm":
                headers.setContentType(MediaType.TEXT_HTML);
                break;
            case "xml":
                headers.setContentType(MediaType.TEXT_XML);
                break;
            case "ico":
                headers.setContentType(MediaType.valueOf("image/x-icon"));
                break;
            case "mp4":
                headers.setContentType(MediaType.valueOf("video/mp4"));
                break;
            case "mpeg":
                headers.setContentType(MediaType.valueOf("video/mpg"));
                break;
            case "mpg":
                headers.setContentType(MediaType.valueOf("video/mpg"));
                break;
            case "avi":
                headers.setContentType(MediaType.valueOf("video/avi"));
                break;
            case "asf":
                headers.setContentType(MediaType.valueOf("video/x-ms-asf"));
                break;
            case "wmv":
                headers.setContentType(MediaType.valueOf("video/x-ms-wmv"));
                break;
            case "svg":
                headers.setContentType(MediaType.valueOf("text/xml"));
                break;
            case "rmvb":
                headers.setContentType(MediaType.valueOf("application/vnd.rn-realmedia-vbr"));
                break;
            case "mp3":
                headers.setContentType(MediaType.valueOf("audio/mp3"));
                break;

            default: {
                String contentType = null;
                //得到文件后台的contentType 暂不开启
                //contentType = ContentTypeUtils.getContentTypeBySuffix(fileType);
                if (StringUtils.isEmpty(contentType)) {
                    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                    unRecognitionType = true;
                } else {
                    headers.setContentType(MediaType.valueOf(contentType));
                }
            }
        }



        if (unRecognitionType) {
            //如果不能识别的类型，则使用下载
            headers.setContentDispositionFormData("attachment", fileName);
        } else {
            //能够识别的类型使用在线打开方式
            response.addHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
        }
        response.setHeader("Content-Length", String.valueOf(fileSize));
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
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
     * Java文件操作 获取文件扩展名
     */
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename.toLowerCase();
    }

    /**
     * Java文件操作 获取不带扩展名的文件名
     */
    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename.toLowerCase();
    }
}
