<template>
  <b-container>
    <b-breadcrumb :items="breadcrumb" />
    <div v-if="consumer">
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
    </div>
  </b-container>
</template>
<script>
  import axiosService from '../services/AxiosService'
  import lagService from '../services/LagService'
  import jmxService from '../services/JmxService'
  import Octicon from 'vue-octicon/components/Octicon.vue'

  export default {
    data: function () {
      return {
        groupId: null,
        consumer: null
      }
    },
    components: { Octicon },
    created: function () {
      const groupId = this.$route.params.groupId
      const consumerId = this.$route.params.id
      this.groupId = groupId
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
      },
      breadcrumb: function () {
        const breadcrumb = [
          {
            text: 'Consumer Groups',
            to: { name: 'ConsumerGroups' }
          },
          {
            text: `Group ${this.groupId}`,
            to: { name: 'ConsumerGroup', params: { id: this.groupId } }
          }
        ]
        if (this.consumer) {
          breadcrumb.push({
            text: `Consumer ${this.consumer.id}`,
            to: { name: 'Consumer', params: { groupId: this.groupId, id: this.consumer.id } }
          })
        }
        return breadcrumb
      }

    }

  }
</script>
