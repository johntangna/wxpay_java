package onmouse.cn.wxpay.utils;

import com.google.common.collect.Lists;
import me.chanjar.weixin.common.annotation.Required;
import me.chanjar.weixin.common.error.WxErrorException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BeanUtils {
    private static Logger log = LoggerFactory.getLogger(BeanUtils.class);

    /**
     * 检查bean里面标记@Required的field是否为空，为空则抛出异常
     */
    public static void checkRequiredFields(Object bean) throws WxErrorException {
        List<String> requiredFields = Lists.newArrayList();

        List<Field> fields = new ArrayList<>(Arrays.asList(bean.getClass().getDeclaredFields()));
        fields.addAll(Arrays.asList(bean.getClass().getSuperclass().getDeclaredFields()));

        for (Field field:
             fields) {
            try {
                boolean isAccessible = field.isAccessible();
                field.setAccessible(true);
                if (field.isAnnotationPresent(Required.class)) {
                    boolean isRequiredMissing = field.get(bean) == null
                            || (field.get(bean) instanceof String
                            && StringUtils.isBlank(field.get(bean).toString()));
                    if (isRequiredMissing) {
                        requiredFields.add(field.getName());
                    }
                }
                field.setAccessible(isAccessible);
            } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
                log.error(e.getMessage(), e);
            }
        }
        if(!requiredFields.isEmpty()) {
            String msg = String.format("必填字段【%s】必须提供值", requiredFields);
            log.debug(msg);
            throw new WxErrorException(msg);
        }
    }
}
