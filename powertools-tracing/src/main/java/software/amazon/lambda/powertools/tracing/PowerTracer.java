/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates.
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package software.amazon.lambda.powertools.tracing;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Entity;
import com.amazonaws.xray.entities.Subsegment;

import java.util.function.Consumer;

import static software.amazon.lambda.powertools.core.internal.LambdaHandlerProcessor.serviceName;

/**
 * A class of helper functions to add additional functionality and ease
 * of use.
 *
 */
public final class PowerTracer {

    /**
     * Put an annotation to the current subsegment.
     *
     * @param key the key of the annotation
     * @param value the value of the annotation
     */
    public static void putAnnotation(String key, String value) {
        AWSXRay.getCurrentSubsegmentOptional()
                .ifPresent(segment -> segment.putAnnotation(key, value));
    }

    /**
     * Put additional metadata for the current subsegment.
     *
     * The namespace used will be the namespace of the current subsegment if it
     * is set else it will follow the namespace process as described in
     * {@link PowertoolsTracing}
     *
     * @param key the key of the metadata
     * @param value the value of the metadata
     */
    public static void putMetadata(String key, Object value) {
        String namespace = AWSXRay.getCurrentSubsegmentOptional()
                .map(Subsegment::getNamespace).orElse(serviceName());

        putMetadata(namespace, key, value);
    }

    /**
     * Put additional metadata for the current subsegment.
     *
     * @param namespace the namespace of the metadata
     * @param key the key of the metadata
     * @param value the value of the metadata
     */
    public static void putMetadata(String namespace, String key, Object value) {
        AWSXRay.getCurrentSubsegmentOptional()
                .ifPresent(segment -> segment.putMetadata(namespace, key, value));
    }

    /**
     * Adds a new subsegment around the passed consumer. This also provides access to
     * the newly created subsegment.
     *
     * The namespace used follows the flow as described in {@link PowertoolsTracing}
     *
     * This method is intended for use with multi-threaded programming where the
     * context is lost between threads.
     *
     * @param name the name of the subsegment
     * @param entity the current x-ray context
     * @param subsegment the x-ray subsegment for the wrapped consumer
     */
    public static void withEntitySubsegment(String name, Entity entity, Consumer<Subsegment> subsegment) {
        AWSXRay.setTraceEntity(entity);
        withEntitySubsegment(serviceName(), name, entity, subsegment);
    }

    /**
     * Adds a new subsegment around the passed consumer. This also provides access to
     * the newly created subsegment.
     *
     * This method is intended for use with multi-threaded programming where the
     * context is lost between threads.
     *
     * @param namespace the namespace of the subsegment
     * @param name the name of the subsegment
     * @param entity the current x-ray context
     * @param subsegment the x-ray subsegment for the wrapped consumer
     */
    public static void withEntitySubsegment(String namespace, String name, Entity entity, Consumer<Subsegment> subsegment) {
        AWSXRay.setTraceEntity(entity);
        withSubsegment(namespace, name, subsegment);
    }

    /**
     * Adds a new subsegment around the passed consumer. This also provides access to
     * the newly created subsegment.
     *
     * The namespace used follows the flow as described in {@link PowertoolsTracing}
     *
     * @param name the name of the subsegment
     * @param subsegment the x-ray subsegment for the wrapped consumer
     */
    public static void withSubsegment(String name, Consumer<Subsegment> subsegment) {
        withSubsegment(serviceName(), name, subsegment);
    }

    /**
     * Adds a new subsegment around the passed consumer. This also provides access to
     * the newly created subsegment.
     *
     * @param namespace the namespace for the subsegment
     * @param name the name of the subsegment
     * @param subsegment the x-ray subsegment for the wrapped consumer
     */
    public static void withSubsegment(String namespace, String name, Consumer<Subsegment> subsegment) {
        Subsegment segment = AWSXRay.beginSubsegment("## " + name);
        segment.setNamespace(namespace);
        try {
            subsegment.accept(segment);
        } finally {
            AWSXRay.endSubsegment();
        }
    }
}
