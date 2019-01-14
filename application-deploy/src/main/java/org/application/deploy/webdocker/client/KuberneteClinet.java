package org.application.deploy.webdocker.client;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.Getter;

@Getter
public class KuberneteClinet {

    private KubernetesClient client;
    public KuberneteClinet(String masterUrl) {
        Config config = new ConfigBuilder().withMasterUrl(masterUrl).build();
        this.client = new DefaultKubernetesClient(config);
    }
}

