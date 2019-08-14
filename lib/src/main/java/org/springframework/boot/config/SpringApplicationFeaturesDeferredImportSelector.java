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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.autoconfigure.AutoConfigurationImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author Dave Syer
 *
 */
class SpringApplicationFeaturesDeferredImportSelector
		extends AutoConfigurationImportSelector {

	private static Log logger = LogFactory
			.getLog(SpringApplicationFeaturesDeferredImportSelector.class);

	private static final Set<String> ANNOTATION_NAMES;
	
	private final Map<String, List<String>> configs = new HashMap<>();

	private final Map<String, String> roots = new HashMap<>();

	static {
		Set<String> names = new LinkedHashSet<>();
		names.add(SpringApplicationFeatures.class.getName());
		names.add(SpringBootFeaturesApplication.class.getName());
		ANNOTATION_NAMES = Collections.unmodifiableSet(names);
	}

	@Override
	protected Class<?> getAnnotationClass() {
		return SpringApplicationFeatures.class;
	}

	@Override
	protected Set<String> getExclusions(AnnotationMetadata metadata,
			AnnotationAttributes attributes) {
		return Collections.emptySet();
	}

	@Override
	protected List<String> getCandidateConfigurations(AnnotationMetadata metadata,
			AnnotationAttributes attributes) {
		Set<String> allautos = new HashSet<>(
				super.getCandidateConfigurations(metadata, attributes));
		List<String> candidates = new ArrayList<>();
		Map<Class<?>, List<Annotation>> annotations = getAnnotations(metadata);
		annotations.forEach((source, sourceAnnotations) -> collectCandidateConfigurations(
				source, sourceAnnotations, candidates));
		List<String> autos = new ArrayList<>();
		for (String candidate : allautos) {
			if (candidates.contains(candidate)) {
				autos.add(candidate);
			}
		}
		logger.info("Autoconfigs (" + metadata.getClassName() + "): " + autos);
		return autos;
	}

	private SpringApplicationFeaturesDeferredImportSelector getSelector() {
		String beanName = SpringApplicationFeaturesDeferredImportSelector.class.getName();
		SpringApplicationFeaturesDeferredImportSelector selector = (SpringApplicationFeaturesDeferredImportSelector) getBeanFactory()
				.getSingleton(beanName);
		if (selector == null) {
			selector = this;
			getBeanFactory().registerSingleton(beanName, selector);
		}
		return selector;
	}

	protected final Map<Class<?>, List<Annotation>> getAnnotations(
			AnnotationMetadata metadata) {
		MultiValueMap<Class<?>, Annotation> annotations = new LinkedMultiValueMap<>();
		Class<?> source = ClassUtils.resolveClassName(metadata.getClassName(), null);
		SpringApplicationFeaturesDeferredImportSelector selector = getSelector();
		String target =source.getName();
		if (!selector.configs.containsKey(target)) {
			// Probably @Conditional processing knocked it out
			return Collections.emptyMap();
		}
		if (selector.roots.containsKey(target)) {
			// Not a root config
			return Collections.emptyMap();
		}
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
			try {
				for (Annotation annotation : source.getDeclaredAnnotations()) {
					if (!AnnotationUtils.isInJavaLangAnnotationPackage(annotation)) {
						if (ANNOTATION_NAMES
								.contains(annotation.annotationType().getName())) {
							annotations.add(source, annotation);
							for (String config : getConfigurationsForAnnotation(source,
									annotation)) {
								Class<?> type = ClassUtils.resolveClassName(config, null);
								collectAnnotations(type, annotations, seen);
							}
						}
						collectAnnotations(annotation.annotationType(), annotations,
								seen);
					}
				}
			}
			catch (Exception e) {
				// Missing type in annotation (probably)
			}
			collectAnnotations(source.getSuperclass(), annotations, seen);
		}
	}

	public void register(String source, List<String> configs) {
		this.configs.computeIfAbsent(source, key -> new ArrayList<>()).addAll(configs);
		for (String config : configs) {
			this.roots.putIfAbsent(config, source);
		}
	}

}
