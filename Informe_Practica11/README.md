# Alden (App de Asistencia)
# Informe Práctica 11: Secuencias de Animación y Transiciones

## 1. Descripción del Objetivo
El objetivo de esta práctica fue implementar **animaciones y transiciones visuales** en la aplicación Android “Control de Asistencia EPN (Alden)”, con el fin de mejorar la experiencia de usuario mediante retroalimentación visual.  
Se trabajó con **animaciones de vistas** (alpha, scale, translation, rotation) y **animaciones por propiedades** (ObjectAnimator / AnimatorSet), además de **transiciones entre pantallas** (Fade, Slide, Scale). También se integró **persistencia de estado** para disparar animaciones según la última acción registrada.

---

## 2. Implementación Realizada
### 2.1 Pantalla de Animaciones
- Se creó una pantalla accesible desde el Dashboard: **“Ver animaciones”**.
- La pantalla se encuentra protegida por validación de sesión (si no existe sesión → redirige a Login).

### 2.2 Animaciones aplicadas
- **ViewPropertyAnimator**: animación tipo “press” en tarjetas/botones.
- **ObjectAnimator / Property Animations**:
  - Animación de error tipo *shake* (temblor) ante acciones no permitidas.
  - Animación de éxito tipo *pulse* ante registro correcto.

### 2.3 Secuencias y transiciones
- Secuencia al registrar asistencia: **press → confirmación visual (pulse/shake) → mensaje**.
- Transiciones entre pantallas:
  - Dashboard → Estadísticas (**Fade**)
  - Dashboard → Detalle (**Slide**)
  - Dashboard → Animaciones (**Scale**)
- Animación de salida coherente al volver (transición inversa).

### 2.4 Persistencia y estado
- Se registró el estado de la última acción del usuario:
  - Última acción (Entrada/Salida)
  - Timestamp
  - Resultado (éxito/fallo)
- Se dispara una animación automática al abrir la Pantalla de Animaciones según el estado persistido.

---

## 3. Capturas
> Coloca aquí tus capturas (mínimo sugerido: Dashboard + Pantalla de Animaciones + ejemplo de transición + ejemplo de éxito/fallo)

- **Figura 1.** Dashboard con opción “Ver animaciones”.  
  `capturas/dashboard_animaciones.png`

- **Figura 2.** Pantalla de Animaciones (estado cargado).  
  `capturas/pantalla_animaciones_estado.png`

- **Figura 3.** Transición Dashboard → Detalle / Estadísticas.  
  `capturas/transicion_detalle.png`  
  `capturas/transicion_estadisticas.png`

- **Figura 4.** Animación de éxito (pulse) / error (shake).  
  `capturas/animacion_exito.png`  
  `capturas/animacion_error.png`

---

## 4. Matriz de Casos de Prueba

| ID | Caso de prueba | Precondición | Pasos | Resultado esperado | Resultado obtenido |
|---|---|---|---|---|---|
| CP-01 | Acceso a “Ver animaciones” con sesión | Sesión iniciada | Dashboard → Ver animaciones | Abre pantalla y permite ejecutar animaciones | [OK/NO] |
| CP-02 | Acceso a “Ver animaciones” sin sesión | Sin sesión | Intentar abrir pantalla | Redirige a Login | [OK/NO] |
| CP-03 | Registro Entrada exitoso | Sesión iniciada, usuario habilitado y en rango | Tocar “Entrada” | Toast de éxito + pulse en tarjeta + guarda estado success=true | [OK/NO] |
| CP-04 | Registro Entrada fallido | Sesión iniciada, usuario no habilitado/fuera de rango | Tocar “Entrada” | Toast de error + shake + guarda estado success=false | [OK/NO] |
| CP-05 | Transición Dashboard → Estadísticas | Sesión iniciada | Dashboard → Estadísticas | Transición Fade y retorno coherente | [OK/NO] |
| CP-06 | Transición Dashboard → Detalle | Sesión iniciada | Dashboard → Ver detalle | Transición Slide y retorno coherente | [OK/NO] |
| CP-07 | Disparo automático por estado | Existe estado guardado | Abrir Pantalla de Animaciones | Si success=true → pulse; si false → shake | [OK/NO] |
| CP-08 | Limpiar estado (si aplica) | Existe estado guardado | Botón “Limpiar estado” | Estado se reinicia y no se dispara animación al reingresar | [OK/NO] |

---

## 5. Conclusiones
- e logró integrar animaciones y transiciones que mejoran la retroalimentación visual, haciendo que acciones como registrar asistencia sean más claras para el usuario (éxito: *pulse*, error: *shake*).
- La implementación de transiciones (Fade/Slide/Scale) permitió una navegación más fluida entre pantallas, reforzando el diseño moderno de la interfaz.
- La persistencia de estado permitió mantener consistencia de la experiencia (la pantalla de animaciones responde según la última acción registrada).

---

## 6. Recomendaciones
- Mantener duraciones cortas y uso de interpoladores suaves para evitar animaciones bruscas o molestas.
- Estandarizar los mensajes (Toast/Eventos) para detectar éxito/fallo de forma confiable.
- Considerar migrar la persistencia a DataStore si se requiere un manejo más robusto de estado en futuras prácticas.

---

## 7. APK (Entrega)
- Archivo APK generado: **Alden_11.apk**
