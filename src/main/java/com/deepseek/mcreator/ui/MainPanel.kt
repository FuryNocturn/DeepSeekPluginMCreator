ppackage com.deepseek.mcreator.ui

import com.deepseek.mcreator.core.*
import net.mcreator.ui.MCreator
import net.mcreator.ui.component.util.PanelUtils
import net.mcreator.ui.init.L10N
import net.mcreator.ui.laf.SlickDarkScrollBarUI
import java.awt.*
import java.awt.event.ActionEvent
import java.io.File
import javax.swing.*
import javax.swing.border.EmptyBorder

class MainPanel(val mcreator: MCreator) : JPanel(BorderLayout()) {
    private val client = DeepSeekClient()
    private val historyManager = HistoryPanel()
    private val templateGenerator = TemplateGenerator()

    // Componentes UI
    private val chatArea = JTextPane().apply {
        contentType = "text/html"
        isEditable = false
        background = Color(30, 30, 30)
        margin = Insets(10, 10, 10, 10)
    }

    private val scrollPane = JScrollPane(chatArea).apply {
        border = null
        verticalScrollBar.ui = SlickDarkScrollBarUI()
        horizontalScrollBar.ui = SlickDarkScrollBarUI()
    }

    private val inputField = JTextField().apply {
        preferredSize = Dimension(400, 32)
        font = Font("Arial", Font.PLAIN, 14)
    }

    private val modelSelector = JComboBox(arrayOf("deepseek-chat", "deepseek-coder")).apply {
        addActionListener {
            client.selectedModel = selectedItem as String
        }
    }

    private val temperatureSlider = JSlider(0, 100, 70).apply {
        toolTipText = "Creatividad (Temperatura)"
        addChangeListener {
            client.temperature = value / 100.0
        }
    }

    private val costLabel = JLabel("Costo acumulado: $0.00").apply {
        foreground = Color(150, 150, 255)
        font = font.deriveFont(Font.BOLD)
    }

    private val tokenLabel = JLabel("Tokens usados: 0").apply {
        foreground = Color(180, 180, 180)
    }

    private val statusPanel = JPanel(FlowLayout(FlowLayout.RIGHT)).apply {
        add(tokenLabel)
        add(Box.createHorizontalStrut(15))
        add(costLabel)
    }

    init {
        border = EmptyBorder(10, 10, 10, 10)
        background = Color(45, 45, 45)

        setupUI()
        setupInitialConfig()
    }

    private fun setupUI() {
        // Panel superior
        add(createTopPanel(), BorderLayout.NORTH)

        // Panel central
        add(createCenterPanel(), BorderLayout.CENTER)

        // Panel inferior
        add(createBottomPanel(), BorderLayout.SOUTH)
    }

    private fun createTopPanel(): JPanel {
        return JPanel(BorderLayout()).apply {
            background = Color(60, 60, 60)

            // Panel de control izquierdo
            val leftPanel = JPanel(FlowLayout(FlowLayout.LEFT, 5, 5)).apply {
                background = Color(60, 60, 60)
                add(createButton("Configuración") { showConfigDialog() })
                add(createButton("Historial") { historyManager.showHistory() })
                add(createButton("Plantillas") { showTemplatesDialog() })
            }

            // Panel de control derecho
            val rightPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 5, 5)).apply {
                background = Color(60, 60, 60)
                add(JLabel("Modelo:"))
                add(modelSelector)
                add(JLabel("Creatividad:"))
                add(temperatureSlider)
            }

