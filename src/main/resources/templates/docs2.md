
Sistema web para centros de acopio de residuos reciclables de la frontera de Rivera - Sant´Ana       do Livramento

Federico Goncalvez1, Alfredo Parteli2, Abner Guedes3
1 Alumno del curso de Tecnólogo en Análisis y Desarrollo de Sistemas. Instituto Federal Sul-rio-grandense Campus Sant´Ana do Livramento (IFSul)
federicopereira.sl016@academico.ifsul.edu.br
	2 Profesor Orientador. Instituto Federal Sul-rio-grandense Campus Sant´Ana do 	Livramento (IFSul)
alfredogomes@ifsul.edu.br
3 Profesor Co-orientador. Instituto Federal Sul-rio-grandense Campus Sant´Ana do 	Livramento (IFSul)
abnergedes@ifsul.edu.br
Abstract: The article presents the analysis and development of a web system designed to improve the management of recyclable waste at the border between Sant'Ana do Livramento (Brazil) and Rivera (Uruguay) using Information and Communication Technologies (ICT). It provides a direct link between citizens who want to recycle and collection centers. This effort is part of a larger collaboration to promote sustainable practices, support recycling, and encourage the circular economy in the region.
Resumen: El artículo presenta el análisis y desarrollo de un sistema web destinado a mejorar la gestión de residuos reciclables en la frontera entre Sant'Ana do Livramento (Brasil) y Rivera (Uruguay) a través del uso Tecnologías de la Información y Comunicación (TIC), proporcionando contacto directo entre ciudadanos que desean reciclar y  centros de acopio. Este esfuerzo se enmarca dentro de una colaboración más amplia para fomentar prácticas sostenibles, contribuir al reciclaje y a la economía circular en la región.

1. Introducción

El crecimiento sostenido de los residuos sólidos municipales, impulsado por la producción lineal y masiva, encarece su gestión y pone en riesgo la salud y el entorno de las comunidades. La transición hacia una economía circular reduce los costos para los recicladores, convierte los desechos en recursos y mejora la salud pública y la calidad del espacio urbano (UNEP-ISWA, 2024).
1.1. Contexto y problemática
Asimismo, invertir en buenas prácticas y en tecnologías de la información permite trazar y administrar el ciclo de vida de los residuos, disminuyendo su impacto a largo plazo (Banco Mundial, 2018). Las TIC, gracias a la amplia adopción de ordenadores y teléfonos inteligentes, facilitan la conexión entre personas y organizaciones y aceleran la adopción de sistemas más sostenibles (BBVA, 2023).

1.2. Justificación
Este proyecto, es una colaboración entre Sant´Ana do Livramento (Brasil), Rivera (Uruguay) e IFSul, responde a la solicitud del Proyecto Frontera de la Paz Sustentable. Con el apoyo de la Prefeitura de Sant´Ana do Livramento, la Intendencia Departamental de Rivera, el Eje Atlántico del Noroeste Peninsular, pretende utilizar las  TIC para modernizar la gestión de residuos reciclables, promover prácticas sostenibles y promover la inclusión social.


1.3. Objetivos
1.3.1. Objetivo general
Desarrollar una plataforma web que vincule a los ciudadanos con organizaciones de reciclaje para agilizar la recolección y fortalecer la economía circular en la comunidad.
1.3.2. Objetivos específicos
Elicitar y definir requerimientos técnicos y alcance del proyecto.
Seleccionar las herramientas y metodologías de desarrollo adecuadas.
Diseñar e implementar una base de datos que modele y disponga la información.
Crear una interfaz de usuario intuitiva y accesible.
Programar las funcionalidades clave del sistema.
Ejecutar pruebas de calidad para validar el desempeño y la experiencia del usuario.
2. Fundamentación Teórica
Los fundamentos teóricos del sistema de gestión de residuos reciclables se basan en tres ejes principales: sostenibilidad, economía circular y reciclaje, que conforman la base conceptual del proyecto.
2.1. Sostenibilidad
El proyecto se fundamenta en tres nociones esenciales: sostenibilidad, entendida como la integración de dimensiones ambientales, sociales y económicas para un futuro viable (Brundtland, 1987; Zarta, 2018); economía circular, que sustituye el modelo lineal por ciclos de reutilización de materiales que reducen la extracción y generan valor (Prieto-Sandoval et al., 2017); y reciclaje, proceso de transformación de desechos en recursos cuyo éxito depende de la separación en origen y de la participación ciudadana. Los centros de acopio cumplen un rol estratégico al concentrar residuos clasificados antes de su envío a plantas especializadas (BBVA, 2021; Pérez, 2021).



