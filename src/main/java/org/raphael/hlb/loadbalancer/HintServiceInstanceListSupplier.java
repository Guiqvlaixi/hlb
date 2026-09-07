package org.raphael.hlb.loadbalancer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.loadbalancer.core.HintBasedServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Raphael
 * @since 2029/09/07
 *
 * 继承该类
 * @see org.springframework.cloud.loadbalancer.core.HintBasedServiceInstanceListSupplier
 *
 * 顺便增加一个根据"hidden"元数据决定要不要隐藏实例的功能
 */
public class HintServiceInstanceListSupplier extends HintBasedServiceInstanceListSupplier {

    private static final Logger log = LoggerFactory.getLogger(HintServiceInstanceListSupplier.class);


    public HintServiceInstanceListSupplier(
        ServiceInstanceListSupplier delegate, ReactiveLoadBalancer.Factory<ServiceInstance> factory
    ) {
        super(delegate, factory);
    }

    @Override
    public Flux<List<ServiceInstance>> get() {
        return getDelegate().get();
    }


    @Override
    public Flux<List<ServiceInstance>> get(Request request) {
        return super.get(request)
            .map(this::filteredByHidden);
    }

    /**
     * 过滤需要隐藏的实例
     */
    private List<ServiceInstance> filteredByHidden(List<ServiceInstance> instances) {
        if (instances.isEmpty()) {
            return instances;
        }

        Predicate<ServiceInstance> notHidden = instance ->
            !Boolean.parseBoolean(
                instance.getMetadata().getOrDefault(HintConstant.HIDDEN, HintConstant.FALSE)
            );

        return instances.stream()
                .filter(notHidden)
                .collect(Collectors.toList());
    }

}
