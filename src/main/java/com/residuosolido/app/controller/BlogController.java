package com.residuosolido.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

/**
 * Blog estático con contenido informativo sobre recolectores informales,
 * organizaciones de acopio y el proyecto binacional.
 * El contenido es estático (hardcodeado) para el MVP; se documenta que
 * puede hacerse dinámico en el futuro (ver docs/HARDENING.md).
 */
@Controller
public class BlogController {

    private static final List<Map<String, String>> ARTICLES = List.of(
            Map.of(
                    "slug", "recolectores-informales",
                    "title", "Recolectores informales: el corazón invisible del reciclaje",
                    "date", "10/09/2026",
                    "excerpt", "En Rivera y Sant'Ana do Livramento, cientos de recolectores informales recorren las calles separando lo que otros descartan. Trabajan sin equipo, sin sueldo fijo y sin reconocimiento. Este artículo cuenta quiénes son y por qué importan.",
                    "icon", "fa-solid fa-users",
                    "tag", "Comunidad"
            ),
            Map.of(
                    "slug", "galpones-de-acopio",
                    "title", "Los galpones de acopio y el calor del verano",
                    "date", "10/09/2026",
                    "excerpt", "En verano, los galpones de acopio superan los 40°C. Sin ventilación ni sombra, las cooperativas de Rivera y Sant'Ana do Livramento trabajan en condiciones extremas. Conocé los desafíos que enfrentan cada día.",
                    "icon", "fa-solid fa-warehouse",
                    "tag", "Organizaciones"
            ),
            Map.of(
                    "slug", "frontera-de-la-paz",
                    "title", "Frontera de la Paz: dos ciudades, un mismo compromiso",
                    "date", "10/09/2026",
                    "excerpt", "Rivera (Uruguay) y Sant'Ana do Livramento (Brasil) forman un continuo urbano de 191 mil habitantes sin frontera física. La Agenda Urbana Binacional 2030 busca integrar la gestión de residuos en ambos lados.",
                    "icon", "fa-solid fa-handshake",
                    "tag", "Proyecto"
            )
    );

    private static final String ARTICLE_RECOLECTORES = """
            <p>En las calles de Rivera y Sant'Ana do Livramento, cientos de personas recorren
            los barrios separando cartón, plástico, metal y vidrio de lo que el resto descarta.
            Son los recolectores informales: trabajadores que sostienen el reciclaje real
            de la ciudad sin contrato, sin sueldo fijo y casi sin reconocimiento.</p>

            <p>Según el Movimiento Nacional de Catadores de Brasil, unas 800 mil personas
            trabajan en la recolección de reciclables en el país. En Uruguay, las cooperativas
            como Renacer del Norte en Rivera recuperan 700 kilos diarios de materiales.</p>

            <p>Su trabajo es esencial: reducen el volumen que llega a los vertederos,
            limpian los barrios y generan ingresos para sus familias. Pero enfrentan
            condiciones precarias: sin guantes, sin transporte, sin galpón propio y
            expuestos al clima extremo.</p>

            <p>Este proyecto los reconoce como parte fundamental del sistema. La plataforma
            permite que las organizaciones de acopio los registren y coordinen, pero la
            gestión completa de recolectores queda documentada como mejora futura.</p>

            <p><strong>Fuentes:</strong> WIEGO (2026), Movimiento Nacional de Catadores (MNCR),
            Intendencia de Rivera, CEMPRE Uruguay.</p>
            """;

    private static final String ARTICLE_GALPONES = """
            <p>Cuando el termómetro supera los 40°C en verano, los galpones de acopio se
            convierten en lugares casi inhabitables. La mayoría son estructuras con techo
            de zinc que absorben y amplifican el calor. Sin ventiladores ni extractores,
            el ambiente se vuelve sofocante para quienes trabajan ahí dentro.</p>

            <p>Un estudio de WIEGO con cooperativas de seis ciudades brasileñas encontró que
            el 85% de los recolectores sufrió ondas de calor anormales en el último año.
            El calor reduce la productividad, agrava problemas de salud y obliga a
            interrumpir las actividades por horas.</p>

            <p>En Rivera, la Cooperativa Renacer del Norte opera con un camión utilitario y
            24 clasificadores. En Sant'Ana do Livramento, las cooperativas enfrentan
            además la competencia de empresas brasileñas que exportan reciclables a
            mejores precios, reduciendo el material disponible para los trabajadores locales.</p>

            <p>Las lluvias intensas son otro desafío: cuando los galpones se inundan, no solo
            se pierde material reciclable, también se pierde ingresos. La mayoría de las
            cooperativas funciona en estructuras frágiles, sin protección contra el clima.</p>

            <p><strong>Fuentes:</strong> WIEGO (2026), Atlas da Reciclagem 2024 (Ancat),
            Diario NORTE (Rivera), Búsqueda (Uruguay).</p>
            """;

    private static final String ARTICLE_FRONTERA = """
            <p>Rivera, en Uruguay, y Sant'Ana do Livramento, en Brasil, son dos ciudades que
            forman un solo continuo urbano. No hay frontera física entre ellas: una plaza
            binacional, la Plaza Internacional, las une y divide al mismo tiempo. Juntas
            suman más de 191 mil habitantes.</p>

            <p>La Unión Europea, a través del Eixo Atlântico del Noroeste Peninsular, aprobó
            en 2020 el proyecto "Frontera de la Paz Sostenible" con una inversión de 1,1
            millones de euros. El objetivo: integrar la gestión de residuos entre ambas
            ciudades, promover la economía circular y la inclusión social.</p>

            <p>En diciembre de 2021 se lanzó la Agenda Urbana Binacional, primera de su tipo
            en el Mercosur. El Plan de Acción 2030 busca reducir las desigualdades en
            recolección de residuos entre los dos lados de la frontera y profesionalizar
            el trabajo de los recolectores informales.</p>

            <p>Este proyecto, Residuo Sólido, nace en ese contexto. Conecta a ciudadanos y
            cooperativas de ambos lados para que los reciclables tengan un mejor destino.
            Es un MVP: una primera versión que demuestra que la tecnología puede acercar
            a la comunidad con quienes reciclan.</p>

            <p><strong>Fuentes:</strong> Eixo Atlântico, Intendencia de Rivera,
            Prefeitura de Sant'Ana do Livramento, Unión Europea (DEVCO).</p>
            """;

    @GetMapping("/blog")
    public String blogIndex(Model model) {
        model.addAttribute("articles", ARTICLES);
        model.addAttribute("breadcrumbs", List.of(
                Map.of("label", "Inicio", "href", "/"),
                Map.of("label", "Blog", "href", "")
        ));
        return "public/blog";
    }

    @GetMapping("/blog/{slug}")
    public String blogArticle(@PathVariable String slug, Model model) {
        Map<String, String> article = ARTICLES.stream()
                .filter(a -> a.get("slug").equals(slug))
                .findFirst()
                .orElse(null);
        if (article == null) {
            return "redirect:/blog";
        }
        String content = switch (slug) {
            case "recolectores-informales" -> ARTICLE_RECOLECTORES;
            case "galpones-de-acopio" -> ARTICLE_GALPONES;
            case "frontera-de-la-paz" -> ARTICLE_FRONTERA;
            default -> "";
        };
        model.addAttribute("article", article);
        model.addAttribute("content", content);
        model.addAttribute("breadcrumbs", List.of(
                Map.of("label", "Inicio", "href", "/"),
                Map.of("label", "Blog", "href", "/blog"),
                Map.of("label", article.get("title"), "href", "")
        ));
        return "public/blog-article";
    }
}
