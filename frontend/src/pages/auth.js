import { api } from '../api/client'

export async function logout() {
  await api.post('/auth/logout')
}

