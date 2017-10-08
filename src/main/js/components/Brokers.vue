<template>
  <div>
    <h2>Brokers</h2>
    <b-container v-if="brokers && brokers.length">
      <b-table striped hover :items="brokers" :fields="brokerFields" @row-clicked="brokerClicked"/>
    </b-container>
  </div>
</template>

<script>
  import axios from '../services/AxiosService'
  export default {
    data: function () {
      return {
        brokers: [],
        brokerFields: [ 'id', 'host', 'port', 'controller' ],
        errors: []
      }
    },
    created: function () {
      axios.get(`brokers`)
        .then(response => {
          this.brokers = response.data
        })
        .catch(e => {
          this.errors.push(e)
        })
    },
    methods: {
      brokerClicked: function (broker) {
        this.$router.push({name: 'Broker', params: { id: broker.id }})
        console.log(broker)
      }
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
