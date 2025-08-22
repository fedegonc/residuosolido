/**
 * Componente para manejo de botones de cambio de idioma
 * Detecta automáticamente los botones y agrega tracking
 */
class LanguageSwitcher {
    constructor() {
        this.init();
    }

    init() {
        document.addEventListener('DOMContentLoaded', () => {
            this.setupLanguageButtons();
        });
    }

    setupLanguageButtons() {
        // Solo buscar por IDs específicos para evitar duplicados
        const specificIds = ['lang-es-link', 'lang-pt-link'];
        let buttonsConfigured = 0;
        
        specificIds.forEach(id => {
            const el = document.getElementById(id);
            if (el) {
                // Remover listeners previos si existen
                el.removeEventListener('click', this.handleLanguageClick);
                
                // Agregar nuevo listener
                el.addEventListener('click', (e) => {
                    this.handleLanguageClick(e, el);
                });
                
                buttonsConfigured++;
            }
        });
    }

    async handleLanguageClick(event, linkElement) {
        // Extraer idioma del href o data-lang
        const href = linkElement.getAttribute('href') || '';
        const dataLang = linkElement.getAttribute('data-lang');
        
        let lang = dataLang;
        if (!lang) {
            // Extraer de URL: /change-language?lang=es
            const match = href.match(/[?&]lang=([^&]+)/);
            lang = match ? match[1] : 'unknown';
        }


        // Enviar tracking al backend INMEDIATAMENTE
        event.preventDefault();
        
        // Asegurar que se envía un referer relativo seguro al backend
        let finalHref = href;
        try {
            const urlObj = new URL(href, window.location.origin);
            if (!urlObj.searchParams.has('referer')) {
                const safeReferer = window.location.pathname + window.location.search;
                urlObj.searchParams.set('referer', safeReferer);
            }
            // Usar ruta relativa + query para navegación
            finalHref = urlObj.pathname + (urlObj.search || '');
        } catch (e) {
        }
        
        try {
            // Fire-and-forget: no bloquear navegación
            this.sendTrackingData(lang, finalHref);
        } catch (error) {
            // ignorar errores de tracking
        }
        // Navegar inmediatamente
        window.location.href = finalHref;
    }

    sendTrackingData(lang, url) {
        const payload = {
            logs: [{
                level: 'info',
                message: `LANG_BTN_CLICK ${lang}`,
                timestamp: Date.now(),
                url: window.location.href,
                targetUrl: url
            }]
        };
        const payloadStr = JSON.stringify(payload);

        try {
            if (navigator.sendBeacon) {
                const blob = new Blob([payloadStr], { type: 'application/json' });
                navigator.sendBeacon('/api/tracking/console-log', blob);
            } else {
                // Fallback con keepalive para permitir envío en navegación
                fetch('/api/tracking/console-log', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: payloadStr,
                    keepalive: true
                }).catch(() => {});
            }
        } catch (_) {
            // Silent fail
        }
    }
}

// Inicializar automáticamente
new LanguageSwitcher();

