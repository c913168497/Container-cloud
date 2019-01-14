package org.application.deploy.utils;

import org.apache.commons.lang3.StringUtils;
import org.application.common.utils.IDUtil;

import java.util.Optional;


/**
 * @author Sen
 * @effect 参数转换 及 参数校验
 */
public class ParameterUtils {

    public static Integer parameterStringToInteger(String value){
       return Optional.ofNullable( value ).map(v -> {
            return StringUtils.isNotEmpty(v.trim()) ?  Integer.parseInt( v ) : null;
        }).orElse(null);
    }

    public static boolean notNullAndBlank(String value){
        return Optional.ofNullable(value).map(m -> StringUtils.isNotEmpty(value.trim())).orElse(false);
    }

    public static String getVolumeName(){
        String mainName = String.valueOf( IDUtil.getRandomId() );
        return "v" + mainName.substring(mainName.length() -4 , mainName.length()); //主机卷名称
    }

    /**
     * 如果路径是文件，则只给予只读权限，如果是文件夹则给予读写
     * @param path
     * @return
     */
    public static boolean setFileReadOnly(String path){
        path = path.substring(path.lastIndexOf("/")+1);
        //如报包含了 . 则认为他是文件，而不是文件夹，则只给它只读权限
        return path.contains(".");
    }

}
