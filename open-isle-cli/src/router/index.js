import { createRouter, createWebHistory } from 'vue-router'
import HomePageView from '../views/HomePageView.vue'
import MessagePageView from '../views/MessagePageView.vue'
import AboutPageView from '../views/AboutPageView.vue'
import PostPageView from '../views/PostPageView.vue'
import LoginPageView from '../views/LoginPageView.vue'
import SignupPageView from '../views/SignupPageView.vue'
import NewPostPageView from '../views/NewPostPageView.vue'
import SettingsPageView from '../views/SettingsPageView.vue'
import ProfileView from '../views/ProfileView.vue'
import NotFoundPageView from '../views/NotFoundPageView.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomePageView
  },
  {
    path: '/message',
    name: 'message',
    component: MessagePageView
  },
  {
    path: '/about',
    name: 'about',
    component: AboutPageView
  },
  {
    path: '/new-post',
    name: 'new-post',
    component: NewPostPageView
  },
  {
    path: '/posts/:id',
    name: 'post',
    component: PostPageView
  },
  {
    path: '/login',
    name: 'login',
    component: LoginPageView
  },
  {
    path: '/signup',
    name: 'signup',
    component: SignupPageView
  },
  {
    path: '/settings',
    name: 'settings',
    component: SettingsPageView
  },
  {
    path: '/users/:id',
    name: 'users',
    component: ProfileView
  },
  {
    path: '/404',
    name: 'not-found',
    component: NotFoundPageView
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
