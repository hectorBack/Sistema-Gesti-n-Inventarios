<script setup>
import { ref, onMounted, watch } from "vue";
import axios from "axios";
import { Pie } from "vue-chartjs";
import { Chart as ChartJS, Title, Tooltip, Legend, ArcElement, CategoryScale } from "chart.js";

ChartJS.register(Title, Tooltip, Legend, ArcElement, CategoryScale);

// 1. Estado inicial limpio
const stats = ref({
  totalMateriales: 0,
  stockCritico: 0,
  movimientosHoy: 0,
  totalTrabajadores: 0,
  movimientosRecientes: [],
  stockPorCategoria: []
});

const cargando = ref(true);
const tieneDatosGrafica = ref(false);

const chartData = ref({
  labels: [],
  datasets: [{ data: [], backgroundColor: ["#3b82f6", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6"] }]
});

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { position: "bottom", labels: { color: "#94a3b8", font: { size: 12 } } }
  }
};

// 2. Función de carga
const cargarDashboard = async () => {
  try {
    cargando.value = true;
    const res = await axios.get("http://localhost:6464/api/dashboard/resumen");
    stats.value = res.data;
    
    // Si hay categorías, preparamos la gráfica
    if (res.data.stockPorCategoria && res.data.stockPorCategoria.length > 0) {
      chartData.value = {
        labels: res.data.stockPorCategoria.map(item => item.nombre),
        datasets: [{
          data: res.data.stockPorCategoria.map(item => item.cantidad),
          backgroundColor: ["#3b82f6", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6", "#ec4899"],
          borderWidth: 0
        }]
      };
      tieneDatosGrafica.value = true;
    }
  } catch (error) {
    console.error("Error al conectar con el backend:", error);
  } finally {
    cargando.value = false;
  }
};

onMounted(cargarDashboard);
</script>

<template>
  <div class="space-y-6">
    <h2 class="text-2xl font-bold text-white flex items-center gap-2">
      <span class="text-green-500 text-3xl">📊</span> Panel de Gestión de Inventarios
    </h2>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <div class="bg-[#1e293b] p-6 rounded-xl border border-slate-800 shadow-lg">
        <p class="text-slate-400 text-xs font-bold uppercase">Total Materiales</p>
        <h3 class="text-3xl font-black text-white mt-1">{{ stats.totalMateriales }}</h3>
      </div>
      
      <div class="bg-[#1e293b] p-6 rounded-xl border border-slate-800 shadow-lg border-l-4 border-l-red-500">
        <p class="text-slate-400 text-xs font-bold uppercase">Stock Crítico</p>
        <h3 class="text-3xl font-black text-red-500 mt-1">{{ stats.stockCritico }}</h3>
      </div>

      <div class="bg-[#1e293b] p-6 rounded-xl border border-slate-800 shadow-lg border-l-4 border-l-green-500">
        <p class="text-slate-400 text-xs font-bold uppercase">Movimientos Hoy</p>
        <h3 class="text-3xl font-black text-green-500 mt-1">{{ stats.movimientosHoy }}</h3>
      </div>

      <div class="bg-[#1e293b] p-6 rounded-xl border border-slate-800 shadow-lg">
        <p class="text-slate-400 text-xs font-bold uppercase">Personal Activo</p>
        <h3 class="text-3xl font-black text-indigo-400 mt-1">{{ stats.totalTrabajadores }}</h3>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      
      <div class="lg:col-span-1 bg-[#0f172a] p-6 rounded-xl border border-slate-800">
        <h4 class="text-white font-bold mb-4">Materiales por Categoría</h4>
        <div class="h-64 relative">
          <Pie v-if="tieneDatosGrafica" :data="chartData" :options="chartOptions" />
          <div v-else-if="cargando" class="h-full flex items-center justify-center text-slate-500 italic">
            Cargando gráfica...
          </div>
          <div v-else class="h-full flex items-center justify-center text-slate-600 text-sm">
            Sin categorías registradas
          </div>
        </div>
      </div>

      <div class="lg:col-span-2 bg-[#0f172a] rounded-xl border border-slate-800 overflow-hidden shadow-2xl">
        <div class="p-4 bg-slate-900/50 border-b border-slate-800 text-white font-bold">
          Últimos 5 Movimientos
        </div>
        <div class="overflow-x-auto">
          <table class="w-full text-left">
            <thead class="bg-slate-900/80 text-slate-500 text-[10px] uppercase font-bold tracking-widest">
              <tr>
                <th class="px-6 py-3">Material</th>
                <th class="px-6 py-3 text-center">Tipo</th>
                <th class="px-6 py-3 text-center">Cant.</th>
                <th class="px-6 py-3">Trabajador</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-800 text-sm text-slate-300">
              <tr v-for="(mov, idx) in stats.movimientosRecientes" :key="idx" class="hover:bg-slate-800/40 transition-colors">
                <td class="px-6 py-4 text-white font-medium text-xs">{{ mov.material }}</td>
                <td class="px-6 py-4 text-center">
                  <span :class="mov.tipo === 'INGRESO' ? 'bg-green-900/30 text-green-400' : 'bg-orange-900/30 text-orange-400'" 
                        class="px-2 py-0.5 rounded text-[9px] font-bold">
                    {{ mov.tipo }}
                  </span>
                </td>
                <td class="px-6 py-4 text-center font-mono">{{ mov.cantidad }}</td>
                <td class="px-6 py-4 text-slate-400 text-xs">{{ mov.trabajador }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

    </div>
  </div>
</template>