import { createRouter, createWebHistory } from 'vue-router'
// Importaremos los componentes que crearemos después
import InventarioView from '../views/InventarioView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/inventario' },
    { path: '/inventario', component: InventarioView },
    { path: '/dashboard', component: () => import('../views/DashboardView.vue') },
    { path: '/movimientos', component: () => import('../views/MovimientosView.vue') },
    { path: '/trabajadores', component: () => import('../views/TrabajadoresView.vue') }
  ]
})

export default router