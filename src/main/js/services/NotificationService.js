import Vue from 'vue'

class NotificationService {
  constructor () {
    this.bus = new Vue()
  }
  notify (variant, message) {
    this.bus.$emit('notification', {variant: variant, message: message})
  }
  notifyError (message) {
    this.notify('danger', message)
  }
  subscribe (listener) {
    this.bus.$on('notification', listener)
  }
}

let notificationService = new NotificationService()

export default notificationService
