<script setup>
import Sidebar from './components/Sidebar.vue'
</script>

<template>
  <div class="flex bg-[#1e293b] min-h-screen text-slate-200"> <Sidebar />
    <main class="flex-1 ml-64 p-8">
      <div class="bg-[#0f172a] rounded-xl shadow-2xl p-6 border border-slate-700">
        <router-view></router-view>
      </div>
    </main>
  </div>
</template>