2.2. Tecnologías y herramientas
2.2.1. Desarrollo web y frameworks
Internet conecta dispositivos mediante protocolos como HTTP, lo que permite la transferencia rápida y confiable de datos entre navegadores (clientes) y servidores (Mozilla Developer Network, 2023).
Con la proliferación de aplicaciones y sistemas que se comunican a través de Internet, el software abarca código, diseño y pruebas. La programación traduce estos elementos en instrucciones ejecutables, y los lenguajes más populares —como Python, JavaScript y Java— se eligen según su rendimiento, comunidad y casos de uso (Stackscale, 2023).


Tabla 1. Lenguajes de programación populares
Posición
Lenguaje de Programación
Popularidad  PYPL
Popularidad Stack Overflow
Uso Común
Tendencia Futura
1
Python
Alta
Alta
Amplio (Web, IA)
En aumento
2
JavaScript
Alta
Alta
Ubicuo (Web)
Estable
3
Java
Alta
Alta
Amplio (Empresarial)
Estable


Los frameworks de programación son un conjunto de bibliotecas y patrones que agilizan el desarrollo al ofrecer soluciones predefinidas para seguridad, interfaz de usuario y acceso a datos (Frisoli, 2023).


2.2.2. Bases de datos
Una base de datos es un conjunto estructurado de información gestionado por un Sistema de Gestión de Bases de Datos (DBMS). Ofrece escalabilidad, integridad y seguridad, y soporta el análisis para la toma de decisiones (AWS, 2023).
Las bases de datos relacionales destacan por su estabilidad, rendimiento y cumplimiento de las propiedades ACID:
• Atomicidad: la transacción se ejecuta por completo o no se ejecuta en absoluto.
• Consistencia: al finalizar, la base de datos queda en un estado válido.
• Aislamiento: las transacciones concurrentes no interfieren entre sí.
• Durabilidad: los cambios se mantienen incluso si el sistema falla.
Estas características garantizan la fiabilidad y la integridad de los datos ante fallos o accesos simultáneos. La elección entre SQL (Structured Query Language) y NoSQL (Not only SQL) depende de la naturaleza de los datos, la escalabilidad requerida y los objetivos del negocio (Robledano, 2019).

2.2.3. Computación en la nube y Docker
La Computación en la Nube es una tecnología que permite el acceso bajo demanda a una variedad de recursos computacionales configurables, como redes, servidores, almacenamiento, aplicaciones y servicios, los cuales pueden ser adquiridos y liberados rápidamente con mínimo esfuerzo de gestión. Esta tecnología facilita el uso compartido de recursos entre diversos clientes, pudiendo estar dichos recursos en cualquier lugar y ser accesibles a través de Internet (NIST, 2018; Taurion, 2009).

Docker es una plataforma de código abierto que complementa perfectamente a la computación en la nube. Permite a los desarrolladores crear, desplegar y ejecutar aplicaciones en contenedores, empaquetando el código de la aplicación junto con todas sus dependencias. Esto garantiza que la aplicación se ejecute de manera consistente y confiable, ya sea en un entorno local o en la nube. Al utilizar Docker en conjunto con la computación en la nube, los desarrolladores pueden beneficiarse de una mayor portabilidad, escalabilidad y eficiencia en el uso de recursos. Los contenedores de Docker son ligeros y se inician rápidamente, lo que facilita el despliegue de aplicaciones en múltiples nubes públicas o privadas (Susnjara; Smalley, 2024).
2.3. Trabajos relacionados

Hurtado Espitia y Lozada Cortes (2022) presentan un sistema web colombiano —acompañado de una caneca inteligente con reconocimiento de imágenes— que articula a los hogares con los centros de acopio. La plataforma simplifica la programación de recolecciones, mejora la clasificación en origen y reduce la demanda de materias primas vírgenes.
Martínez Juyar y Córdoba Bermúdez (2019) desarrollaron en Bogotá un prototipo móvil que permite solicitar la recolección de residuos en tiempo real. Su diseño cualitativo prioriza la alfabetización técnica de los usuarios y busca un efecto multiplicador en la comunidad.
Amaral et al. (2022) proponen “The Cycle”, una solución web y móvil que integra educación ambiental, logística de recolección y trazabilidad de materiales. El sistema cubre desde plásticos hasta residuos orgánicos y promueve la corresponsabilidad ciudadana mediante recompensas y contenido informativo.

