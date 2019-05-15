/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.boot.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.customizer.SpringApplicationCustomizer;
import org.springframework.boot.customizer.SpringApplicationCustomizers;
import org.springframework.core.OrderComparator;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;

/**
 * @author Dave Syer
 *
 */
public class SpringApplicationCustomizerAdapter {

	private SpringApplication application;

	public SpringApplicationCustomizerAdapter(SpringApplication application) {
		this.application = application;
	}

	public void customize() {
		Class<?>[] primarySources = getField(this.application, "primarySources");
		for (SpringApplicationCustomizer customizer : factories(primarySources,
				SpringApplicationCustomizer.class)) {
			customizer.customize(this.application);
		}
	}

	@SuppressWarnings("unchecked")
	public static Class<?>[] getField(Object target, String name) {
		Field field = ReflectionUtils.findField(target.getClass(), name);
		ReflectionUtils.makeAccessible(field);
		return ((Set<Class<?>>) ReflectionUtils.getField(field, target))
				.toArray(new Class<?>[0]);
	}

	private <T> Collection<? extends T> factories(Class<?>[] sources, Class<T> type) {
		List<T> result = new ArrayList<>();
		Set<String> types = new HashSet<>();
		for (Class<?> source : sources) {
			collect(source, type, types);
		}
		result.addAll(createSpringFactoriesInstances(type, null, types));
		OrderComparator.sort(result);
		return result;
	}

	@SuppressWarnings("unchecked")
	private <T> Collection<? extends T> createSpringFactoriesInstances(Class<T> type,
			ClassLoader classLoader, Set<String> names) {
		List<T> instances = new ArrayList<>(names.size());
		for (String name : names) {
			try {
				Class<?> instanceClass = ClassUtils.forName(name, classLoader);
				Assert.isAssignable(type, instanceClass);
				Constructor<?> constructor = instanceClass
						.getDeclaredConstructor(new Class<?>[0]);
				T instance = (T) BeanUtils.instantiateClass(constructor);
				instances.add(instance);
			}
			catch (Throwable ex) {
				throw new IllegalArgumentException(
						"Cannot instantiate " + type + " : " + name, ex);
			}
		}
		return instances;
	}

	private static void collect(Class<?> key, Class<?> type, Set<String> names) {
		if (AnnotatedElementUtils.hasAnnotation(key,
				SpringApplicationCustomizers.class)) {
			MultiValueMap<String, Object> customizers = AnnotatedElementUtils
					.getAllAnnotationAttributes(key,
							SpringApplicationCustomizers.class.getName(), true, true);
			for (Object obj : customizers.get("value")) {
				for (String value : (String[]) obj) {
					names.add(value);
				}
			}
		}

	}

}
