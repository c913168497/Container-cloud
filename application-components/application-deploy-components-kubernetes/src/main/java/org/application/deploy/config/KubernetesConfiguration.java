package org.application.deploy.config;

import lombok.extern.slf4j.Slf4j;
import org.application.deploy.worker.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({KubernetesProperties.class})
@Slf4j
public class KubernetesConfiguration {

    @Autowired
    private KubernetesProperties kubernetesProperties ;

    @Bean("kubernetesDeployWorker")
    public KubernetesDeployWorker init(){
        KubernetesDeployWorker worker = new KubernetesDeployWorker(kubernetesProperties);
        return worker;
    }

    @Bean("kubernetesStartWorker")
    public KubernetesStartWorker initStartWork(){
        KubernetesStartWorker worker = new KubernetesStartWorker();
        return worker;
    }

    @Bean("kubernetesStopWorker")
    public KubernetesStopWorker initStopWork(){
        KubernetesStopWorker worker = new KubernetesStopWorker();
        return worker;
    }
    @Bean("kubernetesRestartWorker")
    public KubernetesReStartWorker initRestartWork(){
        KubernetesReStartWorker worker = new KubernetesReStartWorker();
        return worker;
    }
    @Bean("kubernetesDestoryWorker")
    public KubernetesDestoryWorker initDestoryWorker(){
        KubernetesDestoryWorker worker = new KubernetesDestoryWorker();
        return worker;
    }
    @Bean("kubernetesUpdateker")
    public KubernetesUpdateWorker initUpdateDestoryWorker(){
    	KubernetesUpdateWorker worker = new KubernetesUpdateWorker(kubernetesProperties);
    	return worker;
    }
}