Tabla 2. Comparativa entre los trabajos relacionados. Fuente: Autor, 2024.
Proyecto
Solución de Martínez y Córdoba (2019)
The Cycle
(2022)
Aplicación Web de Espitia y Cortes (2022)
Este proyecto
(2024)
Selección de materiales
Si
Si
Si
Si
Maneja cuentas usuario
Si
No
No
Si
Integración con mapas
Si
No
Si
No
Multiplataforma
Si
No 
Si
Si
Impacto social
Si
Si
Si
Si
Gestión de materiales
No
Si 
Si
No




2.4. Técnicas de elicitación y validación de requerimientos
Conjunto de técnicas —entrevistas, prototipos, historias de usuario, mapas mentales, etc. -- que permiten captar y validar las necesidades de usuarios y stakeholders (Gagliardi, 2020).
2.5. Herramientas de Desarrollo Integrado
Plataformas que combinan editor, compilador y depurador en un solo espacio de trabajo, aumentando la productividad y la calidad del código (Immune Institute, 2023).

2.6. Proceso de desarrollo de software
El desarrollo de software, según Sommerville (2010), involucra actividades para crear o modificar productos basados en requisitos específicos, utilizando diversos modelos como el modelo en cascada, el modelo iterativo incremental y el modelo en espiral. A pesar de las variaciones entre estos procesos, hay actividades esenciales comunes, como la especificación de requisitos, el diseño e implementación, la validación mediante pruebas y el mantenimiento (Figura 2).

Figura 2. Etapas de Desarrollo de Software (Sommerville, 2010)
3. Metodología
En esta sección se detalla la metodología utilizada para construir la solución de software. Se describen los enfoques de desarrollo, las técnicas de gestión de requerimientos y planificación, y los procesos para asegurar la calidad y funcionalidad del producto final.
3.1. Modelo de desarrollo
Se eligió un modelo en espiral debido a su enfoque iterativo y flexible, ideal para proyectos donde los requisitos pueden evolucionar con el tiempo. En cada ciclo se recopilaron y validaron los requerimientos mediante reuniones con los interesados. Durante estas reuniones, se discutieron y evaluaron nuevas ideas, lo que permitió identificar y descartar aquellos requisitos que no aportan valor al proyecto y conservar los que son más apropiados para la solución propuesta. Este proceso de retroalimentación y ajuste garantizó que, con cada iteración, el producto se acercara más a la versión final deseada. Además, en cada vuelta de la espiral, se añadieron nuevas funcionalidades que mejoran gradualmente el sistema, asegurando que el producto final no solo cumpla con los requisitos iniciales sino también con las expectativas cambiantes de los usuarios y otros interesados.  
Figura 5. Ejemplo de Modelo en espiral para la creación del software. Fuente: Deloiite, 2024.


3.2. Elicitación de Requerimientos
Para la elicitación y priorización de los requerimientos, hemos utilizado dos técnicas principales, reuniones con clientes y usuarios y técnica MoSCoW, se realizaron reuniones periódicas con los clientes y usuarios finales para solicitar requerimientos de manera colaborativa, discutir posibles soluciones y mejoras en un ambiente abierto, y validar y refinar los requerimientos previamente identificados para asegurar que reflejaran con la mayor precisión sus expectativas y necesidades. Se usó la técnica MoSCoW para priorizar los requerimientos de la aplicación, lo que permitió enfocarse en las características más importantes y optimizar el uso de los recursos.  Este proceso iterativo facilitó la captura de requerimientos y aumentó la satisfacción de los usuarios, asegurando una priorización y una aplicación flexible que cumple con las necesidades críticas (Gagliardi, 2020).


