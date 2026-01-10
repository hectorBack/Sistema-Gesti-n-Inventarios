<script setup>
import { ref, onMounted, watch } from "vue";
import axios from "axios";

// --- ESTADOS ---
const movimientos = ref([]);
const materiales = ref([]);
const trabajadores = ref([]);
const cargando = ref(true);

// --- FILTROS ---
const filtroTexto = ref("");
const filtroTipo = ref("");
const filtroFecha = ref(""); // Nueva variable para fecha
const paginaActual = ref(0);
const totalPaginas = ref(0);
const totalElementos = ref(0);

// --- FORMULARIO ---
const mostrarModal = ref(false);
const nuevoMovimiento = ref({
  materialCodigo: "",
  trabajador: "",
  cantidad: 1,
  tipo: "SALIDA"
});

// --- NOTIFICACIONES ---
const mostrarToast = ref(false);
const mensajeToast = ref("");
const tipoToast = ref("success");

const dispararToast = (msj, tipo = "success") => {
  mensajeToast.value = msj;
  tipoToast.value = tipo;
  mostrarToast.value = true;
  setTimeout(() => (mostrarToast.value = false), 3000);
};

// --- LOGICA API ---
const obtenerMovimientos = async () => {
  cargando.value = true;
  try {
    const res = await axios.get("http://localhost:6464/api/movimientos", {
      params: { 
        filtro: filtroTexto.value, 
        tipo: filtroTipo.value, 
        fecha: filtroFecha.value, // Parámetro enviado al Backend
        page: paginaActual.value 
      }
    });
    movimientos.value = res.data.content;
    totalPaginas.value = res.data.totalPages;
    totalElementos.value = res.data.totalElements;
  } catch (error) {
    dispararToast("Error al cargar historial", "error");
  } finally {
    cargando.value = false;
  }
};

const cargarCatalogos = async () => {
  try {
    const [resMat, resTrab] = await Promise.all([
      axios.get("http://localhost:6464/api/materiales?size=100"),
      axios.get("http://localhost:6464/api/trabajadores")
    ]);
    materiales.value = resMat.data.content;
    trabajadores.value = resTrab.data;
  } catch (e) {
    console.error("Error cargando catálogos");
  }
};

const registrarMovimiento = async () => {
  try {
    await axios.post("http://localhost:6464/api/movimientos", nuevoMovimiento.value);
    dispararToast("✅ Movimiento registrado con éxito");
    mostrarModal.value = false;
    obtenerMovimientos();
    nuevoMovimiento.value = { materialCodigo: "", trabajador: "", cantidad: 1, tipo: "SALIDA" };
  } catch (error) {
    const errorMsg = error.response?.data?.mensaje || "Error al registrar";
    dispararToast(errorMsg, "error");
  }
};

const eliminarMovimiento = async (id) => {
  if (!confirm("¿Eliminar este movimiento? El stock se revertirá automáticamente.")) return;
  try {
    await axios.delete(`http://localhost:6464/api/movimientos/${id}`);
    dispararToast("Movimiento eliminado");
    obtenerMovimientos();
  } catch (e) {
    dispararToast("No se pudo eliminar", "error");
  }
};

// --- WATCHERS CON RETRASO (DEBOUNCE) ---
let timeout = null;
watch([filtroTexto, filtroTipo, filtroFecha], () => {
  clearTimeout(timeout);
  timeout = setTimeout(() => {
    paginaActual.value = 0;
    obtenerMovimientos();
  }, 400);
});

watch(paginaActual, obtenerMovimientos);

onMounted(() => {
  obtenerMovimientos();
  cargarCatalogos();
});
</script>

