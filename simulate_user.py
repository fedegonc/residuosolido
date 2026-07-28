"""
Simulador de interaccion de usuario con ResiduoSólido.
Requiere: pip install requests
Uso: python simulate_user.py [--base-url http://localhost:8080]
"""

import argparse
import re
import sys
from dataclasses import dataclass, field
from typing import Optional

import requests
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry


@dataclass
class SimulationResult:
    step: str
    success: bool
    detail: str = ""
    response_code: Optional[int] = None


@dataclass
class Simulator:
    base_url: str
    session: requests.Session = field(default_factory=requests.Session)
    results: list[SimulationResult] = field(default_factory=list)
    csrf_token: Optional[str] = None
    csrf_header: Optional[str] = None

    def __post_init__(self):
        retry = Retry(total=2, backoff_factor=0.5, status_forcelist=[502, 503, 504])
        self.session.mount(self.base_url, HTTPAdapter(max_retries=retry))

    def log(self, step: str, success: bool, detail: str = "", code: Optional[int] = None):
        r = SimulationResult(step, success, detail, code)
        self.results.append(r)
        icon = "OK" if success else "FAIL"
        line = f"  [{icon}] {step}"
        if detail:
            line += f" -- {detail}"
        if code:
            line += f" (HTTP {code})"
        print(line)

    def get(self, path: str, **kwargs) -> requests.Response:
        return self.session.get(f"{self.base_url}{path}", **kwargs)

    def post(self, path: str, **kwargs) -> requests.Response:
        return self.session.post(f"{self.base_url}{path}", **kwargs)

    def extract_csrf(self, html: str):
        token_match = re.search(r'name="_csrf"\s+content="([^"]+)"', html)
        header_match = re.search(r'name="_csrf_header"\s+content="([^"]+)"', html)
        if token_match:
            self.csrf_token = token_match.group(1)
        if header_match:
            self.csrf_header = header_match.group(1)
        # Fallback: buscar en input hidden
        if not self.csrf_token:
            input_match = re.search(r'name="_csrf"\s+value="([^"]+)"', html)
            if input_match:
                self.csrf_token = input_match.group(1)
                self.csrf_header = "X-CSRF-TOKEN"

    def csrf_headers(self) -> dict:
        h = {}
        if self.csrf_token and self.csrf_header:
            h[self.csrf_header] = self.csrf_token
        return h

    # --- Steps ---

    def step_1_visit_home(self):
        """Usuario visita la pagina principal."""
        r = self.get("/")
        ok = r.status_code == 200 and "ResiduoS" in r.text
        self.log("Visitar home", ok, "Pagina principal cargada", r.status_code)

    def step_2_visit_login(self):
        """Usuario navega al login."""
        r = self.get("/auth/login")
        ok = r.status_code == 200 and ("login" in r.text.lower() or "senha" in r.text.lower() or "contrase" in r.text.lower())
        self.extract_csrf(r.text)
        self.log("Visitar login", ok, "Formulario de login visible", r.status_code)

    def step_3_register_user(self, username: str, password: str, email: str, first_name: str):
        """Usuario se registra como usuario comun."""
        r = self.get("/auth/register")
        self.extract_csrf(r.text)
        data = {
            "username": username,
            "password": password,
            "email": email,
            "firstName": first_name,
            "_csrf": self.csrf_token or "",
        }
        r = self.post("/auth/register", data=data, allow_redirects=False)
        # Esperamos redirect a /auth/login?success
        ok = r.status_code in (301, 302, 303) and "success" in r.headers.get("Location", "")
        self.log("Registrar usuario comun", ok, f"Redirect: {r.headers.get('Location', 'N/A')}", r.status_code)

    def step_4_login_user(self, username: str, password: str):
        """Usuario inicia sesion."""
        data = {
            "username": username,
            "password": password,
            "_csrf": self.csrf_token or "",
        }
        r = self.post("/auth/login", data=data, allow_redirects=False)
        location = r.headers.get("Location", "")
        ok = r.status_code in (301, 302, 303) and "/usuarios/inicio" in location
        self.log("Login usuario", ok, f"Redirect: {location}", r.status_code)
        # Seguir redirect para extraer nuevo CSRF
        if ok:
            r2 = self.get("/usuarios/inicio")
            self.extract_csrf(r2.text)

    def step_5_visit_user_dashboard(self):
        """Usuario ve su dashboard."""
        r = self.get("/usuarios/inicio")
        ok = r.status_code == 200
        self.log("Ver dashboard usuario", ok, "Dashboard cargado", r.status_code)

    def step_6_visit_user_profile(self):
        """Usuario ve su perfil."""
        r = self.get("/usuarios/perfil")
        ok = r.status_code == 200
        self.log("Ver perfil usuario", ok, "Perfil cargado", r.status_code)

    def step_7_update_user_profile(self, email: str, first_name: str, phone: str):
        """Usuario actualiza su perfil."""
        data = {
            "email": email,
            "firstName": first_name,
            "phone": phone,
            "_csrf": self.csrf_token or "",
        }
        r = self.post("/usuarios/perfil", data=data, allow_redirects=False)
        ok = r.status_code in (301, 302, 303) and "/usuarios/perfil" in r.headers.get("Location", "")
        self.log("Actualizar perfil usuario", ok, f"Redirect: {r.headers.get('Location', 'N/A')}", r.status_code)

    def step_8_create_request(self, city: str, address: str, materials: list[str]):
        """Usuario crea una solicitud de recoleccion."""
        r = self.get("/solicitudes/nueva")
        self.extract_csrf(r.text)
        data = {
            "city": city,
            "address": address,
            "addressReference": "",
            "materials": materials,
            "_csrf": self.csrf_token or "",
        }
        r = self.post("/solicitudes/nueva", data=data, allow_redirects=False)
        location = r.headers.get("Location", "")
        ok = r.status_code in (301, 302, 303) and ("success" in location or "/solicitudes" in location)
        self.log("Crear solicitud", ok, f"Redirect: {location}", r.status_code)

    def step_9_list_user_requests(self):
        """Usuario ve sus solicitudes."""
        r = self.get("/solicitudes")
        ok = r.status_code == 200
        self.log("Ver mis solicitudes", ok, "Lista cargada", r.status_code)

    def step_10_create_guest_request(self, city: str, address: str, materials: list[str], guest_name: str, guest_phone: str):
        """Invitado (sin cuenta) crea una solicitud."""
        # Usar sesion limpia sin cookies
        guest_session = requests.Session()
        r = guest_session.get(f"{self.base_url}/solicitudes/nueva")
        csrf_token = None
        token_match = re.search(r'name="_csrf"\s+content="([^"]+)"', r.text)
        if token_match:
            csrf_token = token_match.group(1)
        if not csrf_token:
            input_match = re.search(r'name="_csrf"\s+value="([^"]+)"', r.text)
            if input_match:
                csrf_token = input_match.group(1)
        data = {
            "city": city,
            "address": address,
            "addressReference": "",
            "materials": materials,
            "guestName": guest_name,
            "guestPhone": guest_phone,
            "_csrf": csrf_token or "",
        }
        r = guest_session.post(f"{self.base_url}/solicitudes/nueva", data=data, allow_redirects=False)
        location = r.headers.get("Location", "")
        ok = r.status_code in (301, 302, 303) and ("success" in location or "/solicitudes" in location)
        self.log("Crear solicitud como invitado", ok, f"Redirect: {location}", r.status_code)

    def step_11_access_org_panel_as_user(self):
        """Usuario comun intenta acceder al panel de organizacion (debe fallar)."""
        r = self.get("/acopio/inicio", allow_redirects=False)
        # Debe ser redirigido a login o dar 403
        ok = r.status_code in (301, 302, 303, 403)
        detail = f"Redirect: {r.headers.get('Location', 'N/A')}" if r.status_code in (301, 302, 303) else "Acceso denegado"
        self.log("Usuario comun intenta acceder a /acopio (debe fallar)", ok, detail, r.status_code)

    def step_12_visit_public_metrics(self):
        """Usuario visita metricas publicas."""
        r = self.get("/metricas")
        ok = r.status_code == 200
        self.log("Ver metricas publicas", ok, "Pagina de metricas cargada", r.status_code)

    def step_13_visit_404(self):
        """Usuario visita una pagina inexistente."""
        r = self.get("/pagina-que-no-existe", allow_redirects=False)
        # Debe redirigir (error handler) o dar 404
        ok = r.status_code in (301, 302, 303, 404)
        detail = f"Redirect: {r.headers.get('Location', 'N/A')}" if r.status_code in (301, 302, 303) else "404"
        self.log("Visitar pagina inexistente (404)", ok, detail, r.status_code)

    def step_14_change_language(self, lang: str):
        """Usuario cambia el idioma."""
        r = self.get(f"/change-language?lang={lang}", allow_redirects=False)
        ok = r.status_code in (301, 302, 303, 200)
        self.log(f"Cambiar idioma a '{lang}'", ok, f"HTTP {r.status_code}", r.status_code)

    def step_15_logout(self):
        """Usuario cierra sesion."""
        data = {"_csrf": self.csrf_token or ""}
        r = self.post("/logout", data=data, allow_redirects=False)
        ok = r.status_code in (301, 302, 303) and r.headers.get("Location", "") in ("/", "")
        self.log("Logout", ok, f"Redirect: {r.headers.get('Location', 'N/A')}", r.status_code)

    # --- Organization flow ---

    def step_16_register_organization(self, username: str, password: str, email: str, first_name: str):
        """Registrar una organizacion."""
        r = self.get("/auth/register")
        self.extract_csrf(r.text)
        data = {
            "username": username,
            "password": password,
            "email": email,
            "firstName": first_name,
            "isOrganization": "on",
            "_csrf": self.csrf_token or "",
        }
        r = self.post("/auth/register", data=data, allow_redirects=False)
        ok = r.status_code in (301, 302, 303) and "success" in r.headers.get("Location", "")
        self.log("Registrar organizacion", ok, f"Redirect: {r.headers.get('Location', 'N/A')}", r.status_code)

    def step_17_login_organization(self, username: str, password: str):
        """Organizacion inicia sesion."""
        data = {
            "username": username,
            "password": password,
            "_csrf": self.csrf_token or "",
        }
        r = self.post("/auth/login", data=data, allow_redirects=False)
        location = r.headers.get("Location", "")
        ok = r.status_code in (301, 302, 303) and "/acopio" in location
        self.log("Login organizacion", ok, f"Redirect: {location}", r.status_code)
        if ok:
            r2 = self.get(location)
            self.extract_csrf(r2.text)

    def step_18_complete_org_profile(self, phone: str, city: str):
        """Organizacion completa su perfil."""
        r = self.get("/acopio/completar-perfil")
        self.extract_csrf(r.text)
        data = {
            "phone": phone,
            "city": city,
            "_csrf": self.csrf_token or "",
        }
        r = self.post("/acopio/completar-perfil", data=data, allow_redirects=False)
        ok = r.status_code in (301, 302, 303) and "/acopio/inicio" in r.headers.get("Location", "")
        self.log("Completar perfil organizacion", ok, f"Redirect: {r.headers.get('Location', 'N/A')}", r.status_code)

    def step_19_visit_org_dashboard(self):
        """Organizacion ve su dashboard."""
        r = self.get("/acopio/inicio")
        ok = r.status_code == 200
        self.log("Ver dashboard organizacion", ok, "Dashboard cargado", r.status_code)

    def step_20_visit_org_requests(self):
        """Organizacion ve sus solicitudes."""
        r = self.get("/acopio/requests")
        ok = r.status_code == 200
        self.log("Ver solicitudes de organizacion", ok, "Lista cargada", r.status_code)

    def step_21_visit_org_profile(self):
        """Organizacion ve su perfil."""
        r = self.get("/acopio/perfil")
        ok = r.status_code == 200
        self.log("Ver perfil organizacion", ok, "Perfil cargado", r.status_code)

    # --- Summary ---

    def print_summary(self):
        total = len(self.results)
        passed = sum(1 for r in self.results if r.success)
        failed = total - passed
        print("\n" + "=" * 60)
        print(f"  RESUMEN: {passed}/{total} pasos exitosos | {failed} fallos")
        print("=" * 60)
        if failed > 0:
            print("\n  Pasos fallidos:")
            for r in self.results:
                if not r.success:
                    print(f"    - {r.step}: {r.detail} (HTTP {r.response_code})")
        print()


