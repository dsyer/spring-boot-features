package org.springframework.boot.factory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;

@Aspect
public class FactoryInterceptor {

	private static final Set<String> EXCLUDES = new HashSet<>(
			Arrays.asList(ApplicationListener.class.getName(),
					ApplicationContextInitializer.class.getName()));

	@Around("execution(* org.springframework.boot.SpringApplication.createSpringFactoriesInstances(..))"
			+ " && args(type, .., names)")
	public Object instances(ProceedingJoinPoint joinPoint, Class<?> type,
			Set<String> names) throws Throwable {
		String name = type.getName();
		if (EXCLUDES.contains(name)) {
			return Collections.emptyList();
		}
		return joinPoint.proceed();
	}

	@Before("execution(org.springframework.context.ConfigurableApplicationContext org.springframework.boot.SpringApplication+.run(..)) "
			+ "&& this(application)")
	public void run(SpringApplication application) throws Throwable {
		new SpringApplicationCustomizerAdapter(application).customize();
	}

}
