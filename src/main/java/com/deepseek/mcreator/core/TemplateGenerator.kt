package com.deepseek.mcreator.core

object TemplateGenerator {
    fun generateTemplate(elementType: String, style: String = "default"): String {
        return when (elementType.toLowerCase()) {
            "item" -> when (style.toLowerCase()) {
                "avanzado" -> advancedItemTemplate()
                "optimizado" -> optimizedItemTemplate()
                else -> basicItemTemplate()
            }
            "block" -> when (style.toLowerCase()) {
                "avanzado" -> advancedBlockTemplate()
                "optimizado" -> optimizedBlockTemplate()
                else -> basicBlockTemplate()
            }
            // Añadir más plantillas según necesidad
            else -> "Plantilla no disponible para $elementType"
        }
    }

    private fun basicItemTemplate(): String {
        return """
            public class CustomItem extends Item {
                public CustomItem() {
                    super(new Properties()
                        .tab(CreativeModeTab.TAB_MISC)
                        .stacksTo(64)
                    );
                }
                
                @Override
                public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
                    // Lógica de uso del ítem
                    return super.use(world, player, hand);
                }
            }
        """.trimIndent()
    }

    // Implementa las demás plantillas de manera similar
}