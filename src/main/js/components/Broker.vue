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
        <b-col sm="2">{{ broker.jmxPort }}</b-col>
      </b-row>
      <b-row>
        <b-col sm="1"><label>Endpoints</label></b-col>
        <b-col sm="4">
          <div v-for="endpoint of broker.endpoints">{{ endpoint }}</div>
        </b-col>
      </b-row>
    </b-container>
  </div>
</template>
<script>
  import axios from '../services/AxiosService'
  export default {
    data: function () {
      return {
        broker: [],
        errors: []
      }
    },
    created: function () {
      let brokerId = this.$route.params.id
      axios.get(`brokers/` + brokerId)
        .then(response => {
          this.broker = response.data
        })
        .catch(e => {
          this.errors.push(e)
        })
    }
  }
</script>