package com.github.gquintana.kafka.brod.broker;

import com.github.gquintana.kafka.brod.jmx.JmxConnection;
import com.github.gquintana.kafka.brod.jmx.JmxService;

public class BrokerJmxService {
    private final JmxService jmxService;

    public BrokerJmxService(JmxService jmxService) {
        this.jmxService = jmxService;
    }

    public Broker enrich(Broker broker) {
        if (broker.getJmxPort() == null) {
            return broker;
        }
        try(JmxConnection connection = jmxService.connect(broker.getHost(), broker.getJmxPort())) {

        }
        return broker;
    }
}
