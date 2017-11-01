<template>
  <div>
    <h2>Broker {{ broker.id }}</h2>
    <b-container v-if="broker">
      <b-row>
        <b-col sm="1"><label>Host</label></b-col>
        <b-col sm="4">{{ broker.host }}</b-col>
        <b-col sm="1"><label>Port</label></b-col>
        <b-col sm="2">{{ broker.port }}</b-col>
        <b-col sm="1"><label>Controller</label></b-col>
        <b-col sm="1">{{ broker.controller }}</b-col>
        <b-col sm="1"><label>Active</label></b-col>
        <b-col sm="1">{{ broker.active }}</b-col>
      </b-row>
      <b-row>
        <b-col sm="1"><label>Protocol</label></b-col>
        <b-col sm="3">{{ broker.protocol }}</b-col>
        <b-col sm="2"><label>JMX Port</label></b-col>
        <b-col sm="2">{{ broker.jmx_port }}</b-col>
      </b-row>
      <b-row>
        <b-col sm="1"><label>Endpoints</label></b-col>
        <b-col sm="4">
          <div v-for="endpoint of broker.endpoints">{{ endpoint }}</div>
        </b-col>
      </b-row>
      <b-row v-if="broker.jmx_metrics">
        <b-col sm="1"><label>JMX Metrics</label></b-col>
        <b-col sm="11">
          <b-table striped hover :items="brokerJmxMetrics">
          </b-table>
        </b-col>
      </b-row>
    </b-container>
  </div>
</template>
<script>
  import axiosService from '../services/AxiosService'
  import numeral from 'numeral'

  function formatJmxMetric (jmxMetric) {
    if (jmxMetric.name.indexOf('heap_memory') >= 0 ||
        jmxMetric.name.endsWith('_size') ||
        jmxMetric.name.indexOf('.bytes_') >= 0) {
      // Memory
      jmxMetric.value = numeral(jmxMetric.value).format('0.000b')
    } else if (typeof jmxMetric.value === 'number' && !Number.isInteger(jmxMetric.value)) {
      jmxMetric.value = numeral(jmxMetric.value).format('0.000')
    }
    return jmxMetric
  }

  export default {
    data: function () {
      return {
        broker: null
      }
    },
    created: function () {
      let brokerId = this.$route.params.id
      axiosService.axios.get(`brokers/` + brokerId)
        .then(response => {
          this.broker = response.data
        })
        .catch(e => axiosService.helper.handleError(`Broker ${brokerId} load failed`))
    },
    computed: {
      brokerJmxMetrics () {
        if (!this.broker || !this.broker.jmx_metrics) {
          return null
        }
        const jmxMetrics = this.broker.jmx_metrics
        return Object.keys(jmxMetrics).map(k => {
          return {name: k, value: jmxMetrics[k]}
        }).map(formatJmxMetric)
      }
    }
  }
</script>
