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
package com.lundellnet.toolbox.evince.precedents;

import static com.lundellnet.toolbox.commons.PredicateUtils.isNotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

import com.lundellnet.toolbox.Reflect;
import com.lundellnet.toolbox.api.data_access.AnnotatedElementType;
import com.lundellnet.toolbox.evince.MatrixParsingStep;
import com.lundellnet.toolbox.evince.precedents.configs.AnnotationPrecedentConfig;

@SuppressWarnings("unchecked")
abstract class AbstractAnnotationPrecedent <T, R>
		implements AnnotationPrecedentConfig<T, R>
{
    //Concerned types denoted by the enum constants first, then at class creation the class types are loaded into an array aswell.
    private static final AnnotatedElementType[] ANNOTATION_TYPES_CONCERNED = new AnnotatedElementType[] {
            AnnotatedElementType.MATRIX_COMPONENT, AnnotatedElementType.MATRIX_COMPONENT_ADAPTER,
            AnnotatedElementType.MATRIX_COMPONENT_FIELD, AnnotatedElementType.MATRIX_COMPONENT_POINT,
            AnnotatedElementType.POINT_LOCATION, AnnotatedElementType.POINT_MAPPING, AnnotatedElementType.POINT_MAPPINGS
        };
    private static final Class<Annotation>[] COMPONENT_POINT_ANNOTATIONS;
    static {
        COMPONENT_POINT_ANNOTATIONS = new Class[ANNOTATION_TYPES_CONCERNED.length];
        
        for (int i = 0; i < ANNOTATION_TYPES_CONCERNED.length; i++) {
            try {
                COMPONENT_POINT_ANNOTATIONS[i] = (Class<Annotation>) Class.forName(ANNOTATION_TYPES_CONCERNED[i].value());
            } catch (ClassNotFoundException ex) {
                // TODO Auto-generated catch block
                ex.printStackTrace();
            }
        }
    }
    
    //private static final Predicate<AbstractAnnotationPrecedent> NULL_PRECEDENCE_CHECK = (p) -> (p != null);
    //assigning a work process type for the type of annotated element,
    private static final Function<AnnotatedElementType, MatrixParsingStep> PRECEDENT_SWITCH = (t) -> {
        switch (t) {
            case MATRIX_COMPONENT:
                return MatrixParsingStep.ITERATE;
            case MATRIX_COMPONENT_ADAPTER:
                return MatrixParsingStep.ITERATE;
            case MATRIX_COMPONENT_FIELD:
                return MatrixParsingStep.TRANSVERSE;
            case MATRIX_COMPONENT_POINT:
                return MatrixParsingStep.ITERATE_AND_MAP;
            case MATRIX_SOURCE:
                return MatrixParsingStep.ITERATE;
            case POINT_LOCATION:
                return MatrixParsingStep.MAP;
            case POINT_MAPPING:
                return MatrixParsingStep.MAP;
            case POINT_MAPPINGS:
                return MatrixParsingStep.MAP;
            default:
                return null;
        }
    };
        
    public static <T, R, C extends AnnotationPrecedentConfig<T, R>> Stream<C> streamPrecedents(Class<?> sourceType) {
    	return Arrays.stream(Reflect.getDeclaredFields(sourceType))
        	    .map((f) -> {
            		for (int i = 0; i < f.getDeclaredAnnotations().length; i++) {
            			for (int j = 0; j < COMPONENT_POINT_ANNOTATIONS.length; j++) {
            			  if (COMPONENT_POINT_ANNOTATIONS[j].equals(f.getDeclaredAnnotations()[i].annotationType()))
            			    return (C) new AbstractAnnotationPrecedent<T, R>(f.getDeclaredAnnotations()[i], f) {
        							@Override
        							public Function<T, R> applicant() {
        								// TODO Auto-generated method stub
        								return null;
        							}
            			  		};
            			}
            		}
            		
            		return null;
        	    })
        	    .filter((p) -> isNotNull(p));
    }
    	
	private final Annotation annotation;
	private final Field annotatedField;
	
    private final AnnotatedElementType annotatedElementType;
	private final MatrixParsingStep decision;
	
	AbstractAnnotationPrecedent(Annotation annotation, Field annotatedField) {
		this.annotation = annotation;
		this.annotatedField = annotatedField;

		this.annotatedElementType = AnnotatedElementType.fromValue(annotation.annotationType().getCanonicalName());
		this.decision = PRECEDENT_SWITCH.apply(annotatedElementType);
	}
	
	@Override
	public Class<? extends Annotation> annotationType() {
		return annotation.annotationType();
	}
	
	@Override
	public Field field() {
	  return annotatedField;
	}
	
	@Override
	public MatrixParsingStep decision() {
		return decision;
	}
}
