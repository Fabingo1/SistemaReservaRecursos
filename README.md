\# Sistema de Reserva de Recursos



\## Usuarios de prueba



Al ejecutar el programa por primera vez, si la carpeta `data/` está vacía, se generan automáticamente:



| Usuario | Clave | Rol |

|---|---|---|

| `admin1` | `admin1` | Administrador |

| `func1` | `func1` | Funcionario |



\## Configurar la funcionalidad de IA (botón "Extraer" en Reservas)



Sin esta configuración, el resto del sistema funciona igual — solo ese botón queda deshabilitado.



1\. Entra a \[aistudio.google.com] e inicia sesión con una cuenta de Google (sin tarjeta de crédito).

2\. "Get API key" → "Create API key" → copia la clave

3\. Agrégala como variable de entorno del sistema:

&#x20;  - Windows: busca "Editar las variables de entorno del sistema" → Variables de entorno → "Variables de usuario" → Nueva.

&#x20;  - Nombre: `GEMINI\_API\_KEY`

&#x20;  - Valor: la clave copiada

4\. Reinicia el IDE y ejecuta el Main.

