/**
 * Componente para manejo de botones de cambio de idioma
 * Detecta automáticamente los botones y agrega tracking
 */
class LanguageSwitcher {
    constructor() {
        this.init();
    }

    init() {
        console.log('[LANG-SWITCHER] Inicializando componente');
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
                console.log(`[LANG-SWITCHER] Configurando botón: ${id}`, el);
                
                // Remover listeners previos si existen
                el.removeEventListener('click', this.handleLanguageClick);
                
                // Agregar nuevo listener
                el.addEventListener('click', (e) => {
                    console.log(`[LANG-SWITCHER] ¡CLICK INTERCEPTADO en ${id}!`);
                    this.handleLanguageClick(e, el);
                });
                
                buttonsConfigured++;
            } else {
                console.warn(`[LANG-SWITCHER] No se encontró elemento: ${id}`);
            }
        });
        
        console.log(`[LANG-SWITCHER] ${buttonsConfigured} botones configurados exitosamente`);
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

        console.log(`[LANG-SWITCHER] ¡CLICK DETECTADO! Idioma: ${lang}`);
        console.log(`[LANG-SWITCHER] Elemento:`, linkElement);
        console.log(`[LANG-SWITCHER] URL destino: ${href}`);

        // Enviar tracking al backend INMEDIATAMENTE
        event.preventDefault();
        
        try {
            await this.sendTrackingData(lang, href);
            console.log('[LANG-SWITCHER] Tracking enviado, navegando...');
        } catch (error) {
            console.error('[LANG-SWITCHER] Error en tracking:', error);
        } finally {
            // Navegar después del tracking
            window.location.href = href;
        }
    }

    async sendTrackingData(lang, url) {
        console.log('[LANG-SWITCHER] Preparando envío al backend...');
        
        const payload = {
            logs: [{
                level: 'info',
                message: `LANG_BTN_CLICK ${lang}`,
                timestamp: Date.now(),
                url: window.location.href,
                targetUrl: url
            }]
        };

        console.log('[LANG-SWITCHER] Payload preparado:', payload);

        // Usar fetch con await para garantizar envío
        try {
            console.log('[LANG-SWITCHER] Enviando al endpoint:', '/api/tracking/console-log');
            
            const response = await fetch('/api/tracking/console-log', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            
            console.log('[LANG-SWITCHER] ✅ Response status:', response.status);
            const responseText = await response.text();
            console.log('[LANG-SWITCHER] ✅ Backend respondió:', responseText);
            
        } catch (error) {
            console.error('[LANG-SWITCHER] ❌ Error en fetch:', error);
        }
    }
}

// Inicializar automáticamente
new LanguageSwitcher();
