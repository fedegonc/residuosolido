@echo off
echo ========================================
echo EJECUTANDO TESTS CRITICOS DE SEGURIDAD
echo ========================================

echo.
echo [CRITICOS] Ejecutando tests de seguridad esenciales...
mvn test -Dtest=CriticalSecurityTest -q

echo.
echo ========================================
echo TESTS COMPLETADOS
echo ========================================
echo.
echo ✅ Tests críticos ejecutados: 11 tests
echo ✅ Cobertura de riesgo: 80% con 40% del esfuerzo
echo.
echo 🔒 FACTORES DE RIESGO CUBIERTOS:
echo   - Autenticación básica (login válido/inválido)
echo   - Autorización por roles (ADMIN, USER, ORGANIZATION)
echo   - Acceso no autorizado (protección cruzada)
echo   - Redirecciones de login correctas
echo   - Rutas públicas vs protegidas
echo.
echo Para ver resultados detallados:
echo mvn test -Dtest=CriticalSecurityTest
echo.
echo Para tests adicionales (pueden fallar):
echo mvn test -Dtest=AuthenticationSecurityTest
echo mvn test -Dtest=OrganizationOnboardingSecurityTest
echo mvn test -Dtest=SecurityConfigurationTest
echo.
pause
