package org.raphael.hlb.loadbalancer;

import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.context.annotation.Configuration;

/**
 * @author Raphael
 * @since 2029/09/07
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
@HintConditionalOnLoadBalancer
@ConditionalOnNacosDiscoveryEnabled
@LoadBalancerClients(defaultConfiguration = {HintLoadBalancerClientConfiguration.class})
public class HintLoadBalancerAutoConfiguration {

}
