package org.raphael.hlb.loadbalancer;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ConditionalOnBlockingDiscoveryEnabled;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.client.ConditionalOnReactiveDiscoveryEnabled;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

/**
 * @author Raphael
 * @since 2029/09/07
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnDiscoveryEnabled
public class HintLoadBalancerClientConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ReactorLoadBalancer<ServiceInstance> nacosLoadBalancer(
        Environment environment,
        LoadBalancerClientFactory loadBalancerClientFactory,
        NacosDiscoveryProperties nacosDiscoveryProperties
    ) {
        String name = environment.getProperty("loadbalancer.client.name");
        ObjectProvider<ServiceInstanceListSupplier> provider = loadBalancerClientFactory
                .getLazyProvider(name, ServiceInstanceListSupplier.class);

        return new HintLoadBalancer(
            provider,
            name,
            nacosDiscoveryProperties
        );

    }



    @Configuration(
            proxyBeanMethods = false
    )
    @ConditionalOnReactiveDiscoveryEnabled
    @Order(183827465)
    public static class ReactiveSupportConfiguration {

        @Bean
        @ConditionalOnBean({ReactiveDiscoveryClient.class})
        @ConditionalOnMissingBean
        @ConditionalOnProperty(
                value = {"spring.cloud.loadbalancer.configurations"},
                havingValue = "default",
                matchIfMissing = true
        )
        public ServiceInstanceListSupplier discoveryClientServiceInstanceListSupplier(ConfigurableApplicationContext context) {
            return ServiceInstanceListSupplier.builder()
                .withDiscoveryClient()
                .with((ctx, delegate) -> {
                    LoadBalancerClientFactory factory = ctx.getBean(LoadBalancerClientFactory.class);
                    return new HintServiceInstanceListSupplier(delegate, factory);
                })
                .build(context);
        }

        @Bean
        @ConditionalOnBean({ReactiveDiscoveryClient.class})
        @ConditionalOnMissingBean
        @ConditionalOnProperty(
                value = {"spring.cloud.loadbalancer.configurations"},
                havingValue = "zone-preference"
        )
        public ServiceInstanceListSupplier zonePreferenceDiscoveryClientServiceInstanceListSupplier(ConfigurableApplicationContext context) {
            return ServiceInstanceListSupplier.builder()
                .withDiscoveryClient()
                .withZonePreference()
                .with((ctx, delegate) -> {
                    LoadBalancerClientFactory factory = ctx.getBean(LoadBalancerClientFactory.class);
                    return new HintServiceInstanceListSupplier(delegate, factory);
                })
                .build(context);
        }
    }



    @Configuration(
            proxyBeanMethods = false
    )
    @ConditionalOnBlockingDiscoveryEnabled
    @Order(183827466)
    public static class BlockingSupportConfiguration {

        @Bean
        @ConditionalOnBean({DiscoveryClient.class})
        @ConditionalOnMissingBean
        @ConditionalOnProperty(
                value = {"spring.cloud.loadbalancer.configurations"},
                havingValue = "default",
                matchIfMissing = true
        )
        public ServiceInstanceListSupplier discoveryClientServiceInstanceListSupplier(ConfigurableApplicationContext context) {
            return ServiceInstanceListSupplier.builder()
                .withBlockingDiscoveryClient()
                .with((ctx, delegate) -> {
                    LoadBalancerClientFactory factory = ctx.getBean(LoadBalancerClientFactory.class);
                    return new HintServiceInstanceListSupplier(delegate, factory);
                })
                .build(context);
        }

        @Bean
        @ConditionalOnBean({DiscoveryClient.class})
        @ConditionalOnMissingBean
        @ConditionalOnProperty(
                value = {"spring.cloud.loadbalancer.configurations"},
                havingValue = "zone-preference"
        )
        public ServiceInstanceListSupplier zonePreferenceDiscoveryClientServiceInstanceListSupplier(ConfigurableApplicationContext context) {
            return ServiceInstanceListSupplier.builder()
                .withBlockingDiscoveryClient()
                .withZonePreference()
                .with((ctx, delegate) -> {
                    LoadBalancerClientFactory factory = ctx.getBean(LoadBalancerClientFactory.class);
                    return new HintServiceInstanceListSupplier(delegate, factory);
                })
                .build(context);
        }

    }

}
