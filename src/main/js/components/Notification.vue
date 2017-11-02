<template>
  <div v-if="notification">
    <b-alert :variant="notification.variant"
             dismissible
             :show="showNotification"
             @dismissed="notificationDismissed">
      {{ notification.message }}
    </b-alert>
  </div>
</template>
<script>
  import notificationService from '../services/NotificationService'
  export default {
    data: function () {
      return {
        notification: null,
        showNotification: false
      }
    },
    created: function () {
      notificationService.subscribeNotification(function (notification) {
        if (notification) {
          this.notification = notification
          this.showNotification = (notification.variant !== 'clear')
        }
      }.bind(this))
    },
    methods: {
      notificationDismissed: function () {
        this.showNotification = false
      }
    }
  }
</script>