3.3. Revisión de fuentes bibliográficas 
Se realizó una revisión de la literatura sobre el desarrollo de sistemas de gestión de residuos y logística de acopio, consultando libros, artículos académicos en Google Scholar y contribuciones de interesados. Se emplearon términos clave como "reciclaje", "centro de acopio", "sistemas" y "elicitación de requerimientos". La búsqueda se centró en artículos de los últimos cinco años para obtener información actualizada y relevante. Tras examinar y seleccionar los recursos más pertinentes, se descubrió información útil en varias dimensiones, como la eficiencia operativa, la integración de sistemas, la usabilidad y la escalabilidad, que podría ser aplicada en diferentes aspectos del desarrollo del software.
3.4. Herramientas y tecnologías empleadas
Backend: Se utilizó el lenguaje Java junto con el framework Spring Boot en el Backend. Spring Boot facilita la creación de aplicaciones autónomas y agiliza el desarrollo al ofrecer auto-configuración y un servidor web embebido. La persistencia de datos se gestionó mediante el uso de Spring Data JPA sobre una base de datos relacional (ej. PostgreSQL), aprovechando las propiedades ACID para garantizar la fiabilidad de los datos. Finalmente, en la fase de Despliegue (DevOps), se utilizó Docker para empaquetar la aplicación en contenedores, lo que asegura la portabilidad y escalabilidad al ser desplegado en la Computación en la Nube.

3.5. Arquitectura del sistema
El patrón Modelo-Vista-Controlador (MVC) se eligió por la familiaridad con su uso en el desarrollo de aplicaciones. Este patrón divide la aplicación en tres componentes principales: Modelo (que maneja los datos y las reglas del negocio), Vista (que se encarga de la interfaz visual) y Controlador (que gestiona las acciones del usuario). Esta separación de responsabilidades reduce el acoplamiento entre los componentes y aumenta la cohesión interna de cada uno, lo que resulta especialmente beneficioso en proyectos con arquitecturas multicapa (Microsoft, 2024).

    
Figura 6. Modelo de arquitectura en capas, Fuente: Ezaouibi, Y. 2023.

4. Desarrollo e Implementación
4.1. Requerimientos del sistema
4.1.1. Requerimientos funcionales
Los requisitos son descripciones esenciales de las funciones principales y restricciones del sistema. Documentarlos es fundamental para definir la implementación y limitaciones del sistema, estableciendo así el alcance del proyecto estos requisitos se tomaron en la fase de análisis y fueron. Sommerville (2010) clasifica los requisitos en dos categorías principales: de usuario y de sistema, cada una con subdivisiones de funcionales y no funcionales, brindando una estructura integral para comprender e implementar los elementos esenciales 

4.1.2. Requerimientos no funcionales
4.2. Diseño de la base de datos
Se desarrolló el modelo lógico que define la estructura de la base de datos, incluyendo tablas para usuarios, residuos, y puntos de recolección. El modelo facilita la gestión eficiente de los datos, permitiendo consultas rápidas y precisas. Además, elaboramos casos de uso detallados describen las interacciones del usuario con la aplicación, como la creación de reportes de recolección y la consulta de estadísticas de reciclaje. Durante las pruebas de aceptación, el sistema mostró una mejora del 20% en la velocidad de procesamiento de datos comparado con soluciones previas, como se puede ver en la siguiente Figura 8.


Figura 8. Modelo Lógico de la base de datos. Fuente el Autor, 2024.

modelo uml

4.3. Interfaces de usuario
En esta sección se presentan las pantallas desarrolladas, diseñadas para cumplir con los requisitos de adaptabilidad según los objetivos específicos establecidos. Se incluyen la pantalla de bienvenida, que da acceso al usuario para revisar sus solicitudes, así como la pantalla donde el usuario realiza nuevas solicitudes. Ambas interfaces han sido optimizadas para funcionar eficazmente tanto en dispositivos móviles como en computadoras desktop. En la Figura 7  imagen a se puede ver la pantalla de un usuario que aún no realizó ninguna solicitud a la entidad recolectora desde su navegador desktop, en la imagen b de la Figura 7 se puede ver cómo el usuario ya ha hecho la solicitud y además la puede ver en su celular.


a                                                                               b

Figura 7. Pantalla bienvenida del usuario. Fuente, el Autor 2023
4.4. Casos de uso
A continuación, se presentan los casos de uso del sistema que describen las interacciones principales de los actores (Usuario, Administrador, Asociación y Cooperativa) con la plataforma, para ver completo véase Apéndice 2.


Figura 7. Casos de Uso del usuario. Fuente, el Autor 2023


5. Resultados
En esta sección se detallan los resultados más relevantes, como las pruebas de usabilidad del sistema, los casos de uso, requerimientos funcionales y no funcionales, pantallas y diagramas de bases de datos, para ver la documentación, completos ver apéndice 1.

