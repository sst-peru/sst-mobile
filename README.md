# sst-mobile

App Android del sistema de gestión de **Seguridad y Salud en el Trabajo** (Ley 29783 — Perú).
La usa el operario en campo para reportar actos y condiciones inseguras en segundos.
Consume el mismo API que el panel web: [`sst-api`](https://github.com/sst-peru/sst-api).

## Stack

Kotlin 2.0 · Jetpack Compose (Material 3) · Retrofit + Gson · Room · WorkManager · DataStore · Coil

## Abrirlo

1. Android Studio → **Open** → esta carpeta. Gradle sincroniza solo.
2. Levanta `sst-api` en tu PC (`python manage.py runserver 0.0.0.0:8000`).
3. Corre la app en un emulador (API 26+) y entra con `operario1` / `demo12345`.
4. `bash scripts/setup-hooks.sh` para activar la validación de commits.

El `API_BASE_URL` está en `app/build.gradle.kts` como `http://10.0.2.2:8000/api/v1/`:
**10.0.2.2 es la dirección con la que el emulador ve el localhost de tu PC.** En un celular
físico, cámbialo por la IP de tu PC en la red (`http://192.168.x.x:8000/api/v1/`), agrégala a
`ALLOWED_HOSTS` en el `.env` del API y revisa `network_security_config.xml`.

## Estructura

```
app/src/main/java/pe/sst/app/
├── SstApplication.kt          AppContainer: inyección de dependencias a mano
├── MainActivity.kt
├── data/
│   ├── remote/                Retrofit: ApiService, DTOs, AuthInterceptor, ApiClient
│   ├── local/                 Room (cola de reportes) y DataStore (tokens, variante A/B)
│   └── repository/            AuthRepository, ReportRepository
├── sync/ReportSyncWorker.kt   sube la cola cuando vuelve la red
├── feature/
│   ├── auth/                  login
│   └── reports/               lista, formulario rápido, formulario largo, foto + GPS
└── ui/                        tema y navegación
```

## Las tres decisiones que sostienen la app

**1. Offline primero, no "offline si falla".** Todo reporte se guarda en Room *antes* de intentar
subirlo, y el operario ve "guardado" de inmediato. `ReportSyncWorker` (WorkManager) espera la
conexión y reintenta con backoff. Nunca se hace esperar al operario por la red: esa espera es
exactamente lo que hace que deje de reportar.

**2. Sin duplicados al sincronizar.** Cada reporte lleva un `client_uuid` generado en el celular.
Si la subida se corta a medias y se reintenta, el backend reconoce el uuid y devuelve el reporte
que ya tenía en vez de crear otro. Además se manda `occurred_at`, así un reporte hecho el lunes
en la mina y sincronizado el miércoles conserva su fecha real.

**3. El A/B test vive en una sola pantalla.** `NewReportScreen` lee la variante asignada al usuario
y muestra `QuickReportForm` (3 pasos: qué viste → qué tipo → foto y enviar) o `LongReportForm`
(formulario tradicional con todos los campos obligatorios). Todo lo demás es idéntico para los dos
grupos, así la única diferencia medida es el formulario. La variante se guarda en DataStore al
entrar, de modo que el experimento también funciona sin señal.

## Permisos y por qué

| Permiso | Para qué |
|---------|----------|
| `INTERNET`, `ACCESS_NETWORK_STATE` | Hablar con el API y saber cuándo hay red |
| `CAMERA` | La foto del hallazgo |
| `ACCESS_FINE_LOCATION` | Geolocalizar dónde fue. Se pide **después** de la foto, cuando ya se entiende para qué; si se rechaza, el reporte se envía sin GPS |

## Pendiente (buenos candidatos a ramas `feature/`)

- Ver el detalle de un reporte ya cerrado con su acción correctiva
- Firmar la conformidad de entrega de EPP desde el celular
- Completar inspecciones con checklist desde el celular
- Notificaciones push cuando te asignan un hallazgo

## Cómo contribuir

Ramas, Conventional Commits y merges: ver [CONTRIBUTING.md](CONTRIBUTING.md).