def main():
    parser = argparse.ArgumentParser(description="Simulador de usuario para ResiduoSólido")
    parser.add_argument("--base-url", default="http://localhost:8080", help="URL base de la aplicacion")
    args = parser.parse_args()

    sim = Simulator(base_url=args.base_url.rstrip("/"))

    # Datos de prueba
    user_data = {
        "username": f"simuser_{int(__import__('time').time()) % 10000}",
        "password": "Test1234!",
        "email": f"simuser_{int(__import__('time').time()) % 10000}@test.com",
        "first_name": "Usuario Simulado",
    }
    org_data = {
        "username": f"simorg_{int(__import__('time').time()) % 10000}",
        "password": "Test1234!",
        "email": f"simorg_{int(__import__('time').time()) % 10000}@test.com",
        "first_name": "Cooperativa Simulada",
    }

    print("\n  === SIMULACION: FLUJO DE USUARIO COMUN ===\n")

    sim.step_1_visit_home()
    sim.step_2_visit_login()
    sim.step_3_register_user(user_data["username"], user_data["password"], user_data["email"], user_data["first_name"])
    sim.step_4_login_user(user_data["username"], user_data["password"])
    sim.step_5_visit_user_dashboard()
    sim.step_6_visit_user_profile()
    sim.step_7_update_user_profile(user_data["email"], "Nombre Actualizado", "099123456")
    sim.step_8_create_request("RIVERA", "Calle Test 123", ["PLASTICO", "PAPEL"])
    sim.step_9_list_user_requests()
    sim.step_11_access_org_panel_as_user()
    sim.step_12_visit_public_metrics()
    sim.step_14_change_language("pt")
    sim.step_14_change_language("es")
    sim.step_15_logout()

    print("\n  === SIMULACION: FLUJO DE INVITADO ===\n")
    sim.step_10_create_guest_request("RIVERA", "Calle Guest 456", ["VIDRIO"], "Invitado Test", "099876543")

    print("\n  === SIMULACION: FLUJO DE ORGANIZACION ===\n")
    sim.step_16_register_organization(org_data["username"], org_data["password"], org_data["email"], org_data["first_name"])
    sim.step_17_login_organization(org_data["username"], org_data["password"])
    sim.step_18_complete_org_profile("099123456", "RIVERA")
    sim.step_19_visit_org_dashboard()
    sim.step_20_visit_org_requests()
    sim.step_21_visit_org_profile()
    sim.step_15_logout()

    print("\n  === SIMULACION: ERRORES ===\n")
    sim.step_13_visit_404()

    sim.print_summary()

    failed = sum(1 for r in sim.results if not r.success)
    sys.exit(1 if failed > 0 else 0)


if __name__ == "__main__":
    main()
