<template>
  <div>
    <h2>Brokers</h2>
    <b-container v-if="brokers && brokers.length">
      <b-table striped hover :items="brokers" :fields="brokerFields" @row-clicked="brokerClicked" class="table-clickable">
        <template slot="controller" scope="data">
          <octicon name="heart" v-if="data.item.controller"/>
        </template>
      </b-table>
    </b-container>
  </div>
</template>

<script>
  import Octicon from 'vue-octicon/components/Octicon.vue'
  import axiosService from '../services/AxiosService'
  export default {
    data: function () {
      return {
        brokers: [],
        brokerFields: [ 'id', 'host', 'port', 'controller' ]
      }
    },
    components: { Octicon },
    created: function () {
      axiosService.axios.get(`brokers`)
        .then(response => {
          this.brokers = response.data
        })
        .catch(e => axiosService.helper.handleError('Brokers load failed', e))
    },
    methods: {
      brokerClicked: function (broker) {
        this.$router.push({name: 'Broker', params: { id: broker.id }})
      }
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
