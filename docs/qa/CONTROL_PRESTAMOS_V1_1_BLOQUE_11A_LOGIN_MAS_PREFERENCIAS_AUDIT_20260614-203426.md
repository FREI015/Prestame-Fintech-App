# CONTROL PRESTAMOS - BLOQUE 11A LOGIN ICONOS Y AUDITORIA MAS/PREFERENCIAS

Fecha:
20260614-203426

Rama:
feature/v1.1-premium-dashboard-security

## Objetivo
Colocar iconos seguros en metodos de desbloqueo y auditar Mas/Preferencias antes de simplificar funciones duplicadas.

## Cambio aplicado en Login
- Metodo PIN: icono Unicode seguro por escape Kotlin.
- Metodo biometria: icono Unicode seguro por escape Kotlin.
- No se insertaron emojis literales para evitar corrupcion de encoding.

## Auditoria de botones sospechosos en Mas/Preferencias


## Auditoria de callbacks vacios en Mas/Preferencias


## Auditoria estructural completa
# AUDITORIA MAS / PREFERENCIAS / ACERCA DE / AYUDA
Fecha: 20260614-203426
Rama: feature/v1.1-premium-dashboard-security

Criterio inicial:
- Quitar duplicidades en Mas.
- Preferencias debe guardar cambios reales o eliminar controles falsos.
- Exportacion no debe duplicarse si ya existe pantalla/flujo dedicado.
- Informacion institucional de la app debe concentrarse en Acerca de.
- Ayuda debe tener contenido util, no tarjetas vacias.
- No tocar datos financieros ni repositorios de negocio en este bloque.

## MoreScreen - secciones, tarjetas, accesos y callbacks
Archivo: app/src/main/java/com/controlprestamos/features/more/presentation/MoreScreen.kt

