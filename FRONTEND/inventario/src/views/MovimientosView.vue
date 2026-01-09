<script setup>
import { ref, onMounted, watch } from "vue";
import axios from "axios";

// Listas y estados
const movimientos = ref([]);
const materiales = ref([]);    // Para el select del formulario
const trabajadores = ref([]);  // Para el select del formulario
const cargando = ref(true);

// Filtros
const filtroTexto = ref("");
const filtroTipo = ref("");
const paginaActual = ref(0);
const totalPaginas = ref(0);
const totalElementos = ref(0);

// Formulario de nuevo movimiento
const mostrarModal = ref(false);
const nuevoMovimiento = ref({
  materialCodigo: "",
  trabajador: "",
  cantidad: 1,
  tipo: "SALIDA" // Por defecto salida, que es lo más común
});

// Mensajes
const mostrarToast = ref(false);
const mensajeToast = ref("");
const tipoToast = ref("success");

const dispararToast = (msj, tipo = "success") => {
  mensajeToast.value = msj;
  tipoToast.value = tipo;
  mostrarToast.value = true;
  setTimeout(() => (mostrarToast.value = false), 3000);
};

// --- PETICIONES API ---

const obtenerMovimientos = async () => {
  cargando.value = true;
  try {
    const res = await axios.get("http://localhost:6464/api/movimientos", {
      params: { 
        filtro: filtroTexto.value, 
        tipo: filtroTipo.value, 
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

// Cargamos catálogos para el formulario
const cargarCatalogos = async () => {
  try {
    const [resMat, resTrab] = await Promise.all([
      axios.get("http://localhost:6464/api/materiales?size=100"), // Traemos suficientes para el select
      axios.get("http://localhost:6464/api/trabajadores") // Asumiendo que existe esta ruta
    ]);
    materiales.value = resMat.data.content;
    trabajadores.value = resTrab.data; // Ajustar según tu estructura de Trabajadores
  } catch (e) {
    console.error("Error cargando catálogos");
  }
};

const registrarMovimiento = async () => {
  try {
    await axios.post("http://localhost:6464/api/movimientos", nuevoMovimiento.value);
    dispararToast("✅ Movimiento registrado y stock actualizado");
    mostrarModal.value = false;
    obtenerMovimientos();
    // Limpiar form
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

// Watches para filtros
watch([filtroTexto, filtroTipo], () => {
  paginaActual.value = 0;
  obtenerMovimientos();
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

    <div class="grid grid-cols-1 md:grid-cols-3 gap-4 bg-[#0f172a] p-4 rounded-xl border border-slate-800">
      <input v-model="filtroTexto" type="text" placeholder="Buscar material o trabajador..." 
        class="bg-[#1e293b] border border-slate-700 rounded-lg px-4 py-2 text-white outline-none focus:ring-2 focus:ring-blue-500" />
      
      <select v-model="filtroTipo" class="bg-[#1e293b] border border-slate-700 rounded-lg px-4 py-2 text-white outline-none">
        <option value="">Todos los tipos</option>
        <option value="INGRESO">Entradas (Ingresos)</option>
        <option value="SALIDA">Salidas</option>
      </select>
      
      <button @click="filtroTexto = ''; filtroTipo = ''" class="text-slate-400 hover:text-white transition-colors text-sm text-right">
        Limpiar filtros
      </button>
    </div>

    <div class="bg-[#0f172a] rounded-xl border border-slate-800 overflow-hidden">
      <table class="w-full text-left">
        <thead class="bg-slate-900/50 text-slate-400 text-xs uppercase">
          <tr>
            <th class="px-6 py-4">Fecha</th>
            <th class="px-6 py-4">Tipo</th>
            <th class="px-6 py-4">Material</th>
            <th class="px-6 py-4">Cantidad</th>
            <th class="px-6 py-4">Trabajador</th>
            <th class="px-6 py-4">Acciones</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-800 text-slate-300">
          <tr v-for="mov in movimientos" :key="mov.id" class="hover:bg-slate-800/40 transition-colors">
            <td class="px-6 py-4 text-sm">{{ mov.fecha }}</td>
            <td class="px-6 py-4">
              <span :class="mov.tipo === 'INGRESO' ? 'text-green-400' : 'text-orange-400'" class="font-bold text-xs">
                {{ mov.tipo }}
              </span>
            </td>
            <td class="px-6 py-4">
              <div class="text-white font-medium">{{ mov.materialNombre }}</div>
              <div class="text-[10px] text-slate-500 font-mono">{{ mov.materialCodigo }}</div>
            </td>
            <td class="px-6 py-4 font-bold">{{ mov.cantidad }}</td>
            <td class="px-6 py-4 text-slate-400">{{ mov.trabajador }}</td>
            <td class="px-6 py-4">
              <button @click="eliminarMovimiento(mov.id)" class="text-red-500 hover:text-red-400 p-2">🗑️</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="flex justify-between items-center bg-[#0f172a] p-4 rounded-xl border border-slate-800">
        <span class="text-slate-400 text-sm">
          Mostrando {{ movimientos.length }} de {{ totalElementos }} movimientos
        </span>

        <div class="flex items-center gap-2">
          <button @click="paginaActual--" :disabled="paginaActual === 0"
            class="px-4 py-2 rounded-lg bg-slate-800 text-white disabled:opacity-30 disabled:cursor-not-allowed hover:bg-slate-700 transition-colors">
            Anterior
          </button>

          <span
            class="px-4 py-2 bg-green-600/20 text-green-400 border border-green-600/50 rounded-lg font-bold text-sm">
            Página {{ paginaActual + 1 }} de {{ totalPaginas }}
          </span>

          <button @click="paginaActual++" :disabled="paginaActual + 1 >= totalPaginas"
            class="px-4 py-2 rounded-lg bg-slate-800 text-white disabled:opacity-30 disabled:cursor-not-allowed hover:bg-slate-700 transition-colors">
            Siguiente
          </button>
        </div>
      </div>
    </div>

    <div v-if="mostrarModal" class="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div class="bg-[#1e293b] w-full max-w-md rounded-2xl border border-slate-700 p-6 space-y-4">
        <h3 class="text-xl font-bold text-white">Nuevo Movimiento</h3>
        
        <div class="space-y-3">
          <div>
            <label class="text-xs text-slate-400 uppercase">Material</label>
            <select v-model="nuevoMovimiento.materialCodigo" class="w-full bg-[#0f172a] border border-slate-600 rounded-lg p-2.5 text-white">
              <option value="">Seleccione material...</option>
              <option v-for="m in materiales" :key="m.codigo" :value="m.codigo">{{ m.nombre }} ({{ m.codigo }})</option>
            </select>
          </div>

          <div>
            <label class="text-xs text-slate-400 uppercase">Trabajador</label>
            <select v-model="nuevoMovimiento.trabajador" class="w-full bg-[#0f172a] border border-slate-600 rounded-lg p-2.5 text-white">
              <option value="">Seleccione trabajador...</option>
              <option v-for="t in trabajadores" :key="t.id" :value="t.nombreCompleto">{{ t.nombreCompleto }}</option>
            </select>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="text-xs text-slate-400 uppercase">Cantidad</label>
              <input v-model.number="nuevoMovimiento.cantidad" type="number" min="1" class="w-full bg-[#0f172a] border border-slate-600 rounded-lg p-2.5 text-white" />
            </div>
            <div>
              <label class="text-xs text-slate-400 uppercase">Tipo</label>
              <select v-model="nuevoMovimiento.tipo" class="w-full bg-[#0f172a] border border-slate-600 rounded-lg p-2.5 text-white">
                <option value="SALIDA">Salida</option>
                <option value="INGRESO">Ingreso</option>
              </select>
            </div>
          </div>
        </div>

        <div class="flex gap-3 pt-4 font-bold">
          <button @click="mostrarModal = false" class="flex-1 bg-slate-700 text-white py-2.5 rounded-lg">Cancelar</button>
          <button @click="registrarMovimiento" class="flex-1 bg-blue-600 text-white py-2.5 rounded-lg">Registrar</button>
        </div>
      </div>
    </div>
</template>