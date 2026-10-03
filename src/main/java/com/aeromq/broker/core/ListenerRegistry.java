package com.aeromq.broker.core;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ListenerRegistry implements BeanPostProcessor {

    private final Map<String, List<ConsumerWrapper>> registry = new ConcurrentHashMap<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Method[] methods = bean.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(AeroListener.class)) {
                AeroListener annotation = method.getAnnotation(AeroListener.class);

                registry.computeIfAbsent(annotation.topic(), k -> new CopyOnWriteArrayList<>())
                        .add(new ConsumerWrapper(bean, method));
            }
        }
        return bean;
    }

    public List<ConsumerWrapper> getConsumers(String topic) {
        return registry.getOrDefault(topic, Collections.emptyList());
    }

    public record ConsumerWrapper(Object bean, Method method) {}
}