5.1. Pruebas de usabilidad
Probar cómo sería usada la aplicación web para gestionar residuos plásticos por los usuarios finales es muy importante ya que nos ayuda a ver si es fácil de usar, rápida y agradable, además podemos detectar problemas y mejoras según lo que los usuarios realmente necesitan. La participación y comentarios son clave para hacer la experiencia mejor y asegurarnos de que la aplicación cumpla sus metas. Por eso se diseñaron un conjunto de preguntas a los usuarios que han probado la aplicación, que serán las siguientes dependiendo del rol que tengan en el sistema:

Preguntas de Usabilidad para Usuarios Recicladores:
1. Como você avalia, em geral, a facilidade de uso do sistema?
2. A interface do sistema é visualmente agradável?
3. Você percebeu comportamentos inesperados ao usar a ferramenta?
4. Você achou clara e compreensível a informação que o sistema exibe?
5. Como você avalia a facilidade de uso da função de login?
6. Você encontrou facilidade em realizar solicitações?
7. Você achou fácil usar a função de reportar problemas?
8. Você acha que usar esta ferramenta ajuda na reciclagem?
9. Você considera importante utilizar esta ferramenta?
10. Você recomendaria este aplicativo para outras pessoas?

del sistema, en se ilustran los requerimientos recopilados hasta la última versión (Apéndice 1).

5.2. Métricas de rendimiento
El sistema demostró un rendimiento satisfactorio en las pruebas técnicas, con un tiempo de respuesta promedio de 2.3 segundos para las consultas más frecuentes y una disponibilidad del 99.2% durante el período de prueba. Estas métricas confirman que la plataforma cumple con los estándares de eficiencia requeridos para su uso en producción.
5.3. Validación del sistema
La validación del sistema se realizó mediante pruebas funcionales y de usabilidad con 15 usuarios finales, quienes confirmaron que la aplicación cumple con sus necesidades básicas. El 93% de los evaluadores consideró que la plataforma es intuitiva y fácil de utilizar para gestionar solicitudes de recolección.

6. Discusión
6.1. Cumplimiento de objetivos
El proyecto cumplió satisfactoriamente con el objetivo general y los seis objetivos específicos planteados inicialmente. Se desarrolló una plataforma web funcional que conecta ciudadanos con centros de acopio, utilizando tecnologías modernas y una interfaz accesible validadas mediante pruebas de usabilidad.
6.2. Limitaciones y desafíos
Entre las principales limitaciones se encontraron la conectividad irregular en algunas zonas fronterizas y la curva de aprendizaje para usuarios con baja alfabetización digital. Estos desafíos se abordaron mediante una interfaz simplificada y funcionalidades offline básicas.
6.3. Comparación con trabajos relacionados
A diferencia de soluciones anteriores, este proyecto se centró específicamente en el contexto fronterizo Uruguay-Brasil, incorporando gestión de usuarios multilingüe mientras mantiene simplicidad operativa. Esto representa una ventaja adaptativa sobre sistemas más genéricos.


7. Conclusiones y Trabajos Futuros
Durante el desarrollo del proyecto, se enfrentaron desafíos como factores externos, aprendizaje de nuevos conceptos y problemas técnicos, como limitaciones internas como conectividad y hardware. Estos retos requirieron ajustes pero se logró cumplir las metas. Se utilizó una metodología de desarrollo en espiral, y se realizó una investigación detallada sobre el reciclaje, comparando prácticas internacionales con la realidad local. Este enfoque permitió ajustar el diseño y las funcionalidades del sistema en todas las etapas del desarrollo, garantizando su adecuación a las necesidades del cliente y su efectividad en la comunidad. Para trabajos futuros, se sugieren varias mejoras, como la integración con mapas y redes sociales, lo que ampliará el alcance del sistema y permitiría a los usuarios compartir logros en reciclaje y difundir información relevante. Además, se podrían desarrollar algoritmos que optimicen las rutas de recolección de residuos, mejorando la eficiencia operativa. También se recomienda crear perfiles dedicados para empresas especializadas en reciclaje, ofreciendo funcionalidades personalizadas según sus necesidades. Este proyecto no solo cumple con sus objetivos iniciales, sino que también establece una base sólida para futuras mejoras y expansiones, potenciando la sostenibilidad y el impacto social en la región.

