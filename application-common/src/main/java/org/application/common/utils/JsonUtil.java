package org.application.common.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
public class JsonUtil
{
    
    private static ObjectMapper objectMapper;
    
    static
    {
        objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    /**
     * 将对象转换为json字符串
     */
    public static <T> String obj2string(T t)
    {
        try
        {
            return objectMapper.writeValueAsString(t);
        }
        catch (IOException e)
        {
            throw Throwables.propagate(e);
        }
    }
    
    /**
     * 将字符串转list对象
     */
    public static <T> List<T> str2list(String jsonStr, Class<T> cls)
    {
        try
        {
            JavaType t = objectMapper.getTypeFactory().constructParametricType(List.class, cls);
            return objectMapper.readValue(jsonStr, t);
        }
        catch (Exception e)
        {
            throw Throwables.propagate(e);
        }
    }
    
    public static <T> T str2obj(String jsonStr, TypeReference typeReference)
    {
        try
        {
            return objectMapper.readValue(jsonStr, typeReference);
        }
        catch (IOException e)
        {
            throw Throwables.propagate(e);
        }
    }
    
    /**
     * 将字符串转为对象
     */
    public static <T> T str2obj(String jsonStr, Class<T> cls)
    {
        try
        {
            return objectMapper.readValue(jsonStr, cls);
        }
        catch (Exception e)
        {
            throw Throwables.propagate(e);
        }
    }
    
    /**
     * 将字符串转为json节点
     */
    public static JsonNode str2node(String jsonStr)
    {
        try
        {
            return objectMapper.readTree(jsonStr);
        }
        catch (Exception e)
        {
            throw Throwables.propagate(e);
        }
    }
    
    public static <T> T readAs(byte[] bytes, TypeReference<T> typeReference)
    {
        try
        {
            return objectMapper.readValue(bytes, typeReference);
        }
        catch (IOException e)
        {
            throw Throwables.propagate(e);
        }
    }
    
}
