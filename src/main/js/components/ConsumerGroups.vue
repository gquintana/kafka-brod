<template>
  <div>
    <h2>Consumer Groups</h2>
    <b-container v-if="groups && groups.length">
      <b-table striped hover :items="groups" @row-clicked="groupClicked" class="table-clickable"/>
    </b-container>
  </div>
</template>

<script>
  import axiosService from '../services/AxiosService'
  export default {
    data: function () {
      return {
        groups: []
      }
    },
    created: function () {
      axiosService.axios.get(`groups`)
        .then(response => {
          this.groups = response.data.map(g => { return { id: g } })
        })
        .catch(e => axiosService.helper.handleError(`Consumer Groups load failed`, e))
    },
    methods: {
      groupClicked: function (group) {
        this.$router.push({ name: 'ConsumerGroup', params: { id: group.id } })
      }
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
