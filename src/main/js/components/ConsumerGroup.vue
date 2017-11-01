<template>
  <div>
    <h2>Consumer Group {{ group.group_id}}</h2>
    <b-container v-if="group">
      <b-row>
        <b-col sm="2"><label>Protocol</label></b-col>
        <b-col sm="3">{{ group.protocol }}</b-col>
        <b-col sm="2"><label>State</label></b-col>
        <b-col sm="1">{{ group.state }}</b-col>
        <b-col sm="2"><label>Assignment</label></b-col>
        <b-col sm="2">{{ group.assignment_strategy }}</b-col>
      </b-row>
      <b-row>
        <b-col sm="2"><label>Members</label></b-col>
        <b-col sm="10">
          <b-table striped hover :items="group.members" :fields="memberFields" @row-clicked="memberClicked" class="table-clickable"/>
        </b-col>
      </b-row>
      <b-row>
        <b-col sm="2"><label>Topics</label></b-col>
        <b-col sm="10">
          <b-table striped hover :items="topics" :fields="topicFields"/>
        </b-col>
      </b-row>
      <b-row v-if="selectedMember">
        <b-col sm="2"><label>Partitions</label></b-col>
        <b-col sm="10">
          <b-table striped hover :items="selectedMember.partitions"/>
        </b-col>
      </b-row>
    </b-container>
  </div>
</template>
<script>
  import axiosService from '../services/AxiosService'
  import Octicon from 'vue-octicon/components/Octicon.vue'

  function isValidLag (lag) {
    return lag === 0 || lag > 0
  }
  function sumLag (lag1, lag2) {
    if (isValidLag(lag1)) {
      if (isValidLag(lag2)) {
        return lag1 + lag2
      } else {
        return lag1
      }
    } else {
      return lag2
    }
  }

  function partitionByTopicReducer (topicMap, partition) {
    let topic = topicMap.get(partition.topic_name)
    if (topic) {
      topic.partition_count++
      topic.lag_total = sumLag(topic.lag_total, partition.lag)
    } else {
      topic = {
        name: partition.topic_name,
        partition_count: 1,
        lag_total: partition.lag
      }
      topicMap.set(partition.topic_name, topic)
    }
    return topicMap
  }

  export default {
    data: function () {
      return {
        group: [],
        memberFields: [ 'client_id', 'client_host', 'member_id', 'partition_count', 'lag_total' ],
        topics: [],
        topicFields: [ 'name', 'partition_count', 'lag_total' ],
        selectedMember: null
      }
    },
    components: { Octicon },
    created: function () {
      const groupId = this.$route.params.id
      axiosService.axios.get(`groups/` + groupId)
        .then(response => {
          const group = response.data
          const partitions = []
          group.members.forEach(member => {
            member.partition_count = member.partitions.length
            member.lag_total = member.partitions.reduce(sumLag, 0)
            member.partitions.forEach(partition => partitions.push(partition))
          })
          this.topics = Array.from(partitions.reduce(partitionByTopicReducer, new Map()).values())
          this.group = group
          this.selectedMember = null
        })
        .catch(e => axiosService.helper.handleError(`Consumer Group ${groupId} load failed`, e))
    },
    methods: {
      memberClicked: function (member) {
        this.selectedMember = member
      }
    }

  }
</script>
