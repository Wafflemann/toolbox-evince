package com.lundellnet.toolbox;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.lundellnet.toolbox.api.data_access.AnnotatedElementType;

class AnnotationProxy <T extends Annotation>
	implements Annotation, InvocationHandler, Serializable
{
	private static final long serialVersionUID = 1337L;
	
	private final Class<T> annotationType;
	private final Map<String, Supplier<Object>> valueMap = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	AnnotationProxy(AnnotatedElementType annotatedType, Supplier<T> instanceSupplier) {
		Class<T> annotationClass = null;
		
		try {
			annotationClass = (Class<T>) Class.forName(annotatedType.value());
		} catch (ClassNotFoundException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		
		this.annotationType = annotationClass;

		Arrays.stream(annotationClass.getDeclaredMethods()).forEach((m) ->
			valueMap.put(m.getName(), () -> {
				try {
					return m.invoke(instanceSupplier.get());
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
					return null;
				}
			}
		));
				
	}
	
	@Override
	public Object invoke(Object p, Method m, Object[] a) {
		return valueMap.get(m.getName()).get();
	}

	@Override
	public Class<T> annotationType() {
		return annotationType;
	}
	
	//you're pry gonna have to do the equals and toString
}
