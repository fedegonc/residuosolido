# Logs de Render.com vía MCP — Devin CLI

> **Propósito:** explicar cómo conectar **Devin CLI** al servidor MCP oficial
> de Render.com para consultar los logs del servicio desplegado
> (EcoSolicitud) desde la terminal, sin entrar al Dashboard.
>
> **Alcance:** solo lectura de logs/estado de deploy. El MCP de Render también
> permite acciones destructivas (redeploy, cambiar variables de entorno); este
> documento no las cubre.

---

## Contexto

- El sistema se despliega en Render.com como PaaS con deploy automático desde
  GitHub (ver `docs/MEJORAS.md`, fila #30).
- El repositorio **no** contiene archivos de configuración de Render
  (`render.yaml` u otros). El servicio se configura desde el Dashboard de
  Render y se construye a partir del `Dockerfile` del repo. El pipeline de
  `.github/workflows/ci.yml` solo corre tests, PMD, JaCoCo y un smoke test de
  Docker; no despliega.
- La fila #36 de `docs/MEJORAS.md` ("MCP de AWS") fue descartada por falta de
  credenciales AWS. Este documento es independiente: se trata del **MCP oficial
  de Render**, que sí aplica al despliegue actual.

---

## Qué es el MCP de Render

Model Context Protocol (MCP) es un estándar abierto para conectar agentes de IA
con herramientas externas. Render publica un servidor MCP hosteado en:

```
https://mcp.render.com/mcp
```

El servidor expone "tools" que llaman a la API de Render. Las relevantes para
este documento son:

| Tool | Descripción |
|---|---|
| `list_workspaces` / `select_workspace` | Listar y elegir el workspace de Render |
| `list_services` / `get_service` | Listar servicios y ver detalle (incluye estado de deploy) |
| `list_logs` | Listar logs filtrando por servicio, nivel, texto, rango de tiempo |
| `list_log_label_values` | Listar valores posibles de una etiqueta de log (ej. `level`) |

Fuentes oficiales (verificar ante cualquier cambio de versión):

- Render: https://render.com/docs/mcp-server
- Código del servidor: https://github.com/render-oss/render-mcp-server
- Devin CLI (MCP): https://docs.devin.ai/cli/extensibility/mcp/configuration

---

## Paso 1 — Obtener una API key de Render

1. Ingresar al Dashboard de Render: https://dashboard.render.com
2. Ir a **Account Settings → API Keys**.
3. Crear una API key nueva (ej. `devin-cli-logs`) y copiarla; solo se muestra
   una vez.

> **Nunca** commitear la API key al repositorio. Usar una variable de entorno
> o el archivo local gitignorado de Devin CLI (ver Paso 2).

Exportar la key en la shell (Linux/macOS):

```bash
export RENDER_API_KEY="rnd_xxxxxxxxxxxxxxxxxxxx"   # placeholder, reemplazar
```

---

## Paso 2 — Registrar el servidor MCP en Devin CLI

Devin CLI administra servidores MCP con `devin mcp add`. Por defecto guarda la
configuración en **scope local** (`.devin/mcp_config.local.json`, gitignorado),
que es el lugar correcto para configuraciones con credenciales personales.

### Opción A — Servidor remoto (HTTP) con API key (recomendada)

Render hostea el servidor, no hay nada que instalar:

```bash
devin mcp add render https://mcp.render.com/mcp \
  --header "Authorization: Bearer $RENDER_API_KEY"
```

Equivalente en archivo `.devin/mcp_config.local.json` (referenciando la variable
de entorno para no escribir la key en el archivo):

```json
{
  "mcpServers": {
    "render": {
      "url": "https://mcp.render.com/mcp",
      "headers": {
        "Authorization": "Bearer ${env:RENDER_API_KEY}"
      }
    }
  }
}
```

### Opción B — Servidor remoto (HTTP) con OAuth

Sin API key; la autorización se hace en el navegador:

```bash
devin mcp add render https://mcp.render.com/mcp
devin mcp login render
```

Si Render rechaza el registro dinámico de cliente, la documentación oficial de
Render indica el `client-id` a usar según la herramienta; en Devin CLI se pasa
con `--oauth-client-id`.

### Opción C — Puente stdio vía `npx` (`mcp-remote`)

Para clientes que solo soportan stdio, Render documenta el uso del paquete
`mcp-remote` como puente hacia el servidor HTTP. En Devin CLI:

```bash
devin mcp add render -e RENDER_API_KEY="$RENDER_API_KEY" -- \
  npx -y mcp-remote https://mcp.render.com/mcp \
  --header "Authorization: Bearer ${RENDER_API_KEY}"
```

Equivalente en `.devin/mcp_config.local.json`:

```json
{
  "mcpServers": {
    "render": {
      "command": "npx",
      "args": [
        "-y",
        "mcp-remote",
        "https://mcp.render.com/mcp",
        "--header",
        "Authorization: Bearer ${RENDER_API_KEY}"
      ],
      "env": {
        "RENDER_API_KEY": "<TU_API_KEY>"
      }
    }
  }
}
```

> **Importante:** los nombres exactos de paquete (`mcp-remote`), la URL del
> servidor, las opciones de `devin mcp add` (`--header`, `-e`, `--scope`,
> `--oauth-client-id`) y la ubicación de los archivos de configuración pueden
> cambiar entre versiones. Confirmar siempre con la documentación oficial de
> Render y de Devin CLI antes de copiar los comandos.

### Verificar

```bash
devin mcp list          # debe aparecer "render"
devin mcp get render    # muestra transporte, URL y estado
```

---

## Paso 3 — Consultar los logs del servicio

Una vez conectado, abrir `devin` en el repo y usar prompts en lenguaje natural.
El agente elige las tools del MCP.

1. **Elegir el workspace** (obligatorio la primera vez; cada acción está
   acotada a un workspace):

   > Listá mis workspaces de Render y seleccioná el de EcoSolicitud.

2. **Identificar el servicio y su estado de deploy:**

   > Listá mis servicios de Render y mostrá el detalle del servicio
   > `residuosolido` (último deploy, estado, commit desplegado).

3. **Pedir logs:**

   > Traé los últimos 100 logs del servicio `residuosolido`.

   > Mostrá solo los logs de nivel `error` de la última hora del servicio
   > `residuosolido`.

   > Buscá en los logs del servicio `residuosolido` las líneas que contengan
   > `MongoTimeoutException` entre las 10:00 y las 11:00 UTC de hoy.

   > Listá las requests con status 500 al path `/solicitudes/nueva`.

Internamente el agente invoca `list_logs` con filtros como `resource`
(ID del servicio), `level`, `text`, `statusCode`, `path`, `startTime`,
`endTime` y `limit`.

### Permisos

Por defecto Devin CLI pide aprobación antes de ejecutar tools de MCP. Para
consultas de solo lectura frecuentes se pueden autoaprobar en la configuración
de permisos (`permissions.allow`). **No** autoaprobar tools de escritura
(`update_environment_variables`, redeploys, creación de servicios).

---

## Resolución de problemas

| Síntoma | Causa probable | Acción |
|---|---|---|
| `Auth required` / 401 | API key inválida o variable de entorno no exportada | Verificar `echo $RENDER_API_KEY`; regenerar la key en Render |
| El agente pide workspace en cada prompt | No se seleccionó workspace | Pedir explícitamente "seleccioná el workspace X" |
| `list_logs` devuelve vacío | Filtro de tiempo o `resource` incorrecto | Pedir primero `list_services` y usar el ID exacto del servicio |
| El servidor no aparece en `devin mcp list` | Configurado en otro scope o versión vieja de Devin CLI (`config.json` en vez de `mcp_config.json`) | Revisar `--scope`; actualizar Devin CLI |
| Métricas no disponibles | Algunas métricas requieren plan Pro en Render | Limitarse a logs y estado de deploy |

---

## Alternativas sin MCP

- **Dashboard de Render → servicio → pestaña Logs** (manual).
- **Render CLI** (`render logs`) desde la terminal, con la misma API key.
- **API REST de Render** (`GET /v1/logs`) con `curl`.

El MCP agrega valor cuando se quiere que el agente correlacione logs con el
código del repo (ej. ubicar el controller que produjo una excepción) sin salir
de la terminal.
