package org.raphael.hlb.loadbalancer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Raphael
 * @since 2029/09/07
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ConditionalOnProperty(value = {"spring.cloud.loadbalancer.hint.enabled"}, havingValue = "true")
public @interface HintConditionalOnLoadBalancer {


}
