package com.myszh.samples.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author LuoQuan
 * @since 2022/6/12
 */
class StringTemplateResolverTest {
    private final StringTemplateResolver templateResolver = StringTemplateResolver.getInstance();

    @Test
    void should_parse_map_non_strict() {
        Map<String, String> context = new HashMap<>();
        context.put("age", "18");
        context.put("name", "zhang san");

        String value = templateResolver.parse("my name is ${name}, age ${age}, addr is ${addr}",
            context, false);
        assertEquals("my name is zhang san, age 18, addr is ${addr}", value);
    }

    @Test
    void should_parse_map_strict() {
        Assertions.assertThrows(
            Exception.class,
            () -> {
                Map<String, String> context = new HashMap<>();
                context.put("age", "18");
                templateResolver.parse("my name is ${name}", context, true);
            });
    }

    @Test
    void should_parse_bean_non_strict() {
        Person person = new Person("zhang san", 18, null, "北京");

        String value = templateResolver.parse("my name is ${name}, age ${age}, addr is ${addr}",
            person, false);
        assertEquals("my name is zhang san, age 18, addr is ${addr}", value);
    }

    @Test
    void should_parse_bean_strict() {
        Assertions.assertThrows(
            Exception.class,
            () -> {
                Person person = new Person(null, 18, null, "shang hai");
                templateResolver.parse("my name is ${name}", person, true);
            });
    }


    @Test
    void should_parse_nested() {
        Person person = new Person("zhang san", 18, "sz", "${addr}");

        String value = templateResolver.parse("my name is ${name}, age ${age}, addr is ${addr2}",
            person, false);
        assertEquals("my name is zhang san, age 18, addr is sz", value);
    }

    @Test
    void should_parse_default_value() {

        String value = templateResolver.parse("my name is ${name:zhang san}", null, true);
        assertEquals("my name is zhang san", value);
    }

    @Test
    void should_parse_expression() {
        String value = templateResolver.parse("10+23=#{10+23}", null);
        assertEquals("10+23=33", value);
    }

    @Test
    void should_parse_expression_of_map() {
        Map<String, String> context = new HashMap<>();
        context.put("age", "18");
        context.put("name", "zhang san");

        String value = templateResolver.parse("my name is #{name}, age #{age}", context);
        assertEquals("my name is zhang san, age 18", value);
    }

    @Test
    void should_parse_expression_of_bean() {
        Person person = new Person("zhang san", 18, "sz", "${addr}");

        Map<String, Object> context = new HashMap<>();
        context.put("name", "zhang san");
        context.put("person", person);

        String value = templateResolver.parse("my name is #{name}, age #{person.age}", context);
        assertEquals("my name is zhang san, age 18", value);
    }

    @Test
    void should_parse_expression_of_multi_context() {
        Person person = new Person("zhang san", 18, "sz", "${addr}");

        Map<String, Object> map = new HashMap<>();
        map.put("name", "zhang san");
        map.put("person", person);

        String value = templateResolver.parse(
            "my name is #{x1.name}, age #{x1.person.age}, addr is #{x3.addr}",
            MultiContext.of()
                .add("x3", person)
                .add("x1", () -> map)
                .add("x2", () -> {
                    // 这种方式可以起到懒加载的效果,真正使用到当前元素才会去加载
                    System.out.println("获取了当前元素x2");
                    return person;
                }));
        assertEquals("my name is zhang san, age 18, addr is sz", value);
    }


    @Data
    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    @Accessors(chain = true)
    private static class Person {
        private String name;
        private int age;
        private String addr;
        private String addr2;
    }
}