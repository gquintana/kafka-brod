<template>
  <div>
    <h2>Consumer Groups</h2>
    <b-container v-if="groups && groups.length">
      <b-table striped hover :items="groups" @row-clicked="groupClicked" class="table-clickable"/>
    </b-container>
  </div>
</template>

<script>
  import axios from '../services/AxiosService'
  import notificationService from '../services/NotificationService'
  export default {
    data: function () {
      return {
        groups: []
      }
    },
    created: function () {
      axios.get(`groups`)
        .then(response => {
          this.groups = response.data.map(g => { return { id: g } })
        })
        .catch(e => notificationService.notifyError(`Consumer Groups load failed: ${e.message}`))
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
