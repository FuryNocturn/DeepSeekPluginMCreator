package com.deepseek.mcreator

import net.mcreator.plugin.JavaPlugin
import net.mcreator.plugin.Plugin
import net.mcreator.plugin.IPlugin
import net.mcreator.plugin.events.ApplicationLoadedEvent
import net.mcreator.ui.MCreator

@JavaPlugin(
    name = "DeepSeek Assistant",
    version = "2.0",
    author = "FuryNocturnTV",
    description = "Asistente de IA para desarrollo de mods"
)
class DeepSeekPlugin : IPlugin {
    override fun initialize(plugin: Plugin) {
        EventProvider.registerListener(this, ApplicationLoadedEvent::class.java) { event ->
            val mcreator = event.mcreator
            val panel = MainPanel(mcreator)
            mcreator.application.mainView.addView(panel)

            // Añadir menú
            mcreator.application.mainMenuBar.toolsMenu.addSeparator()
            mcreator.application.mainMenuBar.toolsMenu.add(JMenuItem("DeepSeek Assistant").apply {
                addActionListener { panel.showView() }
            })
        }
    }
}