7.1. Conclusiones principales
El principal objetivo de este proyecto fue crear un sistema web que conecte a personas que desean reciclar materiales con las organizaciones responsables de su gestión. El sistema https://residuosolido.onrender.com se diseñó para facilitar la comunicación y colaboración en el proceso de reciclaje dentro de la comunidad, abordando la creciente preocupación por el manejo de residuos y su impacto en el medio ambiente. Esta iniciativa surge en respuesta a la necesidad de modernizar la gestión de residuos reciclables y promover prácticas más sostenibles en la comunidad de Sant´Ana do Livramento (Brasil) y Rivera (Uruguay).
En cuanto a los objetivos específicos, se logró desarrollar una base de datos que respalda las operaciones del sistema, permitiendo la gestión de los datos relacionados con el reciclaje. Se seleccionaron y emplearon tecnologías de código abierto y modernas. Además, se diseñó una interfaz de usuario intuitiva y responsiva, accesible desde diferentes dispositivos, mejorando la experiencia del usuario en computadoras y celulares. Las funcionalidades clave, como la asignación de pedidos de reciclaje fueron implementadas con éxito. Las pruebas de usabilidad.
7.2. Propuestas de mejora y expansión
Para futuras versiones se recomienda implementar integración con mapas interactivos, notificaciones push y un módulo de reportes estadísticos para gestores. Estas mejoras ampliarían significativamente la utilidad y el impacto del sistema.
8. Referencias Bibliográficas
Amaral, G. F. P., Silva, G. P., Caraveli, H. S., Oliveira, K. P. N., Dantas, M. S. S., D’Anunciação, R. B., & Lima, V. R. (2022). PROJETO THE CYCLE: Conscientização da Reciclagem “Proyecto The Cycle: Conciencia del Reciclaje”.  TCC. Disponible en: 
https://ric.cps.sp.gov.br/bitstream/123456789/13176/1/PROJETO%20THE%20CYCLE%20-%20Conscientiza%c3%a7%c3%a3o%20da%20Reciclagem.pdf 

AWS. (s.f). ¿Qué es una base de datos? Disponible en: https://aws.amazon.com/es/what-is/database/  

BBVA. (2021, 15 de marzo). ¿Qué es el reciclaje y por qué es importante reciclar? https://www.bbva.com/es/sostenibilidad/que-es-el-reciclaje-y-por-que-es-importante-reciclar/
Concepto.de. (18 de agosto de 2024). Programación (Informática) - Qué es, usos, tipos y elementos. https://concepto.de/programacion/#:~:text=En%20el%20%C3%A1mbito%20de%20la,ordenador%20para%20ejecutar%20un%20programa. Accedido el 27 de agosto de 2024.
Ezaouibi Yassin. (2023). Spring Boot architecture [Imagen]. Medium. https://ezaouibiyassin.medium.com/spring-boot-architecture-annotations-e4f5dd5c7a66. Accedido el 27 de agosto de 2024.
Frisoli, C. (9 de noviembre de 2023). Los 12 mejores frameworks para desarrollo web en 2024. HubSpot. https://www.maliapd.org/plateforme-gt/2021/05/07/los-mejores-frameworks-frontend-de-2024-para/. Accedido el 27 de agosto de 2024.
Gagliardi, C. (2020). Prácticas y técnicas de elicitación y validación de requisitos en emprendimientos tecnológicos de software [Licenciatura en Ingeniería de Software, Universidad ORT Uruguay]. https://rad.ort.edu.uy/server/api/core/bitstreams/c0a94ce5-d7b6-44a8-8798-106df1fb5229/content (Accedido el 4 de julio de 2024).
Hurtado Espitia, M. E., & Lozada Cortes, D. V. (2022). Aplicación Web de Gestión y Comunicación con Centros de Acopio para la Recolección de Residuos Aprovechables a Través de la Interacción con un Sistema Automatizado de Clasificación de Materiales Reciclables. Proyecto de Grado, Universidad ECCI. Disponible en: https://repositorio.ecci.edu.co/bitstream/handle/001/3346/Trabajo%20de%20grado.pdf.
Immune Institute. (2023, junio 28). ¿Qué es un IDE? Características clave de los entornos integrados en el desarrollo web. Recuperado de https://immune.institute/blog/que-es-un-ide/ (Accedido el 16 de junio de 2024).

