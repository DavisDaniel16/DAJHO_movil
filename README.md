# DAJHO 🦉
> **"Gestiona · Vende · Crece"**

---

## 📱 Descripción del Proyecto
**DAJHO** es una aplicación móvil nativa para Android diseñada específicamente para optimizar la gestión comercial de pequeños y medianos comercios minoristas (especialmente tiendas de ropa y calzado ) en Ecuador, alineándose con los requerimientos del régimen **RIMPE Negocio Popular**. La aplicación ofrece una interfaz moderna, intuitiva y ágil para el control de inventario, registro de ventas y supervisión de clientes.

---

## ✨ Características Principales (Features)

- **Autenticación Moderna y Segura:** Pantallas de inicio de sesión (*Login*) y registro de nuevos usuarios (*Sign Up*) con validación de formularios en tiempo real.
- **Diseño UI de Alta Gama:** Interfaz visual con paleta de colores vibrantes (tonos neón y morados), tarjetas flotantes con bordes redondeados y soporte adaptativo.
- **Pantalla Principal (Home):** Panel de bienvenida con indicador de sesión activa y opción de cierre de sesión seguro (*Logout*).
- **Enfoque Comercial:** Diseñado para facilitar el control de stock categorizado por tallas y colores, ideal para comercios de moda.

---

## 🛠️ Stack Tecnológico

- **Lenguaje:** [Kotlin](https://kotlinlang.org/) (100% Nativo)
- **Interfaz de Usuario:** XML Layouts, ConstraintLayout, Material Design 3 Components (`TextInputLayout`, `MaterialButton`, `CardView`).
- **Arquitectura:** Patrón basado en Activities orientadas a flujos de autenticación e inicio.
- **Entorno de Desarrollo:** [Android Studio](https://developer.android.com/studio)
- **Control de Versiones:** Git / GitHub

---

## 📂 Estructura Actual del Proyecto

El código fuente principal se encuentra en `app/src/main/java/com/example/dajho/`:

- `LoginActivity.kt`: Gestiona el acceso de los usuarios registrados y sirve como punto de entrada principal (*Launcher*).
- `SignUpActivity.kt`: Administra el registro de nuevas cuentas de comercio con validación de datos (nombre, teléfono, correo y contraseña).
- `HomeActivity.kt`: Panel de bienvenida tras un inicio de sesión exitoso, mostrando la sesión activa y el botón de cierre de sesión.

---

## 📋 Requisitos Previos

Antes de clonar y ejecutar el proyecto en tu máquina local, asegúrate de contar con lo siguiente:

- **Android Studio:** Versión más reciente recomendada (Koala / Jellyfish o superior).
- **SDK de Android:** 
  - *Compile SDK:* 37 (o superior)
  - *Min SDK:* 26 (Android 8.0 Oreo o superior)
- **Dispositivo o Emulador:** Dispositivo virtual Android con Google Play Services o dispositivo físico con depuración USB activada.

---

## 🚀 Instalación y Ejecución

Sigue estos pasos para poner en marcha el proyecto:

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/tu-usuario/DAJHO.git
   ```

2. **Abrir el proyecto:**
   - Abre **Android Studio**.
   - Selecciona **File > Open** y busca la carpeta del proyecto `DAJHO`.

3. **Sincronizar Gradle:**
   - Espera a que Android Studio descargue las dependencias y sincronice el archivo `build.gradle.kts` automáticamente.

4. **Ejecutar la App:**
   - Selecciona un emulador o conecta tu teléfono Android.
   - Haz clic en el botón **Run** (ícono de reproducción verde ▶️) o presiona `Shift + F10`.

---

## 🚧 Estado del Proyecto

Actualmente, **DAJHO** se encuentra en su fase de **Producto Mínimo Viable (MVP)**:
- ✅ Módulo de Autenticación (Login, Registro y Home).
- ✅ Diseño de interfaz (*Design System*) implementado.
- 🔄 Módulos de Inventario, Gestión de Stock por Talla/Color y Punto de Venta (POS) en desarrollo activo.

---

## 🤝 Contribución

¡Las contribuciones son bienvenidas! Si deseas proponer mejoras o reportar un error:
1. Haz un *Fork* del repositorio.
2. Crea una nueva rama (`git checkout -b feature/nueva-funcionalidad`).
3. Realiza tus cambios y haz *Commit* (`git commit -m "Agregada nueva funcionalidad"`).
4. Haz *Push* a la rama (`git push origin feature/nueva-funcionalidad`).
5. Abre un *Pull Request*.

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Consulta el archivo `LICENSE` para más detalles.
