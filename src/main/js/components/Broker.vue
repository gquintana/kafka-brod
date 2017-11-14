<template>
  <b-container fluid>
    <b-breadcrumb :items="breadcrumb" />
    <div v-if="broker">
      <b-row>
        <b-col sm="1"><label>Host</label></b-col>
        <b-col sm="4">{{ broker.host }}</b-col>
        <b-col sm="1"><label>Port</label></b-col>
        <b-col sm="2">{{ broker.port }}</b-col>
        <b-col sm="1"><label>Controller</label></b-col>
        <b-col sm="1"><octicon v-if="broker.controller" name="heart" /></b-col>
        <b-col sm="1"><label>Available</label></b-col>
        <b-col sm="1"><octicon v-if="broker.available" name="pulse" label="available"/></b-col>
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
          <b-table striped hover :items="brokerJmxMetrics" :fields="brokerJmxFields"
            :current-page="brokerJmxPagination.currentPage" :per-page="brokerJmxPagination.perPage"/>
          <div v-if="brokerJmxMetrics.length>brokerJmxPagination.perPage">
            <b-pagination :total-rows="brokerJmxMetrics.length" :per-page="brokerJmxPagination.perPage" v-model="brokerJmxPagination.currentPage" />
          </div>
        </b-col>
      </b-row>
    </div>
  </b-container>
</template>
<script>
  import axiosService from '../services/AxiosService'
  import jmxService from '../services/JmxService'
  import Octicon from 'vue-octicon/components/Octicon.vue'
  export default {
    components: { Octicon },
    data: function () {
      return {
        brokerId: null,
        broker: null,
        brokerJmxPagination: {
          perPage: 10,
          currentPage: 1
        },
        brokerJmxFields: jmxService.jmxFields
      }
    },
    created: function () {
      const brokerId = this.$route.params.id
      this.brokerId = brokerId
      axiosService.axios.get(`brokers/` + brokerId)
        .then(response => {
          this.broker = response.data
        })
        .catch(e => axiosService.helper.handleError(`Broker ${brokerId} load failed`))
    },
    computed: {
      breadcrumb: function () {
        const breadcrumb = [
          {
            text: 'Brokers',
            to: { name: 'Brokers' }
          }
        ]
        if (this.brokerId) {
          breadcrumb.push({
            text: `Broker ${this.brokerId}`,
            to: { name: 'Broker', params: { id: this.brokerId } }
          })
        }
        return breadcrumb
      },
      brokerJmxMetrics: function () {
        return jmxService.formatJmxMetrics(this.broker)
      }
    }
  }
</script>
