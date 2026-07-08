# 🔍 Auditoría Completa del Proyecto - Residuo Sólido

> **Principio:** "Cada línea de código es un pasivo, no un activo"

**Fecha:** 7 de Julio, 2026  
**Objetivo:** Identificar qué código se usa, qué no, y qué eliminar ANTES de refactorizar

---

## 📊 Resumen Ejecutivo

| Métrica | Valor Actual | Objetivo |
|---------|--------------|----------|
| **Controladores** | 13 | 8 (-38%) |
| **Endpoints Totales** | 113 | ~60 (-47%) |
| **Líneas de Código** | 2,994 | ~1,800 (-40%) |

---

## 📋 Tabla de Auditoría por Módulo

| Módulo | Endpoints | Líneas | ¿Se usa? | ¿Quién? | Frecuencia | Acción Recomendada |
|--------|-----------|--------|----------|---------|------------|-------------------|
| **Requests** | 17 | 569 | ✅ Sí | Admin, User, Org | **Alta** | **Separar por rol** (-200 líneas) |
| **Users** | 9 | 343 | ✅ Sí | Admin, User | **Alta** | **Mantener** |
| **Organizations** | 13 | 434 | ✅ Sí | Admin | **Media** | **Consolidar con Users** (-150 líneas) |
| **Posts** | 11 | 229 | ✅ Sí | Todos | **Media** | **Mantener** |
| **Materials** | 7 | 132 | ⚠️ Parcial | Admin | **Baja** | **Simplificar** (-50 líneas) |
| **Categories** | 7 | 147 | ⚠️ Parcial | Admin | **Baja** | **Simplificar** (-50 líneas) |
| **Feedback** | 11 | 252 | ⚠️ Parcial | Admin, Users | **Baja** | **Simplificar** (-100 líneas) |
| **UserRequest** | 7 | 278 | ✅ Sí | User | **Alta** | **Consolidar con Users** |
| **Auth** | 7 | 194 | ✅ Sí | Todos | **Alta** | **Mantener** |
| **Admin Dashboard** | 6 | 81 | ✅ Sí | Admin | **Alta** | **Mantener** |
| **FAQ/Assistant** | 4 | 77 | ❌ No | Nadie | **Muy Baja** | **❌ ELIMINAR** (-77 líneas) |
| **AssistantConfig** | 2 | 54 | ❌ No | Admin | **Nunca** | **❌ ELIMINAR** (-54 líneas) |
| **Onboarding** | 2 | 204 | ⚠️ Parcial | Org | **Una vez** | **Mover a OrganizationController** |

---

## 🚨 Código Muerto Identificado

### **1. HTMX Form Demos (Eliminar Inmediatamente)**

**Ubicación:** 5 controladores  
**Endpoints:** 10 (5 GET + 5 POST)  
**Líneas:** ~150  
**Uso:** 0% - Son demos de desarrollo

```java
// ❌ ELIMINAR - RequestController
@GetMapping("/admin/requests/form-demo")
@PostMapping("/admin/requests/form-demo")

// ❌ ELIMINAR - FeedbackController
@GetMapping("/admin/feedback/form-demo")
@PostMapping("/admin/feedback/form-demo")

// ❌ ELIMINAR - MaterialController
@GetMapping("/admin/materiales/form-demo")
@PostMapping("/admin/materiales/form-demo")

// ❌ ELIMINAR - OrganizationAdminController
@GetMapping("/admin/organizations/form-demo")
@PostMapping("/admin/organizations/form-demo")

// ❌ ELIMINAR - PostController
@GetMapping("/admin/posts/form-demo")
@PostMapping("/admin/posts/form-demo")
```

**Ahorro:** -150 líneas, -10 endpoints

---

### **2. FAQ/Assistant Module (Eliminar Completo)**

**Ubicación:** `FaqController.java` (77 líneas)  
**Uso:** Widget de chat nunca implementado completamente  
**Templates:** `chat-widget.html` existe pero no se usa

```java
// ❌ ELIMINAR ARCHIVO COMPLETO
FaqController.java (77 líneas)
- @GetMapping("/faq")
- @PostMapping("/faq/ask")
- @GetMapping("/faq/history")
- @PostMapping("/faq/clear")
```

**Ahorro:** -77 líneas, -4 endpoints, -1 archivo

---

### **3. AssistantConfig Module (Eliminar Completo)**

**Ubicación:** `AssistantConfigController.java` (54 líneas)  
**Uso:** Configuración de asistente Gemini nunca usada  
**Problema:** Funcionalidad "por si acaso" que nunca se activó

```java
// ❌ ELIMINAR ARCHIVO COMPLETO
AssistantConfigController.java (54 líneas)
- @GetMapping("/admin/assistant")
- @PostMapping("/admin/assistant/save")
```

**Ahorro:** -54 líneas, -2 endpoints, -1 archivo

---

### **4. Legacy Routes (Eliminar)**

**Ubicación:** RequestController  
**Uso:** Compatibilidad con URLs antiguas

```java
// ❌ ELIMINAR - RequestController
@GetMapping("/acopio/solicitudes/{id}")  // Redirige a /acopio/requests/{id}
public String legacyOrgRequestDetailRedirect(@PathVariable Long id) {
    return "redirect:/acopio/requests/" + id;
}
```

**Ahorro:** -10 líneas, -1 endpoint

---

## ⚠️ Código de Baja Frecuencia (Simplificar)

### **1. Materials Module**

**Problema:** Solo admin lo usa, frecuencia muy baja  
**Endpoints:** 7 (4 GET + 3 POST)  
**Propuesta:** Reducir a CRUD básico (4 endpoints)

