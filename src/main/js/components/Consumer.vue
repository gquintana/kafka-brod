<template>
  <div>
    <h2>Consumer {{ groupId }} {{ consumer.id}}</h2>
    <b-container v-if="consumer">
      <b-row>
        <b-col sm="2"><label>Client Id</label></b-col>
        <b-col sm="3">{{ consumer.client_id }}</b-col>
        <b-col sm="2"><label>Host</label></b-col>
        <b-col sm="1">{{ consumer.client_host }}</b-col>
        <b-col sm="2"><label>Lag Total</label></b-col>
        <b-col sm="1">{{ consumer.lag_total }}</b-col>
      </b-row>
      <b-row>
        <b-col sm="2"><label>Partitions</label></b-col>
        <b-col sm="10">
          <b-table striped hover :items="consumer.partitions"/>
        </b-col>
      </b-row>
      <b-row v-if="consumer.jmx_metrics">
        <b-col sm="2"><label>JMX Metrics</label></b-col>
        <b-col sm="10">
          <b-table striped hover :items="consumerJmxMetrics"/>
        </b-col>
      </b-row>
    </b-container>
  </div>
</template>
<script>
  import axiosService from '../services/AxiosService'
  import lagService from '../services/LagService'
  import jmxService from '../services/JmxService'
  import Octicon from 'vue-octicon/components/Octicon.vue'

  export default {
    data: function () {
      return {
        consumer: null
      }
    },
    components: { Octicon },
    created: function () {
      const groupId = this.$route.params.groupId
      const consumerId = this.$route.params.id
      axiosService.axios.get(`groups/` + groupId + '/consumers/' + consumerId)
        .then(response => {
          const consumer = response.data
          consumer.lag_total = lagService.computeTotalLag(consumer.partitions)
          this.consumer = consumer
        })
        .catch(e => axiosService.helper.handleError(`Consumer ${consumerId} load failed`, e))
    },
    computed: {
      consumerJmxMetrics: function () {
        return jmxService.formatJmxMetrics(this.consumer)
      }
    }

  }
</script>