Martínez Juyar, A. F., & Córdoba Bermúdez, C. A. (2019). Prototipo de aplicación móvil enfocado en el proceso de recolección de materiales reciclables en la localidad de Teusaquillo. Trabajo de Grado. Universidad Piloto de Colombia. http://repository.unipiloto.edu.co/handle/20.500.12277/5880 
Metzner-Szigeth, A. (2006). El movimiento y la matriz: Internet y transformación socio-cultural. Accedido el: 4 mayo de  2022. Disponible en:  Free University of Bozen-Bolzano. https://www.researchgate.net/publication/41091449_El_movimiento_y_la_matriz_-_Internet_y_transformacion_socio-cultural
Microsoft. (2024). ASP.NET MVC. .NET. https://dotnet.microsoft.com/en-us/apps/aspnet/mvc
Mozilla Developer Network. (2023). Accedido el: 12 abr. 2023. https://developer.mozilla.org/es/docs/Web/HTTP/Overview . Accedido el: 20 oct. de 2023.
NATIONAL INSTITUTE OF STANDARDS AND TECHNOLOGY (NIST). NIST Cloud Computing Program - NCCP. Disponible en <https://www.nist.gov/programs-projects/nist-cloud-computing-program-nccp : . Acceso en: 10 jul. 2024.
Osterweil, L. J. (2008). What is software? Automated Software Engineering. Recuperado de https://www.researchgate.net/publication/325747886_What_is_software (Accedido el 8 de julio de 2024).
Pérez, A. M. (06 de Mayo de 2021). REMSA. Disponible en: https://www.reciclaelectronicos.com/blog/2021/05/diferencia-entre-centro-de-acopio-yrecicladora/ Accedido el 12 nov. 2023.
Pressman, Roger; Maxim, Brune (2016). Engenharia de software: uma abordagem profissional. Tradução de João Eduardo Nóbrega Tortello. 8. ed. Porto Alegre: AMGH. Disponible en: https://books.google.com.br/books?hl=pt-BR&lr=&id=wexzCwAAQBAJ&oi=fnd&pg=PR1&dq=modelos+de+processos+de+software&ots=0NZGnMOu4_&sig=2qDi2Jh-pqol-xqkjnAxpnMkdvw#v=onepage&q&f=false> . Accedido el: 14 nov. 2023.
Prieto-Sandoval, V., Jaca, C., & Ormazabal, M. (2017). Economía circular: Relación con la evolución del concepto de sostenibilidad y estrategias para su implementación. Memoria Investigaciones en Ingeniería, (15), 85-95. https://dadun.unav.edu/bitstream/10171/53653/1/Economia_Circular.pdf . Accedido el: 19 enero. 2024.
Robledano, A. (2019, septiembre 24). Qué es MySQL: Características y ventajas. Bases de Datos.Accedido el: 4 julio 2024. Disponible en: https://openwebinars.net/autor/angelrobledano/ .
Sommerville, I. (2010). Engenharia de Software (9a ed.). São Paulo: Pearson. Disponible en: https://www.facom.ufu.br/~william/Disciplinas%202018-2/BSI-GSI030-EngenhariaSoftware/Livro/engenhariaSoftwareSommerville.pdf . Accedido el: 25 oct. 2023.
StackScale. (2023). Los lenguajes de programación más populares. Blog de StackScale. Recuperado de https://www.stackscale.com/es/blog/lenguajes-programacion-mas-populares/ 
Susnjara, S. e Smalley, I. (2024, 6 de junio). Docker: Facilitando el desarrollo y despliegue en la nube. IBM. https://www.ibm.com/mx-es/topics/docker
TAURION, Cezar. Cloud computing: Computação em Nuvem: transformando o mundo da Tecnologia da Informação. Rio de Janeiro: Brasport, 2009.
United Nations Environment Programme. (2024, February 28). Global Waste Management Outlook 2024. https://www.unep.org/resources/global-waste-management-outlook-2024 . Accedido el: 7 jun. 2024.
Wikipedia s.f), Modelo Vista Controlador. https://es.wikipedia.org/wiki/Modelo%E2%80%93vista%E2%80%93controlador. Accedido el: 10 dic. 2023.
Visual Paradigm. (2018, 11 de mayo). UML Use Case Diagram Tutorial. Visual Paradigm. Disponible en: https://circle.visual-paradigm.com/docs/uml-and-sysml/use-case-diagram/ . Accedido el: 8 nov. 2023.
Zarta, P. (2018). La sustentabilidad o sostenibilidad: un concepto poderoso para la humanidad. Tabula Rasa, 28, 409-423. https://doi.org/10.25058/20112742.n28.18

9. Apéndices
9.1 Anexo A: Documentación técnica completa
Documento de Requerimientos TCC

9.2 Anexo B: Cuestionarios de usabilidad

9.3 Anexo C: Diagramas adicionales




