<script setup>
import { ref, onMounted, watch } from "vue";
import axios from "axios";

// --- ESTADOS ---
const materiales = ref([]);
const cargando = ref(true);
const mostrarModal = ref(false);
const busqueda = ref("");
const filtroCategoria = ref("");
const soloCritico = ref(false);

// --- PAGINACIÓN ---
const paginaActual = ref(0);
const totalPaginas = ref(0);
const totalElementos = ref(0);
const tamanoPagina = 10;

// --- NOTIFICACIONES ---
const mensajeToast = ref("");
const mostrarToast = ref(false);
const tipoToast = ref("success");

const dispararToast = (mensaje, tipo = "success") => {
  mensajeToast.value = mensaje;
  tipoToast.value = tipo;
  mostrarToast.value = true;
  setTimeout(() => (mostrarToast.value = false), 3000);
};

// --- FORMULARIO ---
const nuevoMaterial = ref({
  codigo: "",
  nombre: "",
  categoria: "",
  stockInicial: 0,
});

// --- LÓGICA DE API ---
const obtenerInventario = async () => {
  // Solo activamos cargando para la tabla, no para los filtros
  cargando.value = true;
  try {
    const response = await axios.get(`http://localhost:6464/api/materiales`, {
      params: {
        page: paginaActual.value,
        size: tamanoPagina,
        filtro: busqueda.value,
        categoria: filtroCategoria.value,
        critico: soloCritico.value
      },
    });

    materiales.value = response.data.content;
    totalPaginas.value = response.data.totalPages;
    totalElementos.value = response.data.totalElements;
  } catch (error) {
    dispararToast("Error al obtener materiales", "error");
  } finally {
    cargando.value = false;
  }
};

const guardarMaterial = async () => {
  try {
    await axios.post("http://localhost:6464/api/materiales", nuevoMaterial.value);
    mostrarModal.value = false;
    nuevoMaterial.value = { codigo: "", nombre: "", categoria: "", stockInicial: 0 };
    obtenerInventario();
    dispararToast("✅ Material registrado correctamente");
  } catch (error) {
    dispararToast("❌ Error al guardar el material", "error");
  }
};

// --- WATCHERS CON DEBOUNCE (Para evitar perder el foco) ---
let timeout = null;
watch([busqueda, filtroCategoria, soloCritico], () => {
  clearTimeout(timeout);
  timeout = setTimeout(() => {
    paginaActual.value = 0;
    obtenerInventario();
  }, 400); // Espera 400ms tras dejar de escribir
});

watch(paginaActual, () => {
  obtenerInventario();
});

const colorEstado = (estado) => {
  if (!estado) return "bg-slate-800 text-slate-400";
  const e = estado.toUpperCase();
  if (e.includes("BAJO") || e.includes("CRÍTICO"))
    return "bg-red-900/30 text-red-400 border-red-800";
  return "bg-green-900/30 text-green-400 border-green-800";
};

onMounted(obtenerInventario);
</script>

