package com.github.gquintana.kafka.brod.broker;

import java.util.List;
import java.util.Optional;

public interface BrokerService {
    Optional<Broker> getBroker(int id);

    List<Broker> getBrokers();

    Optional<Broker> getControllerBroker();
}
