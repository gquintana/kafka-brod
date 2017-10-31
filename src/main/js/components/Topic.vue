<template>
  <div>
    <h2>Topic {{ topic.name }}</h2>
    <b-container v-if="topic">
      <b-row>
        <b-col sm="2"><label>Partitions</label></b-col>
        <b-col sm="1">{{ topic.partitions }}</b-col>
        <b-col sm="2"><label>Replication factor</label></b-col>
        <b-col sm="1">{{ topic.replication_factor }}</b-col>
        <b-col sm="2"><label>Internal</label></b-col>
        <b-col sm="1">{{ topic.internal }}</b-col>
      </b-row>
      <b-row>
        <b-col sm="2"><label>Configuration</label></b-col>
        <b-col sm="10">
          <table class="table table-striped">
            <thead>
            <tr>
              <th>Key</th>
              <th>Value</th>
            </tr>
            </thead>
            <tbody>
              <tr v-for="(value,key) of topic.config" :key="key">
                <td>{{ key }}</td>
                <td>{{ value }}</td>
              </tr>
            </tbody>
          </table>

        </b-col>
      </b-row>
      <b-row>
        <b-col sm="2"><label>Partitions</label></b-col>
        <b-col sm="10">
          <b-table :items="topicPartitions" :fields="topicPartitionsFields" striped>
            <template slot="replicas" scope="data">
              <span v-for="replica of data.item.replicas" :key="replica.broker_id" class="topic-partition">
                <octicon name="heart" v-if="replica.leader"/>
                <router-link :to="{name:'Broker', params:{id: replica.broker_id}}"><a>{{ replica.broker_id }}</a></router-link>
                <octicon name="issue-reopened" v-if="!replica.in_sync"/>
              </span>
            </template>
          </b-table>
        </b-col>
      </b-row>
    </b-container>
  </div>
</template>
<script>
  import axios from '../services/AxiosService'
  import Octicon from 'vue-octicon/components/Octicon.vue'
  import notificationService from '../services/NotificationService'
  export default {
    data: function () {
      return {
        topic: [],
        topicPartitions: [],
        topicPartitionsFields: [ 'id', 'beginning_offset', 'end_offset', 'records', 'replicas', 'size', 'num_segments' ]
      }
    },
    components: { Octicon },
    created: function () {
      const topicName = this.$route.params.name
      axios.get(`topics/` + topicName)
        .then(response => {
          this.topic = response.data
          return axios.get(`topics/` + topicName + '/partitions')
        })
        .then(response => {
          this.topicPartitions = response.data
        })
        .catch(e => notificationService.notifyError(`Topic ${topicName} load failed: ${e.message}`))
    }
  }
</script>

<style scoped>
.topic-partition {
  display: inline-block;
  text-align: right;
  border: solid 1px #e9ecef;
  margin: 1px;
  padding: 1px;
  background-color: white;
  width: 30px;
}
</style>
