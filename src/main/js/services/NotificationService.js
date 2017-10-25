import Vue from 'vue'

class NotificationService {
  constructor () {
    this.bus = new Vue()
  }
  login () {
    this.bus.$emit('login', {})
  }
  notify (variant, message) {
    this.bus.$emit('notification', {variant: variant, message: message})
  }
  notifyError (message) {
    this.notify('danger', message)
  }
  subscribeNotification (listener) {
    this.bus.$on('notification', listener)
  }
  subscribeLogin (listener) {
    this.bus.$on('login', listener)
  }
}

let notificationService = new NotificationService()

export default notificationService
