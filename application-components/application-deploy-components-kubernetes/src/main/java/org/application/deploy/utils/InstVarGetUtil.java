package org.application.deploy.utils;

import org.apache.commons.lang3.StringUtils;
import org.application.common.common.VarsType;
import org.application.common.entity.InstVar;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Sen
 * @effect 参数过滤
 */
public class InstVarGetUtil {
    /**
     * 环境变量
     * @param InstVars
     * @return
     */
   public static  List<InstVar> getVarsType1(List<InstVar> InstVars){
         return  Optional.ofNullable(InstVars).map(envs ->
                    envs.stream().filter(env ->
                            VarsType.ENV_VAR.getValue().equals(env.getVarsType()) && StringUtils.isNotEmpty(env.getVarsName()) )
                            .collect(Collectors.toList()) )
                    .orElse(new ArrayList<>());
    }

    /**
     * 映射端口
     * @param InstVars
     * @return
     */
    public static  List<InstVar> getVarsType2(List<InstVar> InstVars){
        return  Optional.ofNullable(InstVars).map(envs ->
                envs.stream().filter(env ->
                        VarsType.ENV_PORT.getValue().equals(env.getVarsType()) && StringUtils.isNotEmpty(env.getVarsName()) )
                        .collect(Collectors.toList()) )
                .orElse(new ArrayList<>());
    }

    /**
     * 卷挂载
     * @param InstVars
     * @return
     */
    public static List<InstVar> getVarType4(List<InstVar> InstVars){
        return  Optional.ofNullable(InstVars).map(envs ->
                envs.stream().filter(env ->
                        VarsType.ENV_VOLUME.getValue().equals(env.getVarsType()) && ParameterUtils.notNullAndBlank( env.getVarsName() ) &&  ParameterUtils.notNullAndBlank( env.getVarsValue() ) )
                        .collect(Collectors.toList()) )
                .orElse(new ArrayList<>());
    }

    /**
     * 日志
     * @param InstVars
     * @return
     */
    public static List<InstVar> getVarType5(List<InstVar> InstVars){
        return  Optional.ofNullable(InstVars).map(envs ->
                envs.stream().filter(env ->
                        VarsType.ENV_LOG.getValue().equals(env.getVarsType()) &&  ParameterUtils.notNullAndBlank( env.getVarsValue() ) )
                        .collect(Collectors.toList()) )
                .orElse(new ArrayList<>());
    }

    /**
     * 资源控制
     * @param InstVars
     * @return
     */
    public static List<InstVar> getVarType6(List<InstVar> InstVars){
        return Optional.ofNullable( InstVars ).map(envs ->
                    envs.stream().filter(env ->
                            VarsType.RESOURCE_LIMIT.getValue().equals(env.getVarsType())  && ParameterUtils.notNullAndBlank( env.getVarsName() ) &&  ParameterUtils.notNullAndBlank( env.getVarsValue() ) )
                            .collect(Collectors.toList()) )
                .orElse(new ArrayList<>());
    }
}
