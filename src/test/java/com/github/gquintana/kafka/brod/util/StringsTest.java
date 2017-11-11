package com.github.gquintana.kafka.brod.util;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class StringsTest {
    @Test
    public void testSplitCamelCase() {
        assertThat(Strings.toSnakeCase("FiveMinuteRate"), equalTo("five_minute_rate"));
        assertThat(Strings.toSnakeCase("AaaBbb123Eee_ddd"), equalTo("aaa_bbb123_eee_ddd"));
        assertThat(Strings.toSnakeCase("kafka.server"), equalTo("kafka_server"));
    }

}