<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h2 class="text-2xl font-bold text-white flex items-center gap-2">
        <span class="text-green-500">⚡</span> Listado de Materiales
      </h2>
      <button @click="mostrarModal = true"
        class="bg-green-600 hover:bg-green-500 text-white px-4 py-2 rounded-lg font-bold transition-transform active:scale-95 shadow-lg">
        + Nuevo Material
      </button>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-4 gap-4 bg-[#0f172a] p-4 rounded-xl border border-slate-800">
      <div class="relative">
        <span class="absolute inset-y-0 left-0 flex items-center pl-3">🔍</span>
        <input v-model="busqueda" type="text" placeholder="Código o nombre..."
          class="w-full bg-[#1e293b] border border-slate-700 rounded-xl py-2 pl-10 pr-4 text-white outline-none focus:ring-2 focus:ring-green-500 transition-all" />
      </div>

      <select v-model="filtroCategoria"
        class="bg-[#1e293b] border border-slate-700 rounded-xl py-2 px-4 text-white outline-none focus:ring-2 focus:ring-green-500">
        <option value="">Todas las Categorías</option>
        <option value="Conductores">Conductores</option>
        <option value="Herrajes">Herrajes</option>
        <option value="Transformadores">Transformadores</option>
        <option value="Protección">Protección</option>
      </select>

      <button @click="soloCritico = !soloCritico" 
        :class="soloCritico ? 'bg-red-600 border-red-500 text-white' : 'bg-[#1e293b] border-slate-700 text-slate-400'"
        class="px-4 py-2 rounded-xl font-bold border transition-all flex items-center justify-center gap-2">
        <span>⚠️</span> {{ soloCritico ? 'Mostrando Críticos' : 'Ver Stock Crítico' }}
      </button>

      <button @click="busqueda = ''; filtroCategoria = ''; soloCritico = false" 
        class="text-slate-400 hover:text-white transition-colors text-sm font-medium flex items-center justify-center gap-2 border border-dashed border-slate-700 rounded-lg hover:border-slate-500">
        🗑️ Limpiar filtros
      </button>
    </div>

    <div class="relative min-h-[400px]">
      <div v-if="cargando" class="absolute inset-0 z-10 flex justify-center items-center bg-[#0f172a]/50 backdrop-blur-[2px] rounded-xl">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-green-500"></div>
      </div>

      <div class="overflow-hidden rounded-xl border border-slate-800 bg-[#0f172a]">
        <table class="w-full text-left">
          <thead class="bg-slate-900/50 text-slate-400 text-xs uppercase tracking-widest">
            <tr>
              <th class="px-6 py-4">Código</th>
              <th class="px-6 py-4">Descripción</th>
              <th class="px-6 py-4 text-center">Entradas</th>
              <th class="px-6 py-4 text-center">Salidas</th>
              <th class="px-6 py-4 text-center">Stock</th>
              <th class="px-6 py-4">Estado</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-800 text-slate-300">
            <tr v-for="m in materiales" :key="m.codigo" class="hover:bg-slate-800/40 transition-colors">
              <td class="px-6 py-4 font-mono text-green-400 text-sm">{{ m.codigo }}</td>
              <td class="px-6 py-4 font-medium text-slate-100">{{ m.nombre }}</td>
              <td class="px-6 py-4 text-center text-blue-400">{{ m.ingresos }}</td>
              <td class="px-6 py-4 text-center text-orange-400">{{ m.salidas }}</td>
              <td class="px-6 py-4 text-center font-bold text-lg" :class="m.stock < 5 ? 'text-red-400' : 'text-white'">
                {{ m.stock }}
              </td>
              <td class="px-6 py-4">
                <span :class="colorEstado(m.estadoStock)" class="px-3 py-1 rounded-full text-[10px] font-bold border uppercase">
                  {{ m.estadoStock }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
        
        <div class="flex justify-between items-center p-4 border-t border-slate-800 bg-slate-900/20">
          <span class="text-slate-400 text-xs">
            Mostrando {{ materiales.length }} de {{ totalElementos }} materiales
          </span>
          <div class="flex items-center gap-2">
            <button @click="paginaActual--" :disabled="paginaActual === 0"
              class="px-4 py-1.5 rounded-lg bg-slate-800 text-white disabled:opacity-20 hover:bg-slate-700 transition-colors text-sm">
              Anterior
            </button>
            <span class="px-4 py-1.5 bg-green-600/20 text-green-400 border border-green-600/50 rounded-lg font-bold text-xs">
              {{ paginaActual + 1 }} / {{ totalPaginas }}
            </span>
            <button @click="paginaActual++" :disabled="paginaActual + 1 >= totalPaginas"
              class="px-4 py-1.5 rounded-lg bg-slate-800 text-white disabled:opacity-20 hover:bg-slate-700 transition-colors text-sm">
              Siguiente
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="mostrarModal" class="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
      <div class="bg-[#1e293b] w-full max-w-md rounded-2xl border border-slate-700 shadow-2xl">
        <div class="p-6 border-b border-slate-700 flex justify-between items-center text-white">
          <h3 class="text-xl font-bold">Nuevo Material</h3>
          <button @click="mostrarModal = false" class="text-slate-400 hover:text-white text-2xl">&times;</button>
        </div>
        <form @submit.prevent="guardarMaterial" class="p-6 space-y-4">
          <div>
            <label class="block text-xs font-bold text-slate-400 uppercase mb-1">Código</label>
            <input v-model="nuevoMaterial.codigo" type="text" required class="w-full bg-[#0f172a] border border-slate-600 rounded-lg p-2.5 text-white focus:ring-2 focus:ring-green-500 outline-none" />
          </div>
          <div>
            <label class="block text-xs font-bold text-slate-400 uppercase mb-1">Nombre</label>
            <input v-model="nuevoMaterial.nombre" type="text" required class="w-full bg-[#0f172a] border border-slate-600 rounded-lg p-2.5 text-white focus:ring-2 focus:ring-green-500 outline-none" />
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-xs font-bold text-slate-400 uppercase mb-1">Categoría</label>
              <select v-model="nuevoMaterial.categoria" required class="w-full bg-[#0f172a] border border-slate-600 rounded-lg p-2.5 text-white outline-none">
                <option value="Conductores">Conductores</option>
                <option value="Herrajes">Herrajes</option>
                <option value="Transformadores">Transformadores</option>
                <option value="Protección">Protección</option>
              </select>
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-400 uppercase mb-1">Stock Inicial</label>
              <input v-model.number="nuevoMaterial.stockInicial" type="number" min="0" required class="w-full bg-[#0f172a] border border-slate-600 rounded-lg p-2.5 text-white outline-none" />
            </div>
          </div>
          <div class="pt-4 flex gap-3 font-bold">
            <button type="button" @click="mostrarModal = false" class="flex-1 bg-slate-700 text-white py-2.5 rounded-lg">Cancelar</button>
            <button type="submit" class="flex-1 bg-green-600 text-white py-2.5 rounded-lg shadow-lg">Guardar</button>
          </div>
        </form>
      </div>
    </div>

    <Transition enter-active-class="transform ease-out duration-300 transition" enter-from-class="translate-y-2 opacity-0" enter-to-class="translate-y-0 opacity-100" leave-active-class="transition ease-in duration-100" leave-from-class="opacity-100" leave-to-class="opacity-0">
      <div v-if="mostrarToast" class="fixed bottom-5 right-5 z-[110] flex items-center p-4 w-72 rounded-lg shadow-2xl border" :class="tipoToast === 'success' ? 'bg-[#064e3b] text-green-100 border-green-700' : 'bg-red-900 text-red-100 border-red-700'">
        <span class="mr-3">{{ tipoToast === 'success' ? '✔️' : '⚠️' }}</span>
        <div class="text-xs font-bold uppercase">{{ mensajeToast }}</div>
      </div>
    </Transition>
  </div>
</template>
