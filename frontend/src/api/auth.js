import { api } from './client'

export async function login(username, password) {
  return api.post('/auth/login', { username, password })
}

export async function logout() {
  return api.post('/auth/logout')
}

