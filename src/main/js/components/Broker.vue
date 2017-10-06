<template>
  <div>
    <h2>Broker {{ broker.id }}</h2>
    <div v-if="broker">
      <md-layout md-gutter>
        <md-layout md-flex="50">
          <md-input-container>
            <label>Host</label>
            <md-input v-model="broker.host" readonly></md-input>
          </md-input-container>
        </md-layout>
        <md-layout md-flex="12">
          <md-input-container>
            <label>Port</label>
            <md-input v-model="broker.port" readonly></md-input>
          </md-input-container>
        </md-layout>
        <md-layout md-flex="12">
          <md-input-container>
            <label>Protocol</label>
            <md-input v-model="broker.protocol" readonly></md-input>
          </md-input-container>
        </md-layout>
        <md-layout md-flex="12">
          <md-input-container>
            <label>Controller</label>
            <md-input type="checkbox" v-model="broker.controller" checked readonly></md-input>
          </md-input-container>
        </md-layout>
        <md-layout md-flex="12">
          <md-input-container>
            <label>Active</label>
            <md-input type="checkbox" v-model="broker.active" checked readonly ></md-input>
          </md-input-container>
        </md-layout>
      </md-layout>
      <md-layout gutter>
        <md-layout md-flex="12" md-flex-offset="50">
          <md-input-container>
            <label>JMX Port</label>
            <md-input v-model="broker.jmxPort" readonly></md-input>
          </md-input-container>
        </md-layout>
      </md-layout>
      <md-layout gutter>
        <label>Endpoints</label>
        <md-table>
          <md-table-header>
            <md-table-row>
              <md-table-head>Protocol</md-table-head>
              <md-table-head>Host</md-table-head>
              <md-table-head md-numeric>Port</md-table-head>
            </md-table-row>
          </md-table-header>
          <md-table-body>
            <md-table-row v-for="(endpoint, index) of broker.endpoints" :key="index">
              <md-table-cell>{{ endpoint.protocol }}</md-table-cell>
              <md-table-cell>{{ endpoint.host }}</md-table-cell>
              <md-table-cell md-numeric>{{ endpoint.port}}</md-table-cell>
            </md-table-row>
          </md-table-body>
        </md-table>
      </md-layout>
    </div>
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