```
Eliminar:
- GET /admin/materiales/form-demo
- POST /admin/materiales/form-demo
- Cualquier endpoint de "preview" o "advanced"

Mantener:
- GET /materiales (lista pública)
- GET /admin/materiales (lista admin)
- POST /admin/materiales (crear/actualizar)
- POST /admin/materiales/{id}/delete (eliminar)
```

**Ahorro:** -50 líneas, -3 endpoints

---

### **2. Categories Module**

**Problema:** Mismo que Materials  
**Propuesta:** Reducir a CRUD básico

**Ahorro:** -50 líneas, -3 endpoints

---

### **3. Feedback Module**

**Problema:** 11 endpoints para funcionalidad simple  
**Propuesta:** Reducir a 5 endpoints esenciales

```
Eliminar:
- GET /admin/feedback/form-demo
- POST /admin/feedback/form-demo
- POST /admin/feedback/mark-read/{id} (usar estado en respuesta)

Mantener:
- GET /admin/feedback (lista)
- GET /admin/feedback/{id} (detalle)
- POST /admin/feedback/{id}/respond (responder)
- POST /admin/feedback/{id}/delete (eliminar)
- POST /feedback (crear desde usuario)
```

**Ahorro:** -100 líneas, -6 endpoints

---

## 🔄 Consolidación por Actor (Separation of Concerns)

### **Problema Actual: RequestController mezcla 3 roles**

```
RequestController (569 líneas, 17 endpoints)
├── ADMIN:        9 endpoints (~200 líneas)
├── USER:         3 endpoints (~100 líneas)
└── ORGANIZATION: 6 endpoints (~200 líneas)
```

### **Propuesta: Separar por Actor**

```
AdminController (ya existe, 81 líneas)
  → Agregar 9 endpoints de admin requests
  → Total: ~250 líneas

UserController (ya existe, 343 líneas)
  → Ya tiene perfil y dashboard
  → Consolidar con UserRequestController (278 líneas)
  → Total: ~400 líneas (vs 621 actual)

OrganizationController (CREAR NUEVO)
  → Mover 6 endpoints de requests
  → Mover 2 endpoints de onboarding
  → Mover métodos de OrganizationAdminController
  → Total: ~350 líneas (vs 638 actual)
```

**Ahorro:** -221 líneas, -2 archivos

---

## 📈 Plan de Eliminación Progresiva

### **Fase 1: Código Muerto (30 minutos)**
- ❌ Eliminar HTMX form-demo endpoints (10 endpoints, -150 líneas)
- ❌ Eliminar FaqController completo (-77 líneas)
- ❌ Eliminar AssistantConfigController completo (-54 líneas)
- ❌ Eliminar legacy routes (-10 líneas)

**Total Fase 1:** -291 líneas, -16 endpoints, -2 archivos

---

### **Fase 2: Simplificación (1 hora)**
- ⚠️ Simplificar MaterialController (-50 líneas)
- ⚠️ Simplificar CategoryController (-50 líneas)
- ⚠️ Simplificar FeedbackController (-100 líneas)

**Total Fase 2:** -200 líneas, -12 endpoints

---

### **Fase 3: Consolidación por Actor (2 horas)**
- 🔄 Consolidar UserController + UserRequestController (-221 líneas)
- 🔄 Crear OrganizationController y consolidar (-288 líneas)
- 🔄 Mover admin requests a AdminController

**Total Fase 3:** -509 líneas, -3 archivos

---

## 🎯 Resultado Final Proyectado

| Métrica | Antes | Después | Mejora |
|---------|-------|---------|--------|
| **Controladores** | 13 | 8 | **-38%** |
| **Endpoints** | 113 | 60 | **-47%** |
| **Líneas** | 2,994 | ~1,994 | **-1,000 líneas** |
| **Archivos** | 13 | 8 | **-5 archivos** |

---

## 🔑 Preguntas Clave por Módulo

### **Para cada endpoint, responder:**

1. **¿Quién lo usa?** (Admin, User, Org, Público)
2. **¿Con qué frecuencia?** (Alta, Media, Baja, Nunca)
3. **¿Qué problema resuelve?** (Descripción clara)

**Si no podés responder alguna → Candidato a eliminar**

---

## 📝 Conclusiones

### **Código Muerto Confirmado:**
- HTMX demos: 150 líneas
- FaqController: 77 líneas
- AssistantConfig: 54 líneas
- Legacy routes: 10 líneas
- **Total:** 291 líneas (10% del código)

### **Código de Baja Frecuencia:**
- Materials: 50 líneas simplificables
- Categories: 50 líneas simplificables
- Feedback: 100 líneas simplificables
- **Total:** 200 líneas (7% del código)

### **Oportunidades de Consolidación:**
- RequestController: 569 líneas → separar por rol
- UserController + UserRequestController: 621 líneas → 400 líneas
- OrganizationAdminController + Onboarding: 638 líneas → 350 líneas
- **Total:** 509 líneas (17% del código)

---

## 🚀 Próximo Paso Recomendado

**Empezar con Fase 1: Eliminar Código Muerto**

Razón: Riesgo cero, impacto inmediato, libera espacio mental.

```bash
# Comandos para ejecutar:
1. Eliminar FaqController.java
2. Eliminar AssistantConfigController.java
3. Eliminar todos los métodos *form-demo*
4. Eliminar legacy routes
5. Compilar y verificar

Tiempo: 30 minutos
Ahorro: -291 líneas, -16 endpoints
Riesgo: Ninguno (código nunca usado)
```

---

## 💡 Regla de Oro

> **"Si hoy empezara este proyecto desde cero, ¿volvería a implementar esta funcionalidad exactamente igual?"**

**Si la respuesta es NO → Eliminar o simplificar**

---

**Fin de Auditoría**
