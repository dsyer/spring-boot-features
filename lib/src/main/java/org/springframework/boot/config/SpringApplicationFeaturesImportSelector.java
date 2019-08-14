/*
 * Copyright 2018 the original author or authors.
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

package org.springframework.boot.config;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author Dave Syer
 *
 */
class SpringApplicationFeaturesImportSelector
		implements ImportSelector, BeanClassLoaderAware, BeanFactoryAware {

	private static Log logger = LogFactory
			.getLog(SpringApplicationFeaturesImportSelector.class);

	private static final Set<String> ANNOTATION_NAMES;
	private ClassLoader classLoader;

	private ConfigurableListableBeanFactory beanFactory;

	static {
		Set<String> names = new LinkedHashSet<>();
		names.add(SpringBootFeaturesApplication.class.getName());
		names.add(SpringApplicationFeatures.class.getName());
		ANNOTATION_NAMES = Collections.unmodifiableSet(names);
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public String[] selectImports(AnnotationMetadata metadata) {
		return getCandidateConfigurations(metadata).toArray(new String[0]);
	}

	protected List<String> getAutoConfigurations() {
		List<String> configurations = SpringFactoriesLoader
				.loadFactoryNames(EnableAutoConfiguration.class, classLoader);
		Assert.notEmpty(configurations,
				"No auto configuration classes found in META-INF/spring.factories. If you "
						+ "are using a custom packaging, make sure that file is correct.");
		return configurations;
	}

	protected List<String> getCandidateConfigurations(AnnotationMetadata metadata) {
		Set<String> allautos = new HashSet<>(getAutoConfigurations());
		List<String> candidates = new ArrayList<>();
		Map<Class<?>, List<Annotation>> annotations = getAnnotations(metadata);
		annotations.forEach((source, sourceAnnotations) -> collectCandidateConfigurations(
				source, sourceAnnotations, candidates));
		List<String> result = new ArrayList<>();
		for (String candidate : candidates) {
			if (!allautos.contains(candidate)) {
				result.add(candidate);
			}
		}
		logger.info("Regular configs (" + metadata.getClassName() + "): " + result);
		SpringApplicationFeaturesDeferredImportSelector selector = getSelector();
		selector.register(metadata.getClassName(), result);
		return result;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		Assert.isInstanceOf(ConfigurableListableBeanFactory.class, beanFactory);
		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
	}

	protected final ConfigurableListableBeanFactory getBeanFactory() {
		return this.beanFactory;
	}

	private SpringApplicationFeaturesDeferredImportSelector getSelector() {
		String beanName = SpringApplicationFeaturesDeferredImportSelector.class.getName();
		SpringApplicationFeaturesDeferredImportSelector selector = (SpringApplicationFeaturesDeferredImportSelector) this.beanFactory
				.getSingleton(beanName);
		if (selector == null) {
			selector = new SpringApplicationFeaturesDeferredImportSelector();
			this.beanFactory.registerSingleton(beanName, selector);
		}
		return selector;
	}

	protected final Map<Class<?>, List<Annotation>> getAnnotations(
			AnnotationMetadata metadata) {
		MultiValueMap<Class<?>, Annotation> annotations = new LinkedMultiValueMap<>();
		Class<?> source = ClassUtils.resolveClassName(metadata.getClassName(), null);
		collectAnnotations(source, annotations, new HashSet<>());
		return Collections.unmodifiableMap(annotations);
	}

	private void collectCandidateConfigurations(Class<?> source,
			List<Annotation> annotations, List<String> candidates) {
		for (Annotation annotation : annotations) {
			candidates.addAll(getConfigurationsForAnnotation(source, annotation));
		}
	}

	private Collection<String> getConfigurationsForAnnotation(Class<?> source,
			Annotation annotation) {
		String[] classes = (String[]) AnnotationUtils
				.getAnnotationAttributes(annotation, true).get("classes");
		return classes != null ? Arrays.asList(classes) : Collections.emptyList();
	}

	private void collectAnnotations(Class<?> source,
			MultiValueMap<Class<?>, Annotation> annotations, HashSet<Class<?>> seen) {
		if (source != null && seen.add(source)) {
			for (Annotation annotation : source.getDeclaredAnnotations()) {
				if (!AnnotationUtils.isInJavaLangAnnotationPackage(annotation)) {
					if (ANNOTATION_NAMES
							.contains(annotation.annotationType().getName())) {
						annotations.add(source, annotation);
					}
					collectAnnotations(annotation.annotationType(), annotations, seen);
				}
			}
			collectAnnotations(source.getSuperclass(), annotations, seen);
		}
	}

}
