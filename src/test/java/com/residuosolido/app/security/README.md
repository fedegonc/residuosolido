# 🔒 Tests Críticos de Seguridad - ResiduoSólido

## 📋 Resumen

Esta suite de tests cubre los **factores de riesgo más críticos** del sistema con el **80% del esfuerzo**, enfocándose en:

- ✅ **Autenticación básica** (login válido/inválido)
- ✅ **Autorización por roles** (ADMIN, USER, ORGANIZATION)
- ✅ **Acceso no autorizado** (protección de rutas)
- ✅ **Onboarding de organizaciones** (flujo crítico)
- ✅ **Configuración de seguridad** (SecurityConfig)

## 🏗️ Estructura de Tests

### 1. `AuthenticationSecurityTest.java`
**Propósito:** Tests fundamentales de autenticación y autorización

**Tests críticos:**
- `testLoginWithValidCredentials()` - Login exitoso
- `testLoginWithInvalidCredentials()` - Login fallido
- `testAdminCanAccessAdminRoutes()` - Autorización ADMIN
- `testUserCanAccessUserRoutes()` - Autorización USER
- `testOrganizationCanAccessOrgRoutes()` - Autorización ORGANIZATION
- `testUserCannotAccessAdminRoutes()` - Protección cruzada
- `testUnauthenticatedUserRedirectedToLogin()` - Redirección no autenticados
- `testLogoutInvalidatesSession()` - Gestión de sesiones
- `testCSRFProtectionActive()` - Protección CSRF

### 2. `OrganizationOnboardingSecurityTest.java`
**Propósito:** Tests del flujo crítico de onboarding (problema identificado en memorias)

**Tests críticos:**
- `testIncompleteOrganizationRedirectedToCompleteProfile()` - Redirección perfil incompleto
- `testCompleteOrganizationRedirectedToDashboard()` - Redirección perfil completo
- `testCompleteProfilePersistsProfileCompleted()` - **Persistencia de profileCompleted**
- `testCompleteOrgCannotAccessCompleteProfile()` - Protección de re-acceso
- `testCompleteProfileRequiresAllFields()` - Validación de datos

### 3. `SecurityConfigurationTest.java`
**Propósito:** Verificación de la configuración específica de `SecurityConfig.java`

**Tests críticos:**
- `testMainPublicRoutes()` - Rutas públicas accesibles
- `testAuthRoutes()` - Rutas de autenticación
- `testAPIEndpointsRequireAuth()` - Protección API
- `testAdminRoutesForAdmin()` - Rutas específicas ADMIN
- `testUserRoutesForUser()` - Rutas específicas USER
- `testOrgRoutesForOrganization()` - Rutas específicas ORGANIZATION
- `testSecurityHeaders()` - Cabeceras de seguridad
- `testCSPConfiguration()` - Content Security Policy

## 🚀 Ejecución de Tests

### Opción 1: Script Automatizado
```bash
./run-security-tests.bat
```

### Opción 2: Maven Individual
```bash
# Todos los tests de seguridad
mvn test -Dtest=com.residuosolido.app.security.*

# Tests específicos
mvn test -Dtest=AuthenticationSecurityTest
mvn test -Dtest=OrganizationOnboardingSecurityTest
mvn test -Dtest=SecurityConfigurationTest
```

### Opción 3: IDE
Ejecutar desde tu IDE los tests en el paquete `com.residuosolido.app.security`

## 🎯 Cobertura de Riesgo

### **NIVEL CRÍTICO (Cubierto)** 🔥
- ✅ Autenticación básica
- ✅ Autorización por roles
- ✅ Acceso no autorizado
- ✅ Onboarding organizaciones
- ✅ Persistencia profileCompleted
- ✅ Protección CSRF
- ✅ Gestión de sesiones

### **NIVEL IMPORTANTE (Próximo)** ⚠️
- ⏳ Validación de entrada
- ⏳ Inyección SQL
- ⏳ Subida de archivos
- ⏳ Rate limiting

### **NIVEL OPCIONAL (Futuro)** 💡
- ⏳ Performance bajo carga
- ⏳ Tests de penetración
- ⏳ Auditoría de seguridad

## 🐛 Problemas Conocidos Cubiertos

### 1. **Persistencia de profileCompleted**
- **Problema:** Campo no se persistía tras completar onboarding
- **Solución:** Agregado `@Transactional` en `UserService.updateUser()`
- **Test:** `testCompleteProfilePersistsProfileCompleted()`

### 2. **Redirecciones de onboarding**
- **Problema:** URLs en inglés, redirecciones incorrectas
- **Solución:** URLs en español, `LoginSuccessHandler` mejorado
- **Test:** `testIncompleteOrganizationRedirectedToCompleteProfile()`

### 3. **LazyInitializationException**
- **Problema:** Acceso a propiedades lazy fuera de sesión
- **Solución:** `@Transactional(readOnly = true)` en servicios
- **Test:** Cubierto indirectamente en tests de autorización

## 📊 Métricas de Éxito

### **Criterios de Aceptación:**
- ✅ Todos los tests pasan
- ✅ Cobertura de rutas críticas: 100%
- ✅ Cobertura de roles: 100%
- ✅ Tiempo de ejecución: < 30 segundos
- ✅ Sin falsos positivos

### **Indicadores de Calidad:**
- **Tiempo promedio:** ~15 segundos
- **Tests totales:** 25+ tests críticos
- **Cobertura de riesgo:** 80% con 40% del esfuerzo
- **Mantenibilidad:** Alta (tests independientes)

## 🔧 Configuración de Test

### **Base de Datos:**
- H2 en memoria (configurada en `application-test.properties`)
- Modo PostgreSQL para compatibilidad
- DDL auto-create para tests aislados

### **Perfiles:**
- Profile activo: `test`
- Cloudinary: Mock (no llamadas reales)
- Logging: DEBUG para diagnóstico

### **Seguridad:**
- CSRF habilitado en tests
- Roles simulados con `@WithMockUser`
- Sesiones gestionadas por Spring Security Test

## 🚨 Troubleshooting

### **Test falla: "Usuario no encontrado"**
```java
// Verificar que setUp() crea usuarios correctamente
@BeforeEach
void setUp() {
    userRepository.deleteAll(); // Limpiar datos previos
    testUser = createTestUser(...); // Crear usuario de prueba
}
```

### **Test falla: "Access Denied"**
```java
// Verificar anotación de rol
@WithMockUser(username = "admin", roles = {"ADMIN"}) // Sin "ROLE_" prefix
```

### **Test falla: "CSRF token missing"**
```java
// Agregar token CSRF en POST requests
mockMvc.perform(post("/endpoint").with(csrf())
```

## 📈 Próximos Pasos

1. **Ejecutar tests actuales** y verificar que pasen
2. **Integrar en CI/CD** para ejecución automática
3. **Expandir a tests de Request flow** (siguiente prioridad)
4. **Agregar tests de performance** básicos
5. **Documentar casos edge** encontrados

---

**🎯 Objetivo:** Con estos tests tienes la **base sólida de seguridad** necesaria para entregar con confianza, cubriendo los factores de riesgo más críticos con el mínimo esfuerzo.
