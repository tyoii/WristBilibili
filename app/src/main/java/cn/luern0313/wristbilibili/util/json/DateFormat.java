package cn.luern0313.wristbilibili.util.json;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.luern0313.lson.annotation.LsonDefinedAnnotation;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 时间戳 -> 友好日期
 */
@LsonDefinedAnnotation(config = DateFormat.DateFormatConfig.class,
        acceptableDeserializationType = LsonDefinedAnnotation.AcceptableType.NUMBER,
        acceptableSerializationType = LsonDefinedAnnotation.AcceptableType.STRING)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DateFormat
{
    class DateFormatConfig implements LsonDefinedAnnotation.LsonDefinedAnnotationConfig
    {
        @Override
        public Object deserialization(Object value, Annotation annotation, Object object)
        {
            long sec = Double.valueOf(value.toString()).longValue();
            return new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(new Date(sec * 1000L));
        }

        @Override
        public Object serialization(Object value, Annotation annotation, Object object)
        {
            return value;
        }
    }
}
