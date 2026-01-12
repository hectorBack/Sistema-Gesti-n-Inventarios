<template>
  <div class="h-screen w-64 bg-[#064e3b] text-slate-300 flex flex-col fixed shadow-2xl border-r border-green-900">
    <div class="p-6 text-2xl font-bold border-b border-green-900 flex items-center space-x-2">
      <span class="text-green-400">⚡</span>
      <span> <span class="text-white">Sistema de Gestion de Inventarios</span></span>
    </div>
    
    <nav class="flex-1 px-4 py-6 space-y-2">
      <router-link to="/dashboard" 
        class="flex items-center space-x-3 p-3 rounded-lg hover:bg-green-900/50 transition-all font-medium"
        active-class="bg-green-600 text-white shadow-lg shadow-green-900/20">
        <span class="text-xl">📊</span>
        <span>Dashboard</span>
      </router-link>

      <router-link to="/inventario" 
        class="flex items-center space-x-3 p-3 rounded-lg hover:bg-green-900/50 transition-all font-medium"
        active-class="bg-green-600 text-white shadow-lg shadow-green-900/20">
        <span class="text-xl">📋</span>
        <span>Inventario</span>
      </router-link>

      <router-link to="/movimientos" 
        class="flex items-center space-x-3 p-3 rounded-lg hover:bg-green-900/50 transition-all font-medium"
        active-class="bg-green-600 text-white shadow-lg shadow-green-900/20">
        <span class="text-xl">🔄</span>
        <span>Movimientos</span>
      </router-link>

      <router-link to="/trabajadores" 
        class="flex items-center space-x-3 p-3 rounded-lg hover:bg-green-900/50 transition-all font-medium"
        active-class="bg-green-600 text-white shadow-lg shadow-green-900/20">
        <span class="text-xl">👷</span>
        <span>Trabajadores</span>
      </router-link>
    </nav>

    <div class="p-4 border-t border-green-900">
       <div class="flex items-center space-x-3 p-2 text-xs text-green-300">
          <div class="w-2 h-2 rounded-full bg-green-400 animate-pulse"></div>
          <span>Sistema Conectado</span>
       </div>
    </div>
  </div>
</template>