### Patron: fun MoreScreen
34: fun MoreScreen(

### Patron: onOpen
37: onOpenHome: () -> Unit = {},
38: onOpenClients: () -> Unit = {},
39: onOpenLoans: () -> Unit = {},
40: onOpenPayments: () -> Unit = {},
41: onOpenReports: () -> Unit = {},
42: onOpenFinancialAudit: () -> Unit = onOpenReports,
43: onOpenReport: () -> Unit = onOpenReports,
44: onOpenSettings: () -> Unit = {},
45: onOpenPreferences: () -> Unit = onOpenSettings,
46: onOpenSecurity: () -> Unit = {},
47: onOpenBackup: () -> Unit = {},
48: onOpenExport: () -> Unit = {},
49: onOpenImport: () -> Unit = {},
50: onOpenData: () -> Unit = {},
51: onOpenAbout: () -> Unit = {},
52: onOpenSupport: () -> Unit = {},
53: onOpenHelp: () -> Unit = onOpenSupport,
54: onOpenBusinessSettings: () -> Unit = {},
55: onOpenCurrencySettings: () -> Unit = {},
56: onOpenAppearanceSettings: () -> Unit = {},
57: onOpenMore: () -> Unit = {},
92: onOpenClients = onOpenClients,
93: onOpenLoans = onOpenLoans,
94: onOpenPayments = onOpenPayments,
95: onOpenReports = onOpenReports
99: onOpenPreferences = onOpenPreferences,
100: onOpenBusinessSettings = onOpenBusinessSettings,
101: onOpenCurrencySettings = onOpenCurrencySettings,
102: onOpenAppearanceSettings = onOpenAppearanceSettings
106: onOpenBackup = onOpenBackup,
107: onOpenExport = onOpenExport,
108: onOpenImport = onOpenImport,
109: onOpenSecurity = onOpenSecurity
113: onOpenAbout = onOpenAbout,
114: onOpenSupport = onOpenSupport
130: onOpenHome,
131: onOpenReport,
132: onOpenSettings,
133: onOpenData,
134: onOpenMore,
135: onOpenFinancialAudit,
136: onOpenHelp,
179: onOpenClients: () -> Unit,
180: onOpenLoans: () -> Unit,
181: onOpenPayments: () -> Unit,
182: onOpenReports: () -> Unit
194: onPrimaryClick = onOpenClients
201: onPrimaryClick = onOpenLoans
208: onPrimaryClick = onOpenPayments
215: onPrimaryClick = onOpenReports
222: onOpenPreferences: () -> Unit,
223: onOpenBusinessSettings: () -> Unit,
224: onOpenCurrencySettings: () -> Unit,
225: onOpenAppearanceSettings: () -> Unit
237: onPrimaryClick = onOpenPreferences
244: onPrimaryClick = onOpenBusinessSettings
251: onPrimaryClick = onOpenCurrencySettings
258: onPrimaryClick = onOpenAppearanceSettings
265: onOpenBackup: () -> Unit,
266: onOpenExport: () -> Unit,
267: onOpenImport: () -> Unit,
268: onOpenSecurity: () -> Unit
280: onPrimaryClick = onOpenBackup
287: onPrimaryClick = onOpenExport
294: onPrimaryClick = onOpenImport
301: onPrimaryClick = onOpenSecurity
308: onOpenAbout: () -> Unit,
309: onOpenSupport: () -> Unit
321: onPrimaryClick = onOpenAbout
328: onPrimaryClick = onOpenSupport

### Patron: onNavigate
35: onNavigateBack: () -> Unit = {},
36: onBack: () -> Unit = onNavigateBack,

### Patron: Info
161: InfoBox(
168: InfoBox(
273: subtitle = "Herramientas para proteger información y preparar respaldos."
284: title = "Exportar información",
314: subtitle = "Información de la aplicación y ayuda operativa."
345: InfoBox(
352: InfoBox(
432: private fun InfoBox(

### Patron: Aplicaci
235: description = "Revisar configuración principal de la aplicación.",
314: subtitle = "Información de la aplicación y ayuda operativa."

### Patron: Acerca
318: title = "Acerca de",

### Patron: Ayuda
314: subtitle = "Información de la aplicación y ayuda operativa."
325: title = "Ayuda",

### Patron: Acceso
152: text = "Gestiona ajustes, herramientas, datos y accesos rápidos desde un solo lugar.",
186: title = "Accesos rápidos",
299: description = "Opciones de acceso, privacidad y protección de datos.",

### Patron: Rápido
152: text = "Gestiona ajustes, herramientas, datos y accesos rápidos desde un solo lugar.",
186: title = "Accesos rápidos",

### Patron: Rapido
Sin coincidencias.

### Patron: Export
48: onOpenExport: () -> Unit = {},
107: onOpenExport = onOpenExport,
266: onOpenExport: () -> Unit,
284: title = "Exportar información",
286: primaryText = "Exportar",
287: onPrimaryClick = onOpenExport

### Patron: Backup
47: onOpenBackup: () -> Unit = {},
106: onOpenBackup = onOpenBackup,
265: onOpenBackup: () -> Unit,
280: onPrimaryClick = onOpenBackup

### Patron: Respaldo
273: subtitle = "Herramientas para proteger información y preparar respaldos."
277: title = "Respaldo local",

### Patron: Preferencias
229: title = "Preferencias",
234: title = "Preferencias generales",

### Patron: Ajustes
64: subtitle = "Ajustes y herramientas",
152: text = "Gestiona ajustes, herramientas, datos y accesos rápidos desde un solo lugar.",

### Patron: Soporte
313: title = "Soporte",

### Patron: Button
27: import com.controlprestamos.core.ui.components.PrimaryButton
28: import com.controlprestamos.core.ui.components.SecondaryButton
119: SecondaryButton(
423: PrimaryButton(

### Patron: Card
25: import com.controlprestamos.core.ui.components.AppCard
89: MoreHeaderCard()
91: MainAccessCard(
98: PreferencesCard(
105: DataSecurityCard(
112: SupportCard(
117: AppStatusCard()
142: private fun MoreHeaderCard() {
143: ReferenceCard {
178: private fun MainAccessCard(
184: ReferenceCard {
221: private fun PreferencesCard(
227: ReferenceCard {
264: private fun DataSecurityCard(
270: ReferenceCard {
307: private fun SupportCard(
311: ReferenceCard {
334: private fun AppStatusCard() {
335: ReferenceCard {
400: shape = RoundedCornerShape(AppRadius.card),
441: shape = RoundedCornerShape(AppRadius.card),
468: private fun ReferenceCard(
471: AppCard(

### Patron: Text\\(
25: import com.controlprestamos.core.ui.components.AppCard
89: MoreHeaderCard()
91: MainAccessCard(
98: PreferencesCard(
105: DataSecurityCard(
112: SupportCard(
117: AppStatusCard()
142: private fun MoreHeaderCard() {
143: ReferenceCard {
178: private fun MainAccessCard(
184: ReferenceCard {
221: private fun PreferencesCard(
227: ReferenceCard {
264: private fun DataSecurityCard(
270: ReferenceCard {
307: private fun SupportCard(
311: ReferenceCard {
334: private fun AppStatusCard() {
335: ReferenceCard {
400: shape = RoundedCornerShape(AppRadius.card),
441: shape = RoundedCornerShape(AppRadius.card),
468: private fun ReferenceCard(
471: AppCard(

### Patron: clickable
Sin coincidencias.

### Patron: onClick
121: onClick = onBack
425: onClick = onPrimaryClick

## PreferencesScreen - formularios, botones y controles
Archivo: app/src/main/java/com/controlprestamos/features/preferences/presentation/PreferencesScreen.kt

### Patron: fun PreferencesScreen
41: fun PreferencesScreen(

### Patron: Datos
98: text = "Datos generales",
105: text = "Estos datos se usarán en reportes, recibos, WhatsApp y pantallas principales.",

### Patron: negocio
120: label = "Nombre del negocio"

### Patron: Moneda
77: subtitle = "Personaliza moneda, reportes y apariencia",
129: label = "Símbolo de moneda"

### Patron: moneda
77: subtitle = "Personaliza moneda, reportes y apariencia",
129: label = "Símbolo de moneda"

### Patron: Formato
133: text = "Formato de fecha",

### Patron: Apariencia
77: subtitle = "Personaliza moneda, reportes y apariencia",
182: text = "Elige la apariencia que mejor represente tu forma de trabajar.",

### Patron: Tema
246: text = "Tema: ${themeLabel(visualTheme)}",

### Patron: Guardar
258: text = "Guardar preferencias",

### Patron: Aplicar
Sin coincidencias.

### Patron: Export
Sin coincidencias.

### Patron: Acerca
Sin coincidencias.

### Patron: Ayuda
Sin coincidencias.

### Patron: Button
31: import com.controlprestamos.core.ui.components.PrimaryButton
32: import com.controlprestamos.core.ui.components.SecondaryButton
149: SecondaryButton(
158: SecondaryButton(
257: PrimaryButton(

### Patron: OutlinedButton
Sin coincidencias.

### Patron: TextButton
Sin coincidencias.

### Patron: onClick
152: onClick = {
161: onClick = {
193: onClick = {
225: onClick = {
259: onClick = {
295: onClick: () -> Unit
308: onClick()

### Patron: remember
11: import androidx.compose.foundation.rememberScrollState
21: import androidx.compose.runtime.remember
45: val storedPreferences = remember {
49: var businessName by remember {
53: var currencySymbol by remember {
57: var dateFormat by remember {
61: var visualTheme by remember {
65: var visualScale by remember {
69: var savedMessage by remember {
93: .verticalScroll(rememberScrollState())

### Patron: mutableStateOf
20: import androidx.compose.runtime.mutableStateOf
50: mutableStateOf(storedPreferences.businessName)
54: mutableStateOf(storedPreferences.currencySymbol)
58: mutableStateOf(storedPreferences.dateFormat)
62: mutableStateOf(storedPreferences.visualTheme)
66: mutableStateOf(storedPreferences.visualScale)
70: mutableStateOf("")

### Patron: LocalPreferencesRepository
38: import com.controlprestamos.features.preferences.data.LocalPreferencesRepository
46: LocalPreferencesRepository.getPreferences(context)
260: LocalPreferencesRepository.savePreferences(

### Patron: save
69: var savedMessage by remember {
118: savedMessage = ""
127: savedMessage = ""
154: savedMessage = ""
163: savedMessage = ""
195: savedMessage = ""
227: savedMessage = ""
260: LocalPreferencesRepository.savePreferences(
271: savedMessage = "Preferencias guardadas correctamente."
275: if (savedMessage.isNotBlank()) {
277: text = savedMessage,

### Patron: update
Sin coincidencias.

### Patron: businessName
49: var businessName by remember {
50: mutableStateOf(storedPreferences.businessName)
115: value = businessName,
117: businessName = it
263: businessName = businessName,

### Patron: currency
53: var currencySymbol by remember {
54: mutableStateOf(storedPreferences.currencySymbol)
124: value = currencySymbol,
126: currencySymbol = it.take(4)
264: currencySymbol = currencySymbol,

### Patron: symbol
53: var currencySymbol by remember {
54: mutableStateOf(storedPreferences.currencySymbol)
124: value = currencySymbol,
126: currencySymbol = it.take(4)
264: currencySymbol = currencySymbol,

### Patron: visual
36: import com.controlprestamos.features.preferences.data.AppVisualScale
37: import com.controlprestamos.features.preferences.data.AppVisualTheme
61: var visualTheme by remember {
62: mutableStateOf(storedPreferences.visualTheme)
65: var visualScale by remember {
66: mutableStateOf(storedPreferences.visualScale)
175: text = "Estilo visual",
187: AppVisualTheme.values().forEach { theme ->
191: selected = visualTheme == theme.name,
194: visualTheme = theme.name
207: text = "Escala visual",
214: text = "Ajusta la densidad visual según cómo quieras ver la información.",
219: AppVisualScale.values().forEach { scale ->
223: selected = visualScale == scale.name,
224: accentKind = visualTheme,
226: visualScale = scale.name
246: text = "Tema: ${themeLabel(visualTheme)}",
252: text = "Escala: ${scaleLabel(visualScale)}",
266: visualTheme = visualTheme,
267: visualScale = visualScale
298: AppVisualTheme.FINANCIAL_GREEN.name -> AppColors.Success
299: AppVisualTheme.PROFESSIONAL_GOLD.name -> AppColors.Warning
339: AppVisualTheme.valueOf(value).label
340: }.getOrDefault(AppVisualTheme.EXECUTIVE_BLUE.label)
345: AppVisualScale.valueOf(value).label
346: }.getOrDefault(AppVisualScale.NORMAL.label)

### Patron: theme
14: import androidx.compose.material3.MaterialTheme
33: import com.controlprestamos.core.ui.theme.AppColors
34: import com.controlprestamos.core.ui.theme.AppSpacing
37: import com.controlprestamos.features.preferences.data.AppVisualTheme
61: var visualTheme by remember {
62: mutableStateOf(storedPreferences.visualTheme)
99: style = MaterialTheme.typography.headlineMedium,
106: style = MaterialTheme.typography.bodyMedium,
134: style = MaterialTheme.typography.titleMedium,
141: style = MaterialTheme.typography.bodyMedium,
176: style = MaterialTheme.typography.titleMedium,
183: style = MaterialTheme.typography.bodySmall,
187: AppVisualTheme.values().forEach { theme ->
189: title = theme.label,
190: description = theme.description,
191: selected = visualTheme == theme.name,
192: accentKind = theme.name,
194: visualTheme = theme.name
208: style = MaterialTheme.typography.titleMedium,
215: style = MaterialTheme.typography.bodySmall,
224: accentKind = visualTheme,
240: style = MaterialTheme.typography.titleMedium,
246: text = "Tema: ${themeLabel(visualTheme)}",
247: style = MaterialTheme.typography.bodyMedium,
253: style = MaterialTheme.typography.bodyMedium,
266: visualTheme = visualTheme,
278: style = MaterialTheme.typography.bodyMedium,
298: AppVisualTheme.FINANCIAL_GREEN.name -> AppColors.Success
299: AppVisualTheme.PROFESSIONAL_GOLD.name -> AppColors.Warning
323: style = MaterialTheme.typography.titleSmall,
330: style = MaterialTheme.typography.bodySmall,
337: private fun themeLabel(value: String): String {
339: AppVisualTheme.valueOf(value).label
340: }.getOrDefault(AppVisualTheme.EXECUTIVE_BLUE.label)

### Patron: density
Sin coincidencias.

## LocalPreferencesRepository - datos que realmente se guardan
Archivo: app/src/main/java/com/controlprestamos/features/preferences/data/LocalPreferencesRepository.kt

### Patron: data class
45: data class AppPreferences(

### Patron: KEY_
87: private const val KEY_BUSINESS_NAME = "business_name"
88: private const val KEY_CURRENCY_SYMBOL = "currency_symbol"
89: private const val KEY_DATE_FORMAT = "date_format"
90: private const val KEY_VISUAL_THEME = "visual_theme"
91: private const val KEY_VISUAL_SCALE = "visual_scale"
97: businessName = getCleanPreference(sharedPreferences, KEY_BUSINESS_NAME, "Control Préstamos"),
98: currencySymbol = sharedPreferences.getString(KEY_CURRENCY_SYMBOL, "$") ?: "$",
99: dateFormat = sharedPreferences.getString(KEY_DATE_FORMAT, "dd/MM/yyyy") ?: "dd/MM/yyyy",
100: visualTheme = sharedPreferences.getString(KEY_VISUAL_THEME, AppVisualTheme.EXECUTIVE_BLUE.name)
102: visualScale = sharedPreferences.getString(KEY_VISUAL_SCALE, AppVisualScale.NORMAL.name)
115: .putString(KEY_BUSINESS_NAME, normalized.businessName)
116: .putString(KEY_CURRENCY_SYMBOL, normalized.currencySymbol)
117: .putString(KEY_DATE_FORMAT, normalized.dateFormat)
118: .putString(KEY_VISUAL_THEME, normalized.visualTheme)
119: .putString(KEY_VISUAL_SCALE, normalized.visualScale)

### Patron: businessName
46: val businessName: String = "Control Préstamos",
97: businessName = getCleanPreference(sharedPreferences, KEY_BUSINESS_NAME, "Control Préstamos"),
115: .putString(KEY_BUSINESS_NAME, normalized.businessName)
145: businessName = sanitizeVisibleText(businessName).trim().ifBlank { "Control Préstamos" },

### Patron: currency
47: val currencySymbol: String = "$",
88: private const val KEY_CURRENCY_SYMBOL = "currency_symbol"
98: currencySymbol = sharedPreferences.getString(KEY_CURRENCY_SYMBOL, "$") ?: "$",
116: .putString(KEY_CURRENCY_SYMBOL, normalized.currencySymbol)
146: currencySymbol = currencySymbol.trim().ifBlank { "$" }.take(4),

### Patron: symbol
47: val currencySymbol: String = "$",
88: private const val KEY_CURRENCY_SYMBOL = "currency_symbol"
98: currencySymbol = sharedPreferences.getString(KEY_CURRENCY_SYMBOL, "$") ?: "$",
116: .putString(KEY_CURRENCY_SYMBOL, normalized.currencySymbol)
146: currencySymbol = currencySymbol.trim().ifBlank { "$" }.take(4),

### Patron: theme
5: enum class AppVisualTheme(
49: val visualTheme: String = AppVisualTheme.EXECUTIVE_BLUE.name,
90: private const val KEY_VISUAL_THEME = "visual_theme"
100: visualTheme = sharedPreferences.getString(KEY_VISUAL_THEME, AppVisualTheme.EXECUTIVE_BLUE.name)
101: ?: AppVisualTheme.EXECUTIVE_BLUE.name,
118: .putString(KEY_VISUAL_THEME, normalized.visualTheme)
123: fun getVisualTheme(context: Context): AppVisualTheme {
125: AppVisualTheme.valueOf(getPreferences(context).visualTheme)
126: }.getOrDefault(AppVisualTheme.EXECUTIVE_BLUE)
136: val safeTheme = runCatching {
137: AppVisualTheme.valueOf(visualTheme)
138: }.getOrDefault(AppVisualTheme.EXECUTIVE_BLUE)
148: visualTheme = safeTheme.name,

### Patron: visual
5: enum class AppVisualTheme(
23: enum class AppVisualScale(
37: description = "Más aire visual entre tarjetas."
49: val visualTheme: String = AppVisualTheme.EXECUTIVE_BLUE.name,
50: val visualScale: String = AppVisualScale.NORMAL.name
90: private const val KEY_VISUAL_THEME = "visual_theme"
91: private const val KEY_VISUAL_SCALE = "visual_scale"
100: visualTheme = sharedPreferences.getString(KEY_VISUAL_THEME, AppVisualTheme.EXECUTIVE_BLUE.name)
101: ?: AppVisualTheme.EXECUTIVE_BLUE.name,
102: visualScale = sharedPreferences.getString(KEY_VISUAL_SCALE, AppVisualScale.NORMAL.name)
103: ?: AppVisualScale.NORMAL.name
118: .putString(KEY_VISUAL_THEME, normalized.visualTheme)
119: .putString(KEY_VISUAL_SCALE, normalized.visualScale)
123: fun getVisualTheme(context: Context): AppVisualTheme {
125: AppVisualTheme.valueOf(getPreferences(context).visualTheme)
126: }.getOrDefault(AppVisualTheme.EXECUTIVE_BLUE)
129: fun getVisualScale(context: Context): AppVisualScale {
131: AppVisualScale.valueOf(getPreferences(context).visualScale)
132: }.getOrDefault(AppVisualScale.NORMAL)
137: AppVisualTheme.valueOf(visualTheme)
138: }.getOrDefault(AppVisualTheme.EXECUTIVE_BLUE)
141: AppVisualScale.valueOf(visualScale)
142: }.getOrDefault(AppVisualScale.NORMAL)
148: visualTheme = safeTheme.name,
149: visualScale = safeScale.name

### Patron: density
Sin coincidencias.

### Patron: save
107: fun savePreferences(

### Patron: update
Sin coincidencias.

### Patron: SharedPreferences
71: sharedPreferences: android.content.SharedPreferences,
75: val raw = sharedPreferences.getString(key, fallback) ?: fallback
79: sharedPreferences.edit().putString(key, clean).apply()
94: val sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
97: businessName = getCleanPreference(sharedPreferences, KEY_BUSINESS_NAME, "Control Préstamos"),
98: currencySymbol = sharedPreferences.getString(KEY_CURRENCY_SYMBOL, "$") ?: "$",
99: dateFormat = sharedPreferences.getString(KEY_DATE_FORMAT, "dd/MM/yyyy") ?: "dd/MM/yyyy",
100: visualTheme = sharedPreferences.getString(KEY_VISUAL_THEME, AppVisualTheme.EXECUTIVE_BLUE.name)
102: visualScale = sharedPreferences.getString(KEY_VISUAL_SCALE, AppVisualScale.NORMAL.name)
113: context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

### Patron: sanitizeVisibleText
54: private fun sanitizeVisibleText(value: String): String {
76: val clean = sanitizeVisibleText(raw).trim().ifBlank { fallback }
145: businessName = sanitizeVisibleText(businessName).trim().ifBlank { "Control Préstamos" },

### Patron: getCleanPreference
70: private fun getCleanPreference(
97: businessName = getCleanPreference(sharedPreferences, KEY_BUSINESS_NAME, "Control Préstamos"),

## AppNavGraph - rutas conectadas
Archivo: app/src/main/java/com/controlprestamos/core/navigation/AppNavGraph.kt

### Patron: MoreScreen
31: import com.controlprestamos.features.more.presentation.MoreScreen
272: MoreScreen(

### Patron: PreferencesScreen
36: import com.controlprestamos.features.preferences.presentation.PreferencesScreen
304: PreferencesScreen(

### Patron: BackupScreen
34: import com.controlprestamos.features.backup.presentation.BackupScreen
320: BackupScreen(

### Patron: ReportsScreen
35: import com.controlprestamos.features.reports.presentation.ReportsScreen
328: ReportsScreen(

### Patron: Help
32: import com.controlprestamos.features.help.presentation.HelpScreen
291: onOpenHelp = {
292: navController.navigate(AppRoute.Help.route)
343: composable(AppRoute.Help.route) {
344: HelpScreen(

### Patron: About
Sin coincidencias.

### Patron: Acerca
Sin coincidencias.

### Patron: Ayuda
Sin coincidencias.

### Patron: onOpen
181: onOpenDashboard = {
184: onOpenClients = {
187: onOpenLoans = {
190: onOpenPayments = {
193: onOpenMore = {
204: onOpenDashboard = {
207: onOpenPayments = {
210: onOpenLoans = {
213: onOpenMore = {
219: onOpenClient = { clientId ->
229: onOpenDashboard = {
232: onOpenClients = {
235: onOpenPayments = {
238: onOpenMore = {
241: onOpenLoanDetail = { loanId ->
254: onOpenDashboard = {
257: onOpenClients = {
260: onOpenLoans = {
263: onOpenMore = {
276: onOpenPreferences = {
279: onOpenSecurity = {
282: onOpenBackup = {
285: onOpenReports = {
288: onOpenFinancialAudit = {
291: onOpenHelp = {
389: onOpenLoanDetail = { loanId ->
414: onOpenDashboard = {
417: onOpenClients = {
420: onOpenPayments = {
423: onOpenMore = {
429: onOpenLoan = { loanId ->
485: onOpenPayments = {

### Patron: navigate
43: fun navigateToLogin() {
44: navController.navigate(AppRoute.Login.route) {
50: fun navigateToDashboard() {
51: navController.navigate(AppRoute.Dashboard.route) {
56: fun navigateToClients() {
57: navController.navigate(AppRoute.Clients.route) {
62: fun navigateToPayments() {
63: navController.navigate(AppRoute.Payments.route) {
67: fun navigateToLoans() {
68: navController.navigate(AppRoute.Loans.route) {
76: fun navigateToMore() {
77: navController.navigate(AppRoute.More.route) {
113: navController.navigate(AppRoute.Login.route) {
141: navController.navigate(AppRoute.Dashboard.route) {
146: onNavigateToRegister = {
147: navController.navigate(AppRoute.Register.route)
154: navController.navigate(AppRoute.Dashboard.route) {
159: onNavigateToRegister = {
160: navController.navigate(AppRoute.Register.route)
168: navController.navigate(AppRoute.Dashboard.route) {
173: onNavigateBack = {
182: navController.navigate(AppRoute.Dashboard.route)
185: navController.navigate(AppRoute.Clients.route)
188: navController.navigate(AppRoute.Loans.route)
191: navController.navigate(AppRoute.Payments.route)
194: navController.navigate(AppRoute.More.route)
201: onNavigateBack = {
205: navigateToDashboard()
208: navigateToPayments()
211: navigateToLoans()
214: navigateToMore()
217: navController.navigate(AppRoute.CreateClient.route)
220: navController.navigate(AppRoute.ClientDetail.createRoute(clientId))
226: onNavigateBack = {
230: navigateToDashboard()
233: navigateToClients()
236: navigateToPayments()
239: navigateToMore()
242: navController.navigate(AppRoute.LoanDetail.createRoute(loanId))
245: navigateToClients()
251: onNavigateBack = {
255: navigateToDashboard()
258: navigateToClients()
261: navigateToLoans()
264: navigateToMore()
267: navController.navigate(AppRoute.CreatePayment.createRoute(loanId))
273: onNavigateBack = {
277: navController.navigate(AppRoute.Preferences.route)
280: navController.navigate(AppRoute.Security.route)
283: navController.navigate(AppRoute.Backup.route)
286: navController.navigate(AppRoute.Reports.route)
289: navController.navigate(AppRoute.FinancialAudit.route)
292: navController.navigate(AppRoute.Help.route)
296: navController.navigate(AppRoute.Login.route) {
305: onNavigateBack = {
313: onNavigateBack = {
321: onNavigateBack = {
329: onNavigateBack = {
337: onNavigateBack = {
345: onNavigateBack = {
352: onNavigateBack = {
356: navController.navigate(AppRoute.ClientDetail.createRoute(clientId)) {
377: onNavigateBack = {
381: navController.navigate(AppRoute.CreateLoan.createRoute(clientId))
384: navController.navigate(AppRoute.LoansByClient.createRoute(clientId))
387: navController.navigate(AppRoute.LoanDetail.createRoute(loanId))
390: navController.navigate(AppRoute.LoanDetail.createRoute(loanId))
393: navController.navigate(AppRoute.EditClient.createRoute(clientId))
411: onNavigateBack = {
415: navigateToDashboard()
418: navigateToClients()
421: navigateToPayments()
424: navigateToMore()
427: navController.navigate(AppRoute.CreateLoan.createRoute(clientId))
430: navController.navigate(AppRoute.LoanDetail.createRoute(loanId))
434: navController.navigate(AppRoute.CreatePayment.createRoute(loanId))
452: onNavigateBack = {
456: navController.navigate(AppRoute.LoanDetail.createRoute(loanId)) {
479: onNavigateBack = {
483: navController.navigate(AppRoute.CreatePayment.createRoute(loanId))
486: navController.navigate(AppRoute.PaymentsByLoan.createRoute(loanId))
490: navController.navigate(AppRoute.EditLoan.createRoute(loanId))
509: onNavigateBack = {
513: navController.navigate(AppRoute.CreatePayment.createRoute(loanId))
532: onNavigateBack = {
536: navController.navigate(AppRoute.LoanDetail.createRoute(loanId)) {
559: onNavigateBack = {
563: navController.navigate(AppRoute.ClientDetail.createRoute(updatedClientId)) {
587: onNavigateBack = {
591: navController.navigate(AppRoute.LoanDetail.createRoute(updatedLoanId)) {

### Patron: AppRoute
44: navController.navigate(AppRoute.Login.route) {
51: navController.navigate(AppRoute.Dashboard.route) {
57: navController.navigate(AppRoute.Clients.route) {
63: navController.navigate(AppRoute.Payments.route) {
68: navController.navigate(AppRoute.Loans.route) {
71: popUpTo(AppRoute.Dashboard.route) {
77: navController.navigate(AppRoute.More.route) {
80: popUpTo(AppRoute.Dashboard.route) {
88: LocalSecurityRepository.shouldRequirePinOnLaunch(appLockContext) -> AppRoute.Login.route
89: LocalAuthRepository.isSessionActive(appLockContext) -> AppRoute.Dashboard.route
90: else -> AppRoute.Login.route
111: currentRoute != AppRoute.Login.route
113: navController.navigate(AppRoute.Login.route) {
138: composable(AppRoute.AppLock.route) {
141: navController.navigate(AppRoute.Dashboard.route) {
147: navController.navigate(AppRoute.Register.route)
151: composable(AppRoute.Login.route) {
154: navController.navigate(AppRoute.Dashboard.route) {
160: navController.navigate(AppRoute.Register.route)
165: composable(AppRoute.Register.route) {
168: navController.navigate(AppRoute.Dashboard.route) {
179: composable(AppRoute.Dashboard.route) {
182: navController.navigate(AppRoute.Dashboard.route)
185: navController.navigate(AppRoute.Clients.route)
188: navController.navigate(AppRoute.Loans.route)
191: navController.navigate(AppRoute.Payments.route)
194: navController.navigate(AppRoute.More.route)
199: composable(AppRoute.Clients.route) {
217: navController.navigate(AppRoute.CreateClient.route)
220: navController.navigate(AppRoute.ClientDetail.createRoute(clientId))
224: composable(AppRoute.Loans.route) {
242: navController.navigate(AppRoute.LoanDetail.createRoute(loanId))
249: composable(AppRoute.Payments.route) {
267: navController.navigate(AppRoute.CreatePayment.createRoute(loanId))
271: composable(AppRoute.More.route) {
277: navController.navigate(AppRoute.Preferences.route)
280: navController.navigate(AppRoute.Security.route)
283: navController.navigate(AppRoute.Backup.route)
286: navController.navigate(AppRoute.Reports.route)
289: navController.navigate(AppRoute.FinancialAudit.route)
292: navController.navigate(AppRoute.Help.route)
296: navController.navigate(AppRoute.Login.route) {
303: composable(AppRoute.Preferences.route) {
311: composable(AppRoute.Security.route) {
319: composable(AppRoute.Backup.route) {
327: composable(AppRoute.Reports.route) {
335: composable(AppRoute.FinancialAudit.route) {
343: composable(AppRoute.Help.route) {
350: composable(AppRoute.CreateClient.route) {
356: navController.navigate(AppRoute.ClientDetail.createRoute(clientId)) {
357: popUpTo(AppRoute.Clients.route)
364: route = AppRoute.ClientDetail.route,
366: navArgument(AppRoute.ClientDetail.ARG_CLIENT_ID) {
372: ?.getString(AppRoute.ClientDetail.ARG_CLIENT_ID)
381: navController.navigate(AppRoute.CreateLoan.createRoute(clientId))
384: navController.navigate(AppRoute.LoansByClient.createRoute(clientId))
387: navController.navigate(AppRoute.LoanDetail.createRoute(loanId))
390: navController.navigate(AppRoute.LoanDetail.createRoute(loanId))
393: navController.navigate(AppRoute.EditClient.createRoute(clientId))
398: route = AppRoute.LoansByClient.route,
400: navArgument(AppRoute.LoansByClient.ARG_CLIENT_ID) {
406: ?.getString(AppRoute.LoansByClient.ARG_CLIENT_ID)
427: navController.navigate(AppRoute.CreateLoan.createRoute(clientId))
430: navController.navigate(AppRoute.LoanDetail.createRoute(loanId))
434: navController.navigate(AppRoute.CreatePayment.createRoute(loanId))
439: route = AppRoute.CreateLoan.route,
441: navArgument(AppRoute.CreateLoan.ARG_CLIENT_ID) {
447: ?.getString(AppRoute.CreateLoan.ARG_CLIENT_ID)
456: navController.navigate(AppRoute.LoanDetail.createRoute(loanId)) {
457: popUpTo(AppRoute.CreateLoan.createRoute(clientId)) {
466: route = AppRoute.LoanDetail.route,
468: navArgument(AppRoute.LoanDetail.ARG_LOAN_ID) {
474: ?.getString(AppRoute.LoanDetail.ARG_LOAN_ID)
483: navController.navigate(AppRoute.CreatePayment.createRoute(loanId))
486: navController.navigate(AppRoute.PaymentsByLoan.createRoute(loanId))
490: navController.navigate(AppRoute.EditLoan.createRoute(loanId))
496: route = AppRoute.PaymentsByLoan.route,
498: navArgument(AppRoute.PaymentsByLoan.ARG_LOAN_ID) {
504: ?.getString(AppRoute.PaymentsByLoan.ARG_LOAN_ID)
513: navController.navigate(AppRoute.CreatePayment.createRoute(loanId))
519: route = AppRoute.CreatePayment.route,
521: navArgument(AppRoute.CreatePayment.ARG_LOAN_ID) {
527: ?.getString(AppRoute.CreatePayment.ARG_LOAN_ID)
536: navController.navigate(AppRoute.LoanDetail.createRoute(loanId)) {
537: popUpTo(AppRoute.CreatePayment.createRoute(loanId)) {
546: route = AppRoute.EditClient.route,
548: navArgument(AppRoute.EditClient.ARG_CLIENT_ID) {
554: ?.getString(AppRoute.EditClient.ARG_CLIENT_ID)
563: navController.navigate(AppRoute.ClientDetail.createRoute(updatedClientId)) {
564: popUpTo(AppRoute.EditClient.createRoute(updatedClientId)) {
574: route = AppRoute.EditLoan.route,
576: navArgument(AppRoute.EditLoan.ARG_LOAN_ID) {
582: ?.getString(AppRoute.EditLoan.ARG_LOAN_ID)
591: navController.navigate(AppRoute.LoanDetail.createRoute(updatedLoanId)) {
592: popUpTo(AppRoute.EditLoan.createRoute(updatedLoanId)) {

## BackupScreen - exportacion/respaldo existente
Archivo: app/src/main/java/com/controlprestamos/features/backup/presentation/BackupScreen.kt

### Patron: fun BackupScreen
37: fun BackupScreen(

### Patron: Export
70: val exportLauncher = rememberLauncherForActivityResult(
239: exportLauncher.launch(pendingBackupFileName)

### Patron: export
70: val exportLauncher = rememberLauncherForActivityResult(
239: exportLauncher.launch(pendingBackupFileName)

### Patron: Respaldo
208: text = "Crea un archivo de respaldo y guárdalo en Google Drive, Telegram, WhatsApp, correo o computadora.",

### Patron: Backup
1: package com.controlprestamos.features.backup.presentation
34: import com.controlprestamos.features.backup.data.LocalBackupRepository
37: fun BackupScreen(
42: var pendingBackupContent by remember {
46: var pendingBackupFileName by remember {
47: mutableStateOf("control_prestamos_backup.json")
50: var importedBackupContent by remember {
66: var backupReady by rememberSaveable {
78: if (pendingBackupContent.isBlank()) {
86: text = pendingBackupContent
112: importedBackupContent = ""
117: val validationReport = LocalBackupRepository.inspectBackup(content)
120: importedBackupContent = ""
127: importedBackupContent = content
133: val lastBackupLabel = remember(message, backupReady) {
134: LocalBackupRepository.getLastBackupLabel(context)
202: text = "Última copia: $lastBackupLabel",
216: val result = LocalBackupRepository.createBackup(context)
218: pendingBackupContent = result.json
219: pendingBackupFileName = result.fileName
220: backupReady = result.json.isNotBlank()
222: message = if (backupReady) {
230: if (backupReady) {
239: exportLauncher.launch(pendingBackupFileName)
247: val file = BackupShareUtils.createBackupFile(
249: fileName = pendingBackupFileName,
250: content = pendingBackupContent
253: BackupShareUtils.shareBackupFile(
343: importedBackupContent = ""
354: val result = LocalBackupRepository.restoreBackup(
356: backupJson = importedBackupContent
363: importedBackupContent = ""
365: backupReady = false
366: pendingBackupContent = ""

### Patron: PDF
Sin coincidencias.

### Patron: CSV
Sin coincidencias.

### Patron: Button
30: import com.controlprestamos.core.ui.components.PrimaryButton
31: import com.controlprestamos.core.ui.components.SecondaryButton
213: PrimaryButton(
235: SecondaryButton(
243: SecondaryButton(
283: PrimaryButton(
339: SecondaryButton(
350: PrimaryButton(

### Patron: onClick
215: onClick = {
238: onClick = {
246: onClick = {
285: onClick = {
342: onClick = {
353: onClick = {

### Patron: share
247: val file = BackupShareUtils.createBackupFile(
253: BackupShareUtils.shareBackupFile(

### Patron: import
3: import android.content.Context
4: import android.net.Uri
5: import androidx.activity.compose.rememberLauncherForActivityResult
6: import androidx.activity.result.contract.ActivityResultContracts
7: import androidx.compose.foundation.layout.Arrangement
8: import androidx.compose.foundation.layout.Column
9: import androidx.compose.foundation.layout.Row
10: import androidx.compose.foundation.layout.fillMaxSize
11: import androidx.compose.foundation.layout.fillMaxWidth
12: import androidx.compose.foundation.layout.padding
13: import androidx.compose.foundation.rememberScrollState
14: import androidx.compose.foundation.verticalScroll
15: import androidx.compose.material3.MaterialTheme
16: import androidx.compose.material3.Scaffold
17: import androidx.compose.material3.Surface
18: import androidx.compose.material3.Text
19: import androidx.compose.runtime.Composable
20: import androidx.compose.runtime.getValue
21: import androidx.compose.runtime.mutableStateOf
22: import androidx.compose.runtime.remember
23: import androidx.compose.runtime.saveable.rememberSaveable
24: import androidx.compose.runtime.setValue
25: import androidx.compose.ui.Modifier
26: import androidx.compose.ui.platform.LocalContext
27: import androidx.compose.ui.text.font.FontWeight
28: import com.controlprestamos.core.ui.components.AppCard
29: import com.controlprestamos.core.ui.components.AppTopBar
30: import com.controlprestamos.core.ui.components.PrimaryButton
31: import com.controlprestamos.core.ui.components.SecondaryButton
32: import com.controlprestamos.core.ui.theme.AppColors
33: import com.controlprestamos.core.ui.theme.AppSpacing
34: import com.controlprestamos.features.backup.data.LocalBackupRepository
50: var importedBackupContent by remember {
96: val importLauncher = rememberLauncherForActivityResult(
112: importedBackupContent = ""
120: importedBackupContent = ""
127: importedBackupContent = content
286: importLauncher.launch(
343: importedBackupContent = ""
356: backupJson = importedBackupContent
363: importedBackupContent = ""

## ReportsScreen - exportacion existente en reportes
Archivo: app/src/main/java/com/controlprestamos/features/reports/presentation/ReportsScreen.kt

### Patron: fun ReportsScreen
40: fun ReportsScreen(

### Patron: Export
49: onExportReports: () -> Unit = {},
50: onOpenExport: () -> Unit = onExportReports,
173: ExportAndAuditCard(
175: onOpenExport = onOpenExport
435: private fun ExportAndAuditCard(
437: onOpenExport: () -> Unit
441: title = "Exportación y respaldo",
446: title = "Exportar reporte",
448: primaryText = "Exportar",
449: onPrimaryClick = onOpenExport

### Patron: export
49: onExportReports: () -> Unit = {},
50: onOpenExport: () -> Unit = onExportReports,
173: ExportAndAuditCard(
175: onOpenExport = onOpenExport
435: private fun ExportAndAuditCard(
437: onOpenExport: () -> Unit
441: title = "Exportación y respaldo",
446: title = "Exportar reporte",
448: primaryText = "Exportar",
449: onPrimaryClick = onOpenExport

### Patron: PDF
Sin coincidencias.

### Patron: CSV
Sin coincidencias.

### Patron: Respaldo
441: title = "Exportación y respaldo",
442: subtitle = "Prepara información para revisión o respaldo antes de cambios mayores."
453: title = "Respaldo",
455: primaryText = "Abrir respaldo",

### Patron: Button
27: import com.controlprestamos.core.ui.components.PrimaryButton
28: import com.controlprestamos.core.ui.components.SecondaryButton
185: SecondaryButton(
560: PrimaryButton(

### Patron: onClick
187: onClick = onBack
562: onClick = onPrimaryClick

### Patron: onOpenBackup
48: onOpenBackup: () -> Unit = {},
174: onOpenBackup = onOpenBackup,
436: onOpenBackup: () -> Unit,
456: onPrimaryClick = onOpenBackup

### Patron: onExport
49: onExportReports: () -> Unit = {},
50: onOpenExport: () -> Unit = onExportReports,

## APK
_secure_local\release\apk\ControlPrestamos_v1.1.0_dev_bloque_11a_login_icons_signed_20260614-203426.apk

## SHA256
695F8FECDB4AF9AA6949C4ED7C04D239889B15B75747A9BB81CC7B752E9668D5

## Backup local
_local_archives\v1_1_bloque_11a_before_20260614-203426

## Propuesta para Bloque 11B
- Simplificar pantalla Mas.
- Quitar tarjeta informativa innecesaria.
- Quitar accesos rapidos duplicados.
- Llevar informacion de app a Acerca de.
- Completar contenido real de Ayuda.
- Corregir o eliminar Preferencias que no guardan cambios.
- Evitar exportacion duplicada si Backup/Reportes ya cubren exportaciones.
