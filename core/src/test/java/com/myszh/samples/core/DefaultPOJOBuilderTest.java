package com.myszh.samples.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.myszh.samples.core.pojo.User;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ResourceUtils;

/**
 * @author LuoQuan
 * @since 2022/6/19
 */
class DefaultPOJOBuilderTest {

    private static final Properties properties = new Properties();

    static {
        try {
            File file = ResourceUtils.getFile("classpath:application.properties");
            try (FileInputStream inputStream = new FileInputStream(file)) {
                properties.load(inputStream);
            } catch (Exception e) {
                ReflectionUtils.rethrowRuntimeException(e);
            }
        } catch (Exception e) {
            ReflectionUtils.rethrowRuntimeException(e);
        }
    }

    private final DefaultPOJOBuilder pojoBuilder = new DefaultPOJOBuilder(properties);


    /**
     * 必须构建成功User对象
     */
    @Test
    void should_build_user() {
        MultiContext context = MultiContext.of()
            .add("suffix", "_sz")
            .add("end", () -> ".001");

        User user = pojoBuilder.build(User.class, context, "msg.order.delivery");

        System.out.println(user);
        assertNotNull(user);
    }
}

