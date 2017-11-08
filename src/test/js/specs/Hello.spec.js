import Vue from 'vue'
import BootstrapVue from 'bootstrap-vue'
import Hello from '@/components/Hello'

Vue.use(BootstrapVue)

describe('Hello.vue', () => {
  it('should render correct contents', () => {
    const Constructor = Vue.extend(Hello)
    const vm = new Constructor().$mount()
    expect(vm.$el.querySelector('.jumbotron h1').textContent).to.contain('Kafka Brod')
  })
})
