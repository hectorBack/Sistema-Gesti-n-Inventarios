<script setup>
import { ref, onMounted, computed } from "vue";
import axios from "axios";

// --- ESTADOS ---
const trabajadores = ref([]);
const cargando = ref(true);
const buscar = ref("");

// --- FORMULARIO Y MODAL ---
const mostrarModal = ref(false);
const editando = ref(false);
const trabajadorForm = ref({
  id: null,
  nombreCompleto: ""
});

// --- NOTIFICACIONES ---
const toast = ref({ mostrar: false, msj: "", tipo: "success" });
const dispararToast = (msj, tipo = "success") => {
  toast.value = { mostrar: true, msj, tipo };
  setTimeout(() => (toast.value.mostrar = false), 3000);
};

// --- ACCIONES API ---
const obtenerTrabajadores = async () => {
  cargando.value = true;
  try {
    const res = await axios.get("http://localhost:6464/api/trabajadores");
    trabajadores.value = res.data;
  } catch (error) {
    dispararToast("Error al cargar trabajadores", "error");
  } finally {
    cargando.value = false;
  }
};

const abrirModalNuevo = () => {
  editando.value = false;
  trabajadorForm.value = { id: null, nombreCompleto: "" };
  mostrarModal.value = true;
};

const abrirModalEditar = (t) => {
  editando.value = true;
  trabajadorForm.value = { ...t };
  mostrarModal.value = true;
};

const guardarTrabajador = async () => {
  if (!trabajadorForm.value.nombreCompleto.trim()) return;

  try {
    if (editando.value) {
      await axios.put(`http://localhost:6464/api/trabajadores/${trabajadorForm.value.id}`, trabajadorForm.value);
      dispararToast("Trabajador actualizado con éxito");
    } else {
      await axios.post("http://localhost:6464/api/trabajadores", trabajadorForm.value);
      dispararToast("Trabajador registrado con éxito");
    }
    mostrarModal.value = false;
    obtenerTrabajadores();
  } catch (error) {
    dispararToast("Error al procesar la solicitud", "error");
  }
};

const eliminarTrabajador = async (id) => {
  if (!confirm("¿Estás seguro de eliminar este trabajador? Si tiene movimientos registrados, la operación fallará.")) return;
  
  try {
    await axios.delete(`http://localhost:6464/api/trabajadores/${id}`);
    dispararToast("Trabajador eliminado");
    obtenerTrabajadores();
  } catch (error) {
    dispararToast("No se puede eliminar (posibles movimientos asociados)", "error");
  }
};

// --- FILTRO LOCAL (Súper rápido) ---
const trabajadoresFiltrados = computed(() => {
  return trabajadores.value.filter(t => 
    t.nombreCompleto.toLowerCase().includes(buscar.value.toLowerCase())
  );
});

onMounted(obtenerTrabajadores);
</script>

<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h2 class="text-2xl font-bold text-white flex items-center gap-2">
        <span class="text-indigo-500">👥</span> Gestión de Trabajadores
      </h2>
      <button @click="abrirModalNuevo"
        class="bg-indigo-600 hover:bg-indigo-500 text-white px-5 py-2 rounded-lg font-bold shadow-lg transition-all active:scale-95">
        + Nuevo Trabajador
      </button>
    </div>

    <div class="bg-[#0f172a] p-4 rounded-xl border border-slate-800">
      <div class="relative max-w-md">
        <span class="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-500">🔍</span>
        <input v-model="buscar" type="text" placeholder="Buscar trabajador por nombre..."
          class="w-full bg-[#1e293b] border border-slate-700 rounded-lg py-2 pl-10 pr-4 text-white outline-none focus:ring-2 focus:ring-indigo-500 transition-all" />
      </div>
    </div>

    <div class="relative overflow-hidden rounded-xl border border-slate-800 bg-[#0f172a]">
      <div v-if="cargando" class="p-10 flex justify-center">
        <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-500"></div>
      </div>

      <table v-else class="w-full text-left">
        <thead class="bg-slate-900/50 text-slate-400 text-xs uppercase tracking-widest">
          <tr>
            <th class="px-6 py-4">ID</th>
            <th class="px-6 py-4">Nombre Completo</th>
            <th class="px-6 py-4 text-right">Acciones</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-800 text-slate-300">
          <tr v-for="t in trabajadoresFiltrados" :key="t.id" class="hover:bg-slate-800/40 transition-colors group">
            <td class="px-6 py-4 font-mono text-indigo-400 text-sm">#{{ t.id }}</td>
            <td class="px-6 py-4 font-medium text-slate-100 uppercase">{{ t.nombreCompleto }}</td>
            <td class="px-6 py-4 text-right space-x-2">
              <button @click="abrirModalEditar(t)" class="text-slate-500 hover:text-yellow-500 transition-colors p-2">
                ✏️
              </button>
              <button @click="eliminarTrabajador(t.id)" class="text-slate-500 hover:text-red-500 transition-colors p-2">
                🗑️
              </button>
            </td>
          </tr>
          <tr v-if="trabajadoresFiltrados.length === 0">
            <td colspan="3" class="px-6 py-10 text-center text-slate-500">No se encontraron trabajadores registrados.</td>
          </tr>
        </tbody>
      </table>
      
      <div class="p-4 bg-slate-900/20 text-xs text-slate-500 border-t border-slate-800">
        Total: {{ trabajadoresFiltrados.length }} trabajadores
      </div>
    </div>

    <div v-if="mostrarModal" class="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm">
      <div class="bg-[#1e293b] w-full max-w-md rounded-2xl border border-slate-700 shadow-2xl">
        <div class="p-6 border-b border-slate-700 flex justify-between items-center">
          <h3 class="text-xl font-bold text-white">{{ editando ? 'Editar' : 'Registrar' }} Trabajador</h3>
          <button @click="mostrarModal = false" class="text-slate-400 hover:text-white text-2xl">&times;</button>
        </div>
        
        <form @submit.prevent="guardarTrabajador" class="p-6 space-y-4">
          <div>
            <label class="block text-xs font-bold text-slate-400 uppercase mb-1">Nombre Completo</label>
            <input v-model="trabajadorForm.nombreCompleto" type="text" required placeholder="Ej: Julian Hernandez Moreno"
              class="w-full bg-[#0f172a] border border-slate-600 rounded-lg p-2.5 text-white focus:ring-2 focus:ring-indigo-500 outline-none" />
          </div>
          
          <div class="pt-4 flex gap-3 font-bold">
            <button type="button" @click="mostrarModal = false" 
              class="flex-1 bg-slate-700 text-white py-2.5 rounded-lg hover:bg-slate-600">
              Cancelar
            </button>
            <button type="submit" 
              class="flex-1 bg-indigo-600 text-white py-2.5 rounded-lg shadow-lg hover:bg-indigo-500 transition-colors">
              {{ editando ? 'Guardar Cambios' : 'Registrar' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <Transition name="fade">
      <div v-if="toast.mostrar" 
        :class="toast.tipo === 'success' ? 'bg-green-900/90 border-green-700 text-green-100' : 'bg-red-900/90 border-red-700 text-red-100'"
        class="fixed bottom-5 right-5 z-[110] p-4 rounded-lg border shadow-2xl flex items-center gap-3">
        <span>{{ toast.tipo === 'success' ? '✅' : '⚠️' }}</span>
        <span class="text-xs font-bold uppercase tracking-tight">{{ toast.msj }}</span>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>