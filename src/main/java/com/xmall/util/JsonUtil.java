package com.xmall.util;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * @ClassName JsonUtil
 * @Description: Json序列化和反序列化工具类，此类具有通用性
 * @Author rwxian
 * @Date 2019/8/16 16:46
 * @Version V1.0
 **/
public class JsonUtil {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 序列化
        // 对象的所有字段全部列入
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.ALWAYS);

        // 取消默认转换timestamps形式
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);

        // 忽略空Bean转json的错误
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);

        // 统一所有日期格式
        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT));

        // 反序列化
        // 忽略json字符串中存在，但在java对象中不存在对应属性的情况。防止错误
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * @MethodName: objectToString
     * @Description: Object型转化为String方法，未格式化的
     * @Param: [obj]
     * @Return: java.lang.String
     * @Author: rwxian
     * @Date: 2019/8/16 17:03
     */
    public static <T> String objectToString(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : objectMapper.writeValueAsString(obj);
        } catch (final Exception e) {
            logger.warn("Object型转换为String型发生异常：", e);
            return null;
        }
    }

    /**
     * @MethodName: objectToStringPretty
     * @Description: 返回格式化好的json字符串
     * @Param: [obj]
     * @Return: java.lang.String
     * @Author: rwxian
     * @Date: 2019/8/16 17:05
     */
    public static <T> String objectToStringPretty(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String ? (String) obj :
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (final Exception e) {
            logger.warn("Object型转换为String型发生异常：", e);
            return null;
        }
    }

    /**
     * @MethodName: stringToObject
     * @Description: String转化为Object
     * @Param: [str, clazz]
     * @Return: T
     * @Author: rwxian
     * @Date: 2019/8/16 17:16
     */
    public static <T> T stringToObject(String str, Class<T> clazz) {
        if (StringUtils.isEmpty(str) || clazz == null) {
            return null;
        }
        try {
            return clazz.equals(String.class) ? (T)str : objectMapper.readValue(str, clazz);
        } catch (IOException e) {
            logger.warn("String转换为Object发生异常：", e);
            return null;
        }
    }

    /**
     * @MethodName: stringToObject
     * @Description: String转化为Object,此方法为解决当String反序列化为List<User>这样的类型时，User会变为LinkedHashMap的问题。
     * @Param: [str, typeReference]
     * @Return: T
     * @Author: rwxian
     * @Date: 2019/8/16 17:36
     */
    public static <T> T stringToObject(String str, TypeReference<T> typeReference) {
        if (StringUtils.isEmpty(str) || typeReference == null) {
            return null;
        }
        try {
            return typeReference.getType().equals(String.class) ? (T)str : (T)objectMapper.readValue(str, typeReference);
        } catch (IOException e) {
            logger.warn("String转换为Object发生异常：", e);
            return null;
        }
    }

    /**
     * @MethodName: stringToObject
     * @Description: String转化为Object,此方法为解决当String反序列化为List<User>这样的类型时，User会变为LinkedHashMap的问题，同时参数可变
     * @Param: [str, collectionClass, elementClasses]
     * @Return: T
     * @Author: rwxian
     * @Date: 2019/8/16 17:44
     */
    public static <T> T stringToObject(String str, Class<?> collectionClass, Class<?>... elementClasses) {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);

        try {
            return objectMapper.readValue(str, javaType);
        } catch (IOException e) {
            logger.warn("String转换为Object发生异常：", e);
            return null;
        }
    }
}