            add(leftPanel, BorderLayout.WEST)
            add(rightPanel, BorderLayout.CENTER)
            add(statusPanel, BorderLayout.EAST)
        }
    }

    private fun createCenterPanel(): JComponent {
        return JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            scrollPane,
            createSidePanel()
        ).apply {
            dividerLocation = 700
            resizeWeight = 0.7
        }
    }

    private fun createSidePanel(): JPanel {
        return JPanel(BorderLayout()).apply {
            background = Color(50, 50, 50)
            border = EmptyBorder(5, 5, 5, 5)

            val tabbedPane = JTabbedPane().apply {
                addTab("Contexto", createContextPanel())
                addTab("Acciones Rápidas", createQuickActionsPanel())
            }

            add(tabbedPane, BorderLayout.CENTER)
        }
    }

    private fun createContextPanel(): JComponent {
        val contextArea = JTextArea(generateWorkspaceContext()).apply {
            editable = false
            lineWrap = true
            wrapStyleWord = true
            background = Color(60, 60, 60)
            foreground = Color(200, 200, 200)
        }

        return JScrollPane(contextArea).apply {
            border = null
        }
    }

    private fun generateWorkspaceContext(): String {
        return buildString {
            append("### Análisis del Workspace ###\n\n")
            append("Nombre: ${mcreator.workspace.workspaceName}\n")
            append("Elementos: ${mcreator.workspace.modElements.size}\n")
            append("Texturas: ${mcreator.workspace.folderManager.textures.list().size}\n\n")

            append("Distribución de Elementos:\n")
            mcreator.workspace.modElements
                .groupBy { it.type }
                .forEach { (type, elements) ->
                    append("- ${type.name}: ${elements.size}\n")
                }

            append("\nProblemas Recientes:\n${getRecentErrors()}")
        }
    }

    private fun createQuickActionsPanel(): JPanel {
        return JPanel(GridLayout(0, 1, 5, 5)).apply {
            background = Color(60, 60, 60)

            add(createButton("Analizar Código Actual") { analyzeCurrentCode() })
            add(createButton("Optimizar Texturas") { optimizeTextures() })
            add(createButton("Generar Documentación") { generateDocumentation() })
            add(createButton("Reporte de Errores") { generateErrorReport() })
        }
    }

    private fun createBottomPanel(): JPanel {
        return JPanel(BorderLayout(5, 5)).apply {
            add(inputField, BorderLayout.CENTER)

            val buttonPanel = JPanel(GridLayout(1, 0, 5, 5)).apply {
                add(createButton("Enviar", Color(70, 130, 180)) { sendMessage() }
                        add(createButton("Adjuntar", Color(100, 100, 160)) { attachFile() }
                        add(createButton("Limpiar", Color(160, 80, 80)) { clearChat() }
            }

            add(buttonPanel, BorderLayout.EAST)
        }
    }

    // Resto de implementaciones (sendMessage, analyzeCurrentCode, etc.)
    // ... [Mantén las implementaciones anteriores pero actualizadas para usar los nuevos componentes]

    private fun showTemplatesDialog() {
        val dialog = JDialog()
        dialog.title = "Generador de Plantillas"
        dialog.setSize(600, 400)
        dialog.layout = BorderLayout()

        val elementTypeCombo = JComboBox(arrayOf("Item", "Block", "Entity", "GUI"))
        val styleCombo = JComboBox(arrayOf("Básico", "Avanzado", "Optimizado"))
        val generateButton = JButton("Generar")
        val templateArea = JTextArea().apply {
            editable = false
            lineWrap = true
            wrapStyleWord = true
        }

        generateButton.addActionListener {
            templateArea.text = templateGenerator.generateTemplate(
                elementTypeCombo.selectedItem as String,
                styleCombo.selectedItem as String
            )
        }

        val controlPanel = JPanel().apply {
            add(JLabel("Tipo de Elemento:"))
            add(elementTypeCombo)
            add(JLabel("Estilo:"))
            add(styleCombo)
            add(generateButton)
        }

        dialog.add(controlPanel, BorderLayout.NORTH)
        dialog.add(JScrollPane(templateArea), BorderLayout.CENTER)

        dialog.isVisible = true
    }

    private fun optimizeTextures() {
        val textureFiles = mcreator.workspace.folderManager.textures.listFiles()
            .filter { it.extension in setOf("png", "jpg") }

        if (textureFiles.isEmpty()) {
            appendToChat("Sistema", "No se encontraron texturas para optimizar", Color(255, 120, 120))
            return
        }

        appendToChat("Sistema", "Analizando ${textureFiles.size} texturas...", Color(255, 215, 0))

        Thread {
            textureFiles.forEach { file ->
                try {
                    val analysis = client.analyzeTexture(file)
                    SwingUtilities.invokeLater {
                        appendToChat("DeepSeek", "Análisis de ${file.name}:\n$analysis", Color(180, 255, 180))
                    }
                } catch (e: Exception) {
                    ErrorReporter.reportError(e)
                    SwingUtilities.invokeLater {
                        appendToChat("Error", "Fallo al analizar ${file.name}: ${e.message}", Color(255, 120, 120))
                    }
                }
            }
        }.start()
    }

    private fun generateDocumentation() {
        val currentElement = mcreator.currentModElement ?: run {
            appendToChat("Sistema", "Selecciona un elemento primero", Color(255, 120, 120))
            return
        }

        appendToChat("Sistema", "Generando documentación para ${currentElement.name}...", Color(255, 215, 0))

        Thread {
            try {
                val prompt = """
                    Genera documentación profesional en formato Markdown para este elemento de MCreator:
                    - Nombre: ${currentElement.name}
                    - Tipo: ${currentElement.typeString}
                    
                    Incluye:
                    1. Descripción general
                    2. Parámetros configurados
                    3. Métodos principales
                    4. Ejemplos de uso
                    5. Notas importantes
                """.trimIndent()

                val (response, _, _) = client.sendPrompt(prompt)

                SwingUtilities.invokeLater {
                    val docDialog = JDialog()
                    docDialog.title = "Documentación: ${currentElement.name}"
                    docDialog.setSize(700, 500)

                    val docArea = JTextArea(response).apply {
                        editable = false
                        font = Font("Monospaced", Font.PLAIN, 12)
                    }

                    docDialog.add(JScrollPane(docArea))
                    docDialog.isVisible = true
                }
            } catch (e: Exception) {
                ErrorReporter.reportError(e)
                SwingUtilities.invokeLater {
                    appendToChat("Error", "Fallo al generar documentación: ${e.message}", Color(255, 120, 120))
                }
            }
        }.start()
    }

    private fun generateErrorReport() {
        val report = buildString {
            appendLine("### Reporte de Errores ###")
            appendLine("Workspace: ${mcreator.workspace.workspaceName}")
            appendLine("Fecha: ${java.time.LocalDate.now()}")
            appendLine("\n## Estadísticas ##")
            appendLine(ErrorReporter.getStats())
            appendLine("\n## Errores Recientes ##")
            appendLine(ErrorReporter.getRecentErrors(5).joinToString("\n"))
        }

        val reportDialog = JDialog()
        reportDialog.title = "Reporte de Errores"
        reportDialog.setSize(600, 400)

        val reportArea = JTextArea(report).apply {
            editable = false
            font = Font("Monospaced", Font.PLAIN, 12)
        }

        reportDialog.add(JScrollPane(reportArea))
        reportDialog.isVisible = true
    }

    private fun attachFile() {
        val fileChooser = JFileChooser().apply {
            fileSelectionMode = JFileChooser.FILES_ONLY
            multiSelectionEnabled = true
        }

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileChooser.selectedFiles.forEach { file ->
                when (file.extension.toLowerCase()) {
                    "png", "jpg", "jpeg" -> {
                        appendToChat("Sistema", "Analizando imagen: ${file.name}...", Color(255, 215, 0))
                        analyzeImageFile(file)
                    }
                    "java", "txt", "json" -> {
                        appendToChat("Sistema", "Analizando archivo: ${file.name}...", Color(255, 215, 0))
                        analyzeTextFile(file)
                    }
                    else -> appendToChat("Sistema", "Formato no soportado: ${file.name}", Color(255, 120, 120))
                }
            }
        }
    }

    private fun analyzeImageFile(file: File) {
        Thread {
            try {
                val analysis = client.analyzeTexture(file)
                SwingUtilities.invokeLater {
                    appendToChat("DeepSeek", "Análisis de ${file.name}:\n$analysis", Color(180, 255, 180))
                }
            } catch (e: Exception) {
                ErrorReporter.reportError(e)
                SwingUtilities.invokeLater {
                    appendToChat("Error", "Fallo al analizar imagen: ${e.message}", Color(255, 120, 120))
                }
            }
        }.start()
    }

    private fun analyzeTextFile(file: File) {
        Thread {
            try {
                val content = file.readText()
                val prompt = """
                    Analiza este archivo ${file.extension}:
                    ```
                    $content
                    ```
                    
                    Proporciona:
                    1. Resumen del contenido
                    2. Problemas potenciales
                    3. Sugerencias de mejora
                """.trimIndent()

                val (response, _, _) = client.sendPrompt(prompt)

                SwingUtilities.invokeLater {
                    appendToChat("DeepSeek", "Análisis de ${file.name}:\n$response", Color(180, 255, 180))
                }
            } catch (e: Exception) {
                ErrorReporter.reportError(e)
                SwingUtilities.invokeLater {
                    appendToChat("Error", "Fallo al analizar archivo: ${e.message}", Color(255, 120, 120))
                }
            }
        }.start()
    }

    private fun clearChat() {
        chatArea.text = ""
    }

    // [Mantén los demás métodos como appendToChat, updateCostDisplay, etc.]
}