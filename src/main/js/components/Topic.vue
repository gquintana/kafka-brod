<template>
  <b-container fluid>
    <b-breadcrumb :items="breadcrumb" />
    <div v-if="topic">
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
                <octicon name="issue-reopened" v-if="!replica.in_sync"/>
                <router-link :to="{name:'Broker', params:{id: replica.broker_id}}"><a>{{ replica.broker_id }}</a></router-link>
              </span>
            </template>
          </b-table>
        </b-col>
      </b-row>
    </div>
  </b-container>
</template>
<script>
  import axiosService from '../services/AxiosService'
  import Octicon from 'vue-octicon/components/Octicon.vue'
  const TOPIC_PARTITIONS_FIELDS = [
    {key: 'id', sortable: true},
    {key: 'beginning_offset', tdClass: 'numeric'},
    {key: 'end_offset', tdClass: 'numeric'},
    {key: 'records', tdClass: 'numeric', sortable: true},
    {key: 'replicas'}
  ]
  const TOPIC_PARTITIONS_FIELDS_JMX = TOPIC_PARTITIONS_FIELDS.concat([
    {key: 'size', tdClass: 'numeric', sortable: true},
    {key: 'num_segments', tdClass: 'numeric', sortable: true}
  ])
  export default {
    data: function () {
      return {
        topicName: null,
        topic: [],
        topicPartitions: [],
        topicPartitionsJmx: false
      }
    },
    components: { Octicon },
    created: function () {
      const topicName = this.$route.params.name
      this.topicName = topicName
      axiosService.axios.get(`topics/` + topicName)
        .then(response => {
          this.topic = response.data
          const topicPartitions = []
          for (let partitionId = 0; partitionId < this.topic.partitions; partitionId++) {
            topicPartitions.push({id: partitionId, replicas: []})
          }
          this.topicPartitions = topicPartitions
          this.topicPartitionsJmx = false
          return axiosService.axios.get(`topics/` + topicName + '/partitions')
        })
        .then(response => {
          this.topicPartitions = response.data
          this.topicPartitionsJmx = this.topicPartitions.filter(p => p.size || p.num_segments) !== undefined
        })
        .catch(e => axiosService.helper.handleError(`Topic ${topicName} load failed`, e))
    },
    computed: {
      breadcrumb: function () {
        const breadcrumb = [
          {
            text: 'Topics',
            to: { name: 'Topics' }
          }
        ]
        if (this.topicName) {
          breadcrumb.push({
            text: `Topic ${this.topicName}`,
            to: { name: 'Topic', params: { id: this.topicName } }
          })
        }
        return breadcrumb
      },
      topicPartitionsFields: function () {
        return this.topicPartitionsJmx ? TOPIC_PARTITIONS_FIELDS_JMX : TOPIC_PARTITIONS_FIELDS
      }
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
  min-width: 30px;
}
</style>
