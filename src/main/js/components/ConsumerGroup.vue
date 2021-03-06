<template>
  <b-container fluid>
    <b-breadcrumb :items="breadcrumb" />
    <div v-if="group">
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
          <b-table striped hover :items="group.members" :fields="memberFields"
            :current-page="memberPagination.currentPage" :per-page="memberPagination.perPage"
            @row-clicked="memberClicked" class="table-clickable">
            <template slot="id" scope="data">
              <router-link :to="{ name: 'Consumer', params: { groupId: group.id, id: data.item.id } }"><a>{{ data.item.id }}</a></router-link>
            </template>
          </b-table>
          <div v-if="group.members.length>memberPagination.perPage">
            <b-pagination :per-page="memberPagination.perPage" :total-rows="group.members.length" v-model="memberPagination.currentPage"/>
          </div>
        </b-col>
      </b-row>
      <b-row>
        <b-col sm="2"><label>Topics</label></b-col>
        <b-col sm="10">
          <b-table striped hover :items="topics" :fields="topicFields"
            :current-page="topicPagination.currentPage" :per-page="topicPagination.perPage"
            >
            <template slot="name" scope="data">
              <router-link :to="{name:'Topic', params:{name: data.item.name}}"><a>{{ data.item.name }}</a></router-link>
            </template>
          </b-table>
          <div v-if="topics.length>topicPagination.perPage">
            <b-pagination :per-page="topicPagination.perPage" :total-rows="topics.length" v-model="topicPagination.currentPage"/>
          </div>
        </b-col>
      </b-row>
    </div>
  </b-container>
</template>
<script>
  import axiosService from '../services/AxiosService'
  import lagService from '../services/LagService'
  import Octicon from 'vue-octicon/components/Octicon.vue'

  function partitionByTopicReducer (topicMap, partition) {
    let topic = topicMap.get(partition.topic_name)
    if (topic) {
      topic.partition_count++
      topic.lag_total = lagService.sumLag(topic.lag_total, partition.lag)
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
        memberPagination: {
          perPage: 10,
          currentPage: 1
        },
        memberFields: [
          {key: 'id', sortable: true},
          {key: 'client_id'},
          {key: 'client_host'},
          {key: 'partition_count', tdClass: 'numeric', sortable: true},
          {key: 'lag_total', tdClass: 'numeric', sortable: true}
        ],
        topics: [],
        topicPagination: {
          perPage: 10,
          currentPage: 1
        },
        topicFields: [
          {key: 'name', sortable: true},
          {key: 'partition_count', tdClass: 'numeric', sortable: true},
          {key: 'lag_total', tdClass: 'numeric', sortable: true}
        ]
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
            member.client_host = member.client_host || member.client_ip
            member.lag_total = lagService.computeTotalLag(member.partitions)
            member.partitions.forEach(partition => partitions.push(partition))
          })
          this.topics = Array.from(partitions.reduce(partitionByTopicReducer, new Map()).values())
          this.group = group
        })
        .catch(e => axiosService.helper.handleError(`Consumer Group ${groupId} load failed`, e))
    },
    methods: {
      memberClicked: function (member) {
        this.$router.push({ name: 'Consumer', params: { groupId: this.group.id, id: member.id } })
      }
    },
    computed: {
      breadcrumb: function () {
        const breadcrumb = [
          {
            text: 'Consumer Groups',
            to: { name: 'ConsumerGroups' }
          }
        ]
        if (this.group) {
          breadcrumb.push({
            text: `Group ${this.group.id}`,
            to: { name: 'ConsumerGroup', params: { id: this.group.id } }
          })
        }
        return breadcrumb
      }
    }

  }
</script>
