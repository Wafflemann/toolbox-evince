package com.lundellnet.toolbox.evince;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.lundellnet.toolbox.Reflect;
import com.lundellnet.toolbox.api.data_access.AnnotatedElementType;

@SuppressWarnings("unchecked")
public class AnnotationPrecedent {
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
    
    private static final Predicate<AnnotationPrecedent> NULL_PRECEDENCE_CHECK = (p) -> (p != null);
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
    
    public static final Function<Field, AnnotationPrecedent> FIELD_TO_PRECEDENT_MAPPER = (f) -> {
    		for (int i = 0; i < f.getDeclaredAnnotations().length; i++) {
    			for (int j = 0; j < COMPONENT_POINT_ANNOTATIONS.length; j++) {
    			  if (COMPONENT_POINT_ANNOTATIONS[j].equals(f.getDeclaredAnnotations()[i].annotationType()))
    			    return new AnnotationPrecedent(f.getDeclaredAnnotations()[i], f);
    			}
    		}
    		
    		return null;
    	};
    
    public static Stream<AnnotationPrecedent> streamPrecedents(Class<?> sourceType) {
    	return Arrays.stream(Reflect.getDeclaredFields(sourceType))
        	    .map(FIELD_TO_PRECEDENT_MAPPER)
        	    .filter(NULL_PRECEDENCE_CHECK);
    }
    	
	private final Annotation annotation;
	private final Field annotatedField;
	
    private final AnnotatedElementType annotatedElementType;
	private final MatrixParsingStep step;
	
	AnnotationPrecedent(Annotation annotation, Field annotatedField) {
		this.annotation = annotation;
		this.annotatedField = annotatedField;

		this.annotatedElementType = AnnotatedElementType.fromValue(annotation.annotationType().getCanonicalName());
		this.step = PRECEDENT_SWITCH.apply(annotatedElementType);
	}
	
	public Class<? extends Annotation> getAnnotationType() {
		return annotation.annotationType();
	}
	
	public Field getField() {
	  return annotatedField;
	}
	
	public MatrixParsingStep getStep() {
		return step;
	}
}
