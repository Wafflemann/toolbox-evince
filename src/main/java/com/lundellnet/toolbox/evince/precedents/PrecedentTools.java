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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.function.Function;

import com.lundellnet.toolbox.api.precedents.Precedent;
import com.lundellnet.toolbox.evince.precedents.configs.AnnotationPrecedentConfig;

public class PrecedentTools {

	public static <T, R, P extends AnnotationPrecedent<T, R>> P deduceAccessPrecedence(Field annotatedField) {
		Annotation matrixAnnotation = null;
		
		AnnotationPrecedentConfig<T, R> conf = new AbstractAnnotationPrecedent<T, R>(matrixAnnotation, annotatedField) {

				@Override
				public Function<T, R> applicant() {
					// TODO Auto-generated method stub
					return null;
				}
			};
			
		//TODO finish
		return null;
	}
}
