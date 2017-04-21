/*
 Copyright 2017 Appropriate Technologies LLC.

 This file is part of toolbox-evince, a component of the Lundellnet Java Toolbox.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.lundellnet.toolbox;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.function.Function;

import org.apache.log4j.Logger;

import com.lundellnet.toolbox.evince.MutationType;

/**
 * Class for handling basic refection.
 */
public class Reflect {
    
    private static final Logger DEFAULT_LOG = Logger.getLogger(Reflect.class);
    private static final String GET_SET_REGEX = "\\w{1}(\\w*)([A-Z]*.*)";
    
    private Reflect() {}
    
    @SuppressWarnings("unchecked")
	public static <T> Class<T> getClassFromName(String classPath) {
    	try {
			return (Class<T>) Class.forName(classPath);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return null;
    }
    
    /**
     * Gets the public method from the specified method name, class object,
     * and method argument types.
     * 
     * @param methodName the name of the desired method.
     * @param methodClass the class object in which the method exists.
     * @param types the method argument types.
     * @return returns a method object, null if nothing is found.
     */
    public static Method getPublicMethod(String methodName,
            Class<?> methodClass, Class<?>... types) {
        Method method = null;
        try {
            method = methodClass.getMethod(methodName, types);
        } catch (NoSuchMethodException ex) {
            log().error("Error getting public method: \"" + methodName + "\"\nEX: ",ex);
            throw new RuntimeException(ex);
        } catch (SecurityException ex) {
            log().error("Error getting public method: \"" + methodName + "\"\nEx: ", ex);
            throw new RuntimeException(ex);
        }
        
        return method;
    }
    
    /**
     * Gets all public methods from the specified class object.
     * 
     * @param methodClass class to extract all public methods from.
     * @return return an array of methods associated with the specified class.
     */
    public static Method[] getPublicMethods(Class<?> methodClass) {
        Method[] methods = null;
        
        try {
            methods = methodClass.getMethods();
        } catch (SecurityException ex) {
            log().error(
                "Error getting public methods from class: \"" +
                    methodClass.getName() + "\"\nEX: ",
                ex
            );
            
            throw new RuntimeException(ex);
        }
        
        return methods;
    }
    
    public static Method[] getDeclaredMethods(Class<?> methodClass) {
    	Method[] methods = null;
    	
    	try {
    		methods = methodClass.getDeclaredMethods();
    	} catch (SecurityException ex) {
    		log().error("Error getting methods from class: \"" + methodClass.getName() + "\"\n EX: ", ex);
    	}
    	
    	return methods;
    }
    
    public static Method getMutationMethod(MutationType mutationType, Class<?> methodClass, Field mutationField) {
		String fieldName = mutationField.getName();
		
		return Reflect.getPublicMethod(
				fieldName.replaceAll(GET_SET_REGEX, mutationType.name().toLowerCase().concat(fieldName.substring(0, 1).toUpperCase()) + "$1$2"),
				methodClass,
				(mutationType.equals(MutationType.GET) ? mutationField.getType() : null)
			);
    }
    
    
    /**
     * Invokes a specified method object under a specific object instance
     * passing arguments to the specified method being invoked.
     * 
     * @param <T> The object instance type.
     * @param method method to be invoked.
     * @param objectInstance object instance the method is being invoked under.
     * @param args the arguments to be passed to the invoked method.
     * @return returns the returned data from the invoked method.
     */
    public static <T> Object invokePublicMethod(Method method, T objectInstance,
            Object... args) {
        Object data = null;
        try {
            boolean accessible = method.isAccessible();
            /*
             * As long as the method is public, invoke it regardless if it's accessible.
             */
            if (
                !accessible
            &&
                (method.getModifiers() == Modifier.PUBLIC)
            ) {
                method.setAccessible(true);
                data = method.invoke(objectInstance, args);
                method.setAccessible(accessible);
            } else {
                data = method.invoke(objectInstance, args);
            }
        } catch (IllegalAccessException ex) {
            log().error("Error invoking public method: \"" + method.getName() + "\" isAccessible: " + method.isAccessible() + "\nEX: ", ex);
            throw new RuntimeException(ex);
        } catch (IllegalArgumentException ex) {
            log().error("Error invoking public method: \"" + method.getName() + "\"\nEX: ",ex);
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            log().error("Error invoking public method: \"" + method.getName() + "\" EX: " + ex + "\nTarget Ex: ", ex.getTargetException());
            throw new RuntimeException(ex);
        } catch (NullPointerException ex) {
            log().error("Error invoking public method: \"" + method.getName() + "\"\nEX: ", ex);
            throw new RuntimeException(ex);
        }
        
        return data;
    }
    
    /**
     * Gets the public field object from a specified class object.
     * 
     * @param fieldName the name of the public field desired.
     * @param fieldClass the class object in which the field object exists.
     * @return returns a field object, null if nothing is found.
     */
    public static Field getPublicField(String fieldName, Class<?> fieldClass) {
        Field field = null;
        try {
            field = fieldClass.getField(fieldName);
        } catch (NoSuchFieldException ex) {
            log().error("Error accessing public field: \"" + fieldName + "\"\nEX: ", ex);
            throw new RuntimeException(ex);
        } catch (SecurityException ex) {
            log().error("Error accessing public field: \"" + fieldName + "\"\nEX: ", ex);
            throw new RuntimeException(ex);
        }
        return field;
    }
    
    /**
     * Gets an array of public field objects from a specified class object.
     * 
     * @param fieldClass the class object in which the field object exists.
     * @return returns an array of field objects, null if nothing is found.
     */
    public static Field[] getPublicFields(Class<?> fieldClass) {
        Field[] fields = null;
        try {
            fields = fieldClass.getFields();
        } catch (SecurityException ex) {
            log().error("Error accessing public fields in class: \"" + fieldClass.getName() + "\"\nEX: ", ex);
            throw new RuntimeException(ex);
        }
        
        return fields;
    }
    
    public static Field getDeclaredField(String fieldName, Class<?> fieldClass) {
        Field field = null;
        try {
            field = fieldClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            log().error("Error accessing declared field: \"" + fieldName + "\"\nEX: ", ex);
            throw new RuntimeException(ex);
        } catch (SecurityException ex) {
            log().error("Error accessing declared field: \"" + fieldName + "\"\nEX: ", ex);
            throw new RuntimeException(ex);
        }
        return field;
    }
    
    public static Field[] getDeclaredFields(Class<?> fieldClass) {
    	Field[] fields = null;
    	try {
    		fields = fieldClass.getDeclaredFields();
    	} catch (SecurityException ex) {
    		log().error("Error accessing public fields in class: \"" + fieldClass.getName() + "\"\n EX: ", ex);
    	}
    	
    	return fields;
    }
    
    /**
     * Gets the value from a public field by using a field name, class object
     * the field resides in, and object instance to extract the value from.
     * 
     * @param <T> the value type being returned.
     * @param <E> the object instance type.
     * @param fieldName the name of the field to extract a value from.
     * @param fieldClass the class object in which the field exists.
     * @param fieldClassInstance the object instance to extract the value from.
     * @return returns the value of the public field, null if nothing found.
     */
    public static <T, E> T getPublicFieldValue(String fieldName,
            Class<?> fieldClass, E fieldClassInstance) {
        return getPublicFieldValue(
                getPublicField(fieldName, fieldClass),
                fieldClassInstance
            );
    }
    
    /**
     * Gets the value from a public field by using a field object and the
     * object instance to extract the value from.
     * 
     * @param <T> the value type being returned.
     * @param <E> the object instance type.
     * @param field the field object to extract a value from.
     * @param fieldClassInstance the object instance to extract the value from.
     * @return returns the value of the public field, null if nothing found.
     */
    public static <T, E> T getPublicFieldValue(Field field, E fieldClassInstance) {
        Class<?> type = field.getType();
        T returnValue = null;
        
        try {
            boolean accessible = field.isAccessible();
            if (
                !accessible
            &&
                (field.getModifiers() == Modifier.PUBLIC)
            ) {
                field.setAccessible(true);
                returnValue = (T)type.cast(field.get(fieldClassInstance));
                field.setAccessible(accessible);
            } else {
                returnValue = (T)type.cast(field.get(fieldClassInstance));
            }
        } catch (IllegalArgumentException ex) {
            log().error("Error accessing public field: \"" + field.getName() + "\"'s value.\nEX: ", ex);
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            log().error("Error accessing public field: \"" + field.getName() + "\"'s value. isAccessible: " + field.isAccessible() + "\nEX: ", ex);
            throw new RuntimeException(ex);
        }
        
        return returnValue;
    }
    
    /**
     * Sets a value to a public field object inside a specified object instance.
     * 
     * @param <T> the value type being returned.
     * @param <E> the object instance type.
     * @param field the field object to set the value of.
     * @param value the value to set for the specified field object.
     * @param fieldClassInstance the object instance to set the specified
     * field object's value in.
     * @return returns the value set to the field object, null if no value
     * is set.
     */
    public static <T, E> T setPublicFieldValue(Field field, T value,
            E fieldClassInstance) {
        T returnValue = null;
        
        try {
            boolean accessible = field.isAccessible();
            if (
                !accessible
            &&
                (field.getModifiers() == Modifier.PUBLIC)
            ) {
                field.setAccessible(true);
                field.set(fieldClassInstance, value);
                field.setAccessible(accessible);
            } else {
                field.set(fieldClassInstance, value);
            }
            
            returnValue = value;
        } catch (IllegalArgumentException ex) {
            log().error("Error setting public field: \"" + field.getName() + "\"\nEX: ", ex);
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            log().error("Error setting public field: \"" + field.getName() + "\"\nEX: ", ex);
            throw new RuntimeException(ex);
        }
        
        return returnValue;
    }
    
    @SuppressWarnings("unchecked")
	public static <E extends Enum<E>> Function<Field, E> enumInstance() {
    	return (f) -> (E) Reflect.invokePublicMethod(Reflect.getPublicMethod("valueOf", f.getType(), String.class), null, f.getName());
    }
    
    public static <E extends Enum<E>> E enumInstance(Field f) {
    	return Reflect.<E>enumInstance().apply(f);
    }
    
    /**
     * Gets the appropriate constructor from the specified class object.
     * 
     * @param <T> Class type to get the constructor from.
     * @param findByMethod <true> for using Reflect.class's method for finding
     * the constructor or <false> for using java's method for finding the constructor.
     * @param clazz the class object to get the constructor from.
     * @param params the params the constructor takes in.
     * @return returns a constructor from the above specified class.
     */
    public static <T> Constructor<T> getConstructor(
            boolean findByMethod, Class<T> clazz, Object... params
    ) {
        try {
            return findByMethod ?
                    Reflect.<T>findConstructor(clazz, params) :
                    clazz.getConstructor(getParamTypes(params));
        } catch (NoSuchMethodException ex) {
            log().error("Error finding Constructor for class: \"" + clazz.getName() + "\"\nEX: ", ex);
            throw new RuntimeException(ex);
        }
    } 
    
    /**
     * Invokes a constructor specified by the constructors argument parameters
     * to instantiate a class object and return that instantiated class.
     * 
     * @param <T> type of the instantiated class being returned.
     * @param classToInstantiate class object to instantiate.
     * @param params argument parameters the constructor takes in.
     * @return returns an instantiated class.
     */
    public static <T> T instantiateClass(Class<?> classToInstantiate, Object... params) {
        try {
            Constructor<?> constructor = getConstructor(
                    doParamsContainProxies(params), classToInstantiate, params
                );
        
            return Reflect.<T>instantiateClass(constructor, params);
        } catch (SecurityException ex) {
            log().error("Error instantiating class: \"" + classToInstantiate.getName() + "\"\nEX: ", ex);
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Invokes the specified constructor using passed argument parameters then
     * returns the instantiated class created by the constructor.
     * 
     * @param <T> type of the instantiated class being returned.
     * @param constructor object constructor to invoke.
     * @param params argument parameters to pass to the constructor.
     * @return returns an instantiated class.
     */
    public static <T> T instantiateClass(Constructor<?> constructor, Object... params) {
        T classInstance = null;
        boolean accessible  = constructor.isAccessible();
        
        try {
            if (!accessible) {
                constructor.setAccessible(true);
                classInstance = (T) constructor.newInstance(params);
                constructor.setAccessible(accessible);
            }
        } catch (InstantiationException ex) {
            log().error("Error instantiating class: \"" + constructor.getName() + "\"\nEX: ", ex);
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            log().error("Error instantiating class: \"" + constructor.getName() + "\"\nEX: ", ex);
            throw new RuntimeException(ex);
        } catch (IllegalArgumentException ex) {
            log().error("Error instantiating class: \"" + constructor.getName() + "\"\nEX: ", ex);
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            log().error("Error instantiating class: \"" + constructor.getName() + "\" EX: " +  ex + "\nTarget EX: ", ex.getTargetException());
            throw new RuntimeException(ex);
        }
        
        return classInstance;
    }
    
    public static void paramMatcher(Method method, Object[] parameters) {
        // The expected parameters and the passed parameter list at this
        // point are expected to be equal in length, otherwise TestNG would have
        // thrown an exception at this point.
        Class<?>[] paramTypes = method.getParameterTypes();
        // If necessary or possible, convert any parameters into the required
        // types used by the method.
        for (int i = 0; i < paramTypes.length; ++i) {
            if (parameters[i] == null) {
                continue;
            }
            
            Class<?> givenParamType = parameters[i].getClass();
            Class<?> expectedParamType = paramTypes[i];
            if (!expectedParamType.isAssignableFrom(givenParamType) && givenParamType.isAssignableFrom(String.class)) {
                try {
                    String paramString = (String) parameters[i];
                    if (!expectedParamType.isEnum()) {
                        if (expectedParamType.isPrimitive()) {
                            if (expectedParamType.equals(boolean.class)) {
                                expectedParamType = Boolean.class;
                            } else if (expectedParamType.equals(byte.class)) {
                                expectedParamType = Byte.class;
                            } else if (expectedParamType.equals(char.class)) {
                                expectedParamType = Character.class;
                            } else if (expectedParamType.equals(short.class)) {
                                expectedParamType = Short.class;
                            } else if (expectedParamType.equals(int.class)) {
                                expectedParamType = Integer.class;
                            } else if (expectedParamType.equals(long.class)) {
                                expectedParamType = Long.class;
                            } else if (expectedParamType.equals(float.class)) {
                                expectedParamType = Float.class;
                            } else if (expectedParamType.equals(double.class)) {
                                expectedParamType = Double.class;
                            }
                        }
                        Constructor ctor = expectedParamType.getConstructor(String.class);
                        parameters[i] = ctor.newInstance(paramString);
                    } else {
                        for (Object obj : expectedParamType.getEnumConstants()) {
                            Enum enumVal = (Enum) obj;
                            if (enumVal.name().equals(paramString)) {
                                parameters[i] = enumVal;
                                break;
                            }
                        }
                    }
                } catch (InstantiationException ex) {
                    log().error("Unable to convert String into appropriate type "
                            + expectedParamType.getSimpleName()
                            + " because the target is an interface or an abstract class");
                } catch (IllegalAccessException ex) {
                    log().error("Unable to convert String into appropriate type "
                            + expectedParamType.getSimpleName()
                            + " because the constructor is not accessible.");
                } catch (IllegalArgumentException ex) {
                    log().error("Unable to convert String into appropriate type "
                            + expectedParamType.getSimpleName()
                            + " because the constructor doesn't take a String.");
                } catch (InvocationTargetException ex) {
                    log().error("Unable to convert String into appropriate type "
                            + expectedParamType.getSimpleName()
                            + " because the constructor threw an exception.", ex.getCause());
                } catch (NoSuchMethodException ex) {
                    log().error("Unable to convert String into appropriate type "
                            + expectedParamType.getSimpleName()
                            + " because no constructor was found that takes a String.");
                } catch (SecurityException ex) {
                    log().error("Unable to convert String into appropriate type "
                            + expectedParamType.getSimpleName()
                            + " because of a SecurityException.");
                }
            }
        }
    }
    
    private static Logger log() {
        Logger log = null; /*getCurrentTestLog*/
        
        return (log != null) ?
                log : DEFAULT_LOG;
    }
    
    private static boolean doParamsMatch(Constructor<?> constructor, Object... params) {
        boolean status = false;
        int matchCount = 0;
        Class<?>[] constructorTypes = constructor.getParameterTypes();
        
        if (constructorTypes.length != params.length) {
            return status;
        }
        
        if (constructorTypes.length == 0 && params.length == 0) {
            return true;
        }
        
        for (Class<?> constructorType : constructorTypes) {
            for (Object param : params) {
                Class<?> paramClass = param.getClass();
                
                if (paramClass.isSynthetic()) {
                    try {
                        if (constructorType.cast(param) != null) {
                            matchCount++;
                            break;
                        }
                    } catch (Exception ex) {
                        break;
                    }
                }
                
                if (constructorType.isPrimitive()) {
                    if (
                        paramClass.equals(Boolean.class)
                    &&  constructorType.equals(boolean.class)
                    ) {
                        matchCount++;
                        break;
                    } else if (
                        paramClass.equals(Byte.class)
                    &&  constructorType.equals(byte.class)
                    ) {
                        matchCount++;
                        break;
                    } else if (
                        paramClass.equals(Character.class)
                    &&  constructorType.equals(char.class)
                    ) {
                        matchCount++;
                        break;
                    } else if (
                        paramClass.equals(Short.class)
                    &&  constructorType.equals(short.class)
                    ) {
                        matchCount++;
                        break;
                    } else if (
                        paramClass.equals(Integer.class)
                    &&  constructorType.equals(int.class)
                    ) {
                        matchCount++;
                        break;
                    } else if (
                        paramClass.equals(Long.class)
                    &&  constructorType.equals(long.class)
                    ) {
                        matchCount++;
                        break;
                    } else if(
                        paramClass.equals(Float.class)
                    &&  constructorType.equals(float.class)
                    ) {
                        matchCount++;
                        break;
                    } else if(
                        paramClass.equals(Double.class)
                    &&  constructorType.equals(double.class)
                    ) {
                        matchCount++;
                        break;
                    }
                } else if (constructorType.isInstance(param)) {
                    matchCount++;
                    break;
                }
            }
            
            if (matchCount == constructorTypes.length) {
                status = true;
                break;
            }
        }
        
        return status;
    }
    
    private static boolean doParamsContainProxies(Object... params) {
        boolean status = false;
        
        for (Object param : params) {
            if (Proxy.isProxyClass(param.getClass())) {
                status = true;
                break;
            }
        }
        
        return status;
    }
    
    private static Class<?>[] getParamTypes(Object... params) {
        Class<?>[] paramTypes = new Class<?>[params.length];
        
        int i = 0;
        for (Object param: params) {
            paramTypes[i] = param.getClass();
            i++;
        }
        
        return paramTypes;
    }
    
    private static <T> Constructor<T> findConstructor(Class<T> clazz, Object... params) {
        Constructor<T> desiredConstructor = null;
        
        for(Constructor<?> constructor : clazz.getConstructors()) {
            if (doParamsMatch(constructor, params)) {
                desiredConstructor = (Constructor<T>) constructor;
                break;
            }
        }
        
        return desiredConstructor;
    }
}