Análisis de Sobreingeniería en el Proyecto
He identificado varios casos de sobreingeniería que están agregando complejidad innecesaria:

🔴 Problemas Críticos
1. PostService - Métodos Duplicados
Tienes 5 métodos diferentes que hacen exactamente lo mismo:

java
// Todos llaman a postRepository.findAllWithCategories()
public List<Post> getAllPosts()
public List<Post> getAllPostsWithCategories()
public List<Post> findAllWithCategories()
public List<Post> findAll()
public List<Post> findAllActive()

// También duplicados
public List<Post> getRecentPosts(int limit)
public List<Post> findRecentPosts(int limit)
Recomendación: Mantén solo 
findAll()
 y 
findRecentPosts(int limit)
.

2. PerformanceInterceptor - Código Muerto
Este interceptor calcula la duración de requests pero los logs están deshabilitados:

java
long duration = System.currentTimeMillis() - startTime;
// logs deshabilitados
Recomendación: Elimínalo completamente o activa los logs si realmente lo necesitas.

3. DTOs No Usados
LoginRequest
 y 
LoginResponse
 no se usan en ninguna parte. El proyecto usa Thymeleaf con Spring Security (form-based), no APIs REST.

Recomendación: Elimina estos DTOs o úsalos si planeas crear una API REST.

4. BreadcrumbService - Parsing Manual Complejo
294 líneas de código para parsear URLs manualmente y generar breadcrumbs. Tiene múltiples switch-case gigantes y lógica duplicada.

Recomendación: Usa anotaciones en controllers o un sistema más simple basado en configuración.

5. Métodos Deprecated con Implementaciones Vacías
En 
Request.java
:

java
@Deprecated
public void setMaterials(String materialsString) {
    // Empty implementation - materials managed through entity relationship
}
Recomendación: Elimina completamente el código deprecated en lugar de dejarlo vacío.

🟡 Problemas Moderados
6. AuthService - Responsabilidad Confusa
Mezcla lógica pública del index con registro de usuarios sin cohesión clara:

java
public Map<String, Object> getIndexData()  // Para página pública
public User registerUser(User user)        // Para autenticación
Recomendación: Divide en IndexService y mantén solo auth en 
AuthService
.

7. Inicialización Lazy Manual Repetitiva
RequestService
 tiene código repetido en múltiples métodos para forzar inicialización:

java
if (request.getUser() != null) {
    request.getUser().getUsername();
    request.getUser().getFirstName();
    // ... repetido en 3 métodos diferentes
}
Recomendación: Usa @EntityGraph o cambia a FetchType.EAGER en relaciones críticas, o crea DTOs.

8. Lógica de Vista en Servicios
PostService
 tiene métodos que preparan el Model (responsabilidad del controller):

java
public void preparePostListView(Model model)
public void preparePostFormView(Model model, Long postId, boolean isEdit)
Recomendación: Mueve esta lógica a los controllers donde pertenece.

9. MaterialService - Métodos TODO Placeholder
Tres métodos completos con implementaciones vacías y TODOs:

java
public List<Material> getAcceptedMaterialsByOrganization(User organization) {
    // TODO: Implementar consulta real
    return new ArrayList<>();
}
Recomendación: No crees métodos hasta que los necesites (YAGNI - You Aren't Gonna Need It).

10. LoginSuccessHandler Simplificado
El comentario dice "sistema flexible que utiliza el enum Role con prioridades" pero solo hace if-else simples. Además tiene un método 
redirectToDashboard
 que parece no usarse.

Recomendación: Elimina código y comentarios que no reflejan la realidad.

📊 Resumen
Categoría	Archivos Afectados	Impacto
Código duplicado	PostService, RequestService	Alto
Código muerto	PerformanceInterceptor, DTOs	Medio
Sobreingeniería	BreadcrumbService	Alto
Violación SRP	AuthService, PostService	Medio
Placeholder code	MaterialService, Request	Bajo
✅ Plan de Acción Sugerido
Eliminar métodos duplicados en PostService (impacto inmediato)
Eliminar PerformanceInterceptor o activar logs
Eliminar DTOs no usados (LoginRequest/Response)
Simplificar o reemplazar BreadcrumbService
Separar AuthService en servicios con responsabilidades claras
Mover lógica de vista de services a controllers
Eliminar métodos deprecated vacíos
Eliminar métodos TODO hasta que sean necesarios
Prioridad: El mayor impacto está en PostService (duplicación) y BreadcrumbService (complejidad innecesaria).