package com.github.gquintana.kafka.brod.broker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BrokersResourceTest {

    @Mock
    public BrokerService brokerService;
    public BrokersResource brokersResource;

    @Before
    public void setUp() {
        brokersResource = new BrokersResource(null, brokerService);
    }

    @Test
    public void testGetIds() {
        // Given
        when(brokerService.getBrokers()).thenReturn(asList(new Broker(2), new Broker(3)));
        // When
        List<Broker> brokers = brokersResource.getBrokers();
        // Then
        assertThat(brokers.stream().map(Broker::getId).collect(toList()), hasItems(2, 3));
    }

    @Test
    public void testGetByIdWhenFound() {
        // Given
        when(brokerService.getBroker(eq(2))).thenReturn(Optional.of(new Broker(2)));
        // When
        Response response = brokersResource.getBroker(2);
        // Then
        assertThat(response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
    }


    @Test
    public void testGetByIdWhenNotFound() {
        // Given
        when(brokerService.getBroker(eq(2))).thenReturn(Optional.empty());
        // When
        Response response = brokersResource.getBroker(2);
        // Then
        assertThat(response.getStatus(), equalTo(Response.Status.NOT_FOUND.getStatusCode()));
    }
}
