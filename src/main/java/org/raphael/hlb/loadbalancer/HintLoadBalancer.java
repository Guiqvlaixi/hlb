package org.raphael.hlb.loadbalancer;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.balancer.NacosBalancer;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Raphael
 * @since 2029/09/07
 */
public class HintLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private static final Logger log = LoggerFactory.getLogger(HintLoadBalancer.class);

    /**
     * 服务ID
     */
    private final String serviceId;

    /**
     * 服务实例列表提供者
     */
    private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

    /**
     * Nacos 服务发现配置
     */
    private final NacosDiscoveryProperties nacosDiscoveryProperties;

    /**
     * 构造方法
     *
     * @param supplierProvider                    服务实例列表提供者
     * @param serviceId                           服务ID
     * @param nacosDiscoveryProperties            Nacos 服务发现配置
     */
    public HintLoadBalancer(
        ObjectProvider<ServiceInstanceListSupplier> supplierProvider,
        String serviceId,
        NacosDiscoveryProperties nacosDiscoveryProperties
    ) {
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = supplierProvider;
        this.nacosDiscoveryProperties = nacosDiscoveryProperties;
    }

    /**
     * @see com.alibaba.cloud.nacos.loadbalancer.NacosLoadBalancer;
     */
    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = this.serviceInstanceListSupplierProvider
                .getIfAvailable(NoopServiceInstanceListSupplier::new);
        /* 注意：其实就这一句差异跟NacosLoadBalancer */
        return supplier.get(request).next()
                .map(this::getInstanceResponse);
    }

    /**
     * 获取下游实例的响应
     *
     * @param serviceInstances 下游实例列表
     * @return 响应
     */
    private Response<ServiceInstance> getInstanceResponse(
            List<ServiceInstance> serviceInstances) {
        if (serviceInstances.isEmpty()) {
            log.warn("No servers available for service: " + this.serviceId);
            return new EmptyResponse();
        }

        try {
            String clusterName = this.nacosDiscoveryProperties.getClusterName();

            List<ServiceInstance> instancesToChoose = serviceInstances;
            if (StringUtils.isNotBlank(clusterName)) {
                List<ServiceInstance> sameClusterInstances = serviceInstances.stream()
                        .filter(serviceInstance -> {
                            String cluster = serviceInstance.getMetadata()
                                    .get("nacos.cluster");
                            return StringUtils.equals(cluster, clusterName);
                        }).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(sameClusterInstances)) {
                    instancesToChoose = sameClusterInstances;
                }
            } else {
                log.warn(
                        "A cross-cluster call occurs，name = {}, clusterName = {}, instance = {}",
                        serviceId, clusterName, serviceInstances);
            }

            ServiceInstance instance = NacosBalancer
                    .getHostByRandomWeight3(instancesToChoose);

            return new DefaultResponse(instance);
        } catch (Exception e) {
            log.warn("LinkLoadBalancer error", e);
            return null;
        }

    }

}