<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h2 class="text-2xl font-bold text-white flex items-center gap-2">
        <span class="text-blue-500">📦</span> Historial de Movimientos
      </h2>
      <button @click="mostrarModal = true" 
        class="bg-blue-600 hover:bg-blue-500 text-white px-5 py-2 rounded-lg font-bold shadow-lg transition-all active:scale-95">
        + Registrar Movimiento
      </button>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-4 gap-4 bg-[#0f172a] p-4 rounded-xl border border-slate-800">
      <div class="relative">
        <input v-model="filtroTexto" type="text" placeholder="Buscar material o trabajador..." 
          class="w-full bg-[#1e293b] border border-slate-700 rounded-lg pl-4 pr-4 py-2 text-white outline-none focus:ring-2 focus:ring-blue-500" />
      </div>
      
      <select v-model="filtroTipo" class="bg-[#1e293b] border border-slate-700 rounded-lg px-4 py-2 text-white outline-none focus:ring-2 focus:ring-blue-500">
        <option value="">Todos los tipos</option>
        <option value="INGRESO">Entradas (Ingresos)</option>
        <option value="SALIDA">Salidas</option>
      </select>

      <div class="relative">
        <input v-model="filtroFecha" type="date" 
          class="w-full bg-[#1e293b] border border-slate-700 rounded-lg px-4 py-2 text-white outline-none focus:ring-2 focus:ring-blue-500 [color-scheme:dark]" />
      </div>
      
      <button @click="filtroTexto = ''; filtroTipo = ''; filtroFecha = ''" 
        class="text-slate-400 hover:text-white transition-colors text-sm font-medium flex items-center justify-center gap-2 border border-dashed border-slate-700 rounded-lg hover:border-slate-500">
        🗑️ Limpiar filtros
      </button>
    </div>

    <div class="relative min-h-[400px]">
      <div v-if="cargando" class="absolute inset-0 z-10 flex justify-center items-center bg-[#0f172a]/50 backdrop-blur-[2px] rounded-xl">
        <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-500"></div>
      </div>

      <div class="bg-[#0f172a] rounded-xl border border-slate-800 overflow-hidden">
        <table class="w-full text-left">
          <thead class="bg-slate-900/50 text-slate-400 text-xs uppercase tracking-wider">
            <tr>
              <th class="px-6 py-4">Fecha</th>
              <th class="px-6 py-4">Tipo</th>
              <th class="px-6 py-4">Material</th>
              <th class="px-6 py-4">Cantidad</th>
              <th class="px-6 py-4">Trabajador</th>
              <th class="px-6 py-4 text-center">Acciones</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-800 text-slate-300">
            <tr v-for="mov in movimientos" :key="mov.id" class="hover:bg-slate-800/40 transition-colors">
              <td class="px-6 py-4 text-sm font-mono">{{ mov.fecha }}</td>
              <td class="px-6 py-4">
                <span :class="mov.tipo === 'INGRESO' ? 'bg-green-900/30 text-green-400 border-green-800' : 'bg-orange-900/30 text-orange-400 border-orange-800'" 
                  class="px-2 py-1 rounded border font-bold text-[10px] uppercase">
                  {{ mov.tipo }}
                </span>
              </td>
              <td class="px-6 py-4">
                <div class="text-white font-medium">{{ mov.materialNombre }}</div>
                <div class="text-[10px] text-slate-500 font-mono">{{ mov.materialCodigo }}</div>
              </td>
              <td class="px-6 py-4 font-bold text-lg">{{ mov.cantidad }}</td>
              <td class="px-6 py-4 text-slate-400">{{ mov.trabajador }}</td>
              <td class="px-6 py-4 text-center">
                <button @click="eliminarMovimiento(mov.id)" class="text-slate-500 hover:text-red-500 transition-colors p-2">
                  🗑️
                </button>
              </td>
            </tr>
            <tr v-if="movimientos.length === 0 && !cargando">
              <td colspan="6" class="px-6 py-10 text-center text-slate-500 italic">No se encontraron movimientos con estos filtros.</td>
            </tr>
          </tbody>
        </table>
        
        <div class="flex justify-between items-center bg-slate-900/30 p-4 border-t border-slate-800">
          <span class="text-slate-400 text-xs font-medium">
            Total: <span class="text-white">{{ totalElementos }}</span> registros
          </span>
          <div class="flex items-center gap-2">
            <button @click="paginaActual--" :disabled="paginaActual === 0"
              class="px-3 py-1.5 rounded-lg bg-slate-800 text-white disabled:opacity-20 hover:bg-slate-700 transition-all text-xs font-bold">
              Anterior
            </button>
            <span class="px-3 py-1.5 bg-blue-600/10 text-blue-400 border border-blue-600/30 rounded-lg font-bold text-xs">
              {{ paginaActual + 1 }} / {{ totalPaginas }}
            </span>
            <button @click="paginaActual++" :disabled="paginaActual + 1 >= totalPaginas"
              class="px-3 py-1.5 rounded-lg bg-slate-800 text-white disabled:opacity-20 hover:bg-slate-700 transition-all text-xs font-bold">
              Siguiente
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="mostrarModal" class="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div class="bg-[#1e293b] w-full max-w-md rounded-2xl border border-slate-700 shadow-2xl overflow-hidden">
        <div class="bg-slate-800/50 p-4 border-b border-slate-700 flex justify-between items-center">
          <h3 class="text-lg font-bold text-white">Registrar Movimiento</h3>
          <button @click="mostrarModal = false" class="text-slate-400 hover:text-white text-2xl">&times;</button>
        </div>
        
        <div class="p-6 space-y-4">
          <div>
            <label class="text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-1 block">Material</label>
            <select v-model="nuevoMovimiento.materialCodigo" class="w-full bg-[#0f172a] border border-slate-600 rounded-lg p-2.5 text-white focus:ring-2 focus:ring-blue-500 outline-none">
              <option value="">Seleccione material...</option>
              <option v-for="m in materiales" :key="m.codigo" :value="m.codigo">{{ m.nombre }} ({{ m.codigo }})</option>
            </select>
          </div>

          <div>
            <label class="text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-1 block">Trabajador</label>
            <select v-model="nuevoMovimiento.trabajador" class="w-full bg-[#0f172a] border border-slate-600 rounded-lg p-2.5 text-white focus:ring-2 focus:ring-blue-500 outline-none">
              <option value="">Seleccione trabajador...</option>
              <option v-for="t in trabajadores" :key="t.id" :value="t.nombreCompleto">{{ t.nombreCompleto }}</option>
            </select>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-1 block">Cantidad</label>
              <input v-model.number="nuevoMovimiento.cantidad" type="number" min="1" class="w-full bg-[#0f172a] border border-slate-600 rounded-lg p-2.5 text-white focus:ring-2 focus:ring-blue-500 outline-none" />
            </div>
            <div>
              <label class="text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-1 block">Tipo</label>
              <select v-model="nuevoMovimiento.tipo" class="w-full bg-[#0f172a] border border-slate-600 rounded-lg p-2.5 text-white focus:ring-2 focus:ring-blue-500 outline-none">
                <option value="SALIDA">Salida 📤</option>
                <option value="INGRESO">Ingreso 📥</option>
              </select>
            </div>
          </div>
        </div>

        <div class="p-6 bg-slate-800/30 flex gap-3">
          <button @click="mostrarModal = false" class="flex-1 bg-slate-700 hover:bg-slate-600 text-white py-2.5 rounded-lg font-bold transition-colors">Cancelar</button>
          <button @click="registrarMovimiento" class="flex-1 bg-blue-600 hover:bg-blue-500 text-white py-2.5 rounded-lg font-bold shadow-lg transition-transform active:scale-95">Confirmar</button>
        </div>
      </div>
    </div>

    <Transition name="toast">
      <div v-if="mostrarToast" class="fixed bottom-5 right-5 z-[60] flex items-center p-4 min-w-[280px] rounded-xl shadow-2xl border" 
        :class="tipoToast === 'success' ? 'bg-[#064e3b] text-green-100 border-green-700' : 'bg-red-900 text-red-100 border-red-700'">
        <span class="mr-3 text-lg">{{ tipoToast === 'success' ? '✅' : '⚠️' }}</span>
        <div class="text-xs font-bold uppercase tracking-tight">{{ mensajeToast }}</div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.toast-enter-active, .toast-leave-active { transition: all 0.4s ease; }
.toast-enter-from { transform: translateX(100px); opacity: 0; }
.toast-leave-to { opacity: 0; transform: scale(0.9); }
</style>