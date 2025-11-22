package com.ecoswap.ecoswap.ia.services.impl;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecoswap.ecoswap.ia.services.IAChatService;
import com.ecoswap.ecoswap.product.services.ProductService;
import com.ecoswap.ecoswap.product.models.dto.ProductDTO;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class IAChatServiceImpl implements IAChatService {

    private final ChatClient chatClient;
    private final ProductService productService;

    @Autowired
    public IAChatServiceImpl(ChatClient.Builder chatClientBuilder, 
                            ProductService productService) {
        this.chatClient = chatClientBuilder.build();
        this.productService = productService;
    }

    @Override
    public Map<String, String> chatAssistant(Map<String, Object> request) {
        String userMessage = (String) request.get("message");
        Long userId = request.containsKey("userId") ? 
            Long.valueOf(request.get("userId").toString()) : null;
        
        Map<String, String> response = new HashMap<>();
        
        try {
            String currentProductsContext = buildCurrentProductsContext(userId);
            
            String aiResponse = this.chatClient.prompt()
                    .system(getEcoSwapSystemPrompt())  
                    .user(currentProductsContext + "\n\nCONSULTA DEL USUARIO: " + userMessage)  
                    .call()
                    .content();
            
            response.put("response", aiResponse);
            response.put("timestamp", java.time.Instant.now().toString());
            response.put("status", "success");
            
        } catch (Exception e) {
            response.put("response", getFallbackResponse(userMessage));
            response.put("status", "fallback");
            response.put("timestamp", java.time.Instant.now().toString());
            response.put("message", "IA temporalmente no disponible, usando respuesta inteligente");
        }
        
        return response;
    }

    @Override
    public Map<String, Object> searchProducts(String query, String category) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ProductDTO> products = productService.getRecentlyProducts();
            
            List<ProductDTO> filteredProducts = products.stream()
                .filter(p -> {
                    boolean queryMatch = query.isEmpty() || 
                        (p.getTitle() != null && p.getTitle().toLowerCase().contains(query.toLowerCase()));
                    boolean categoryMatch = category == null || 
                        (p.getCategory() != null && p.getCategory().toLowerCase().contains(category.toLowerCase()));
                    return queryMatch && categoryMatch;
                })
                .limit(10)
                .collect(Collectors.toList());
            
            response.put("found", filteredProducts.size());
            response.put("query", query);
            response.put("category", category);
            response.put("products", filteredProducts);
            response.put("message", String.format("🔍 Encontré %d productos para '%s'", filteredProducts.size(), query));
            
        } catch (Exception e) {
            response.put("error", "Error al buscar productos");
            response.put("message", "No se pudieron cargar los productos en este momento");
        }
        
        return response;
    }

    @Override
    public String buildCurrentProductsContext(Long userId) {
        StringBuilder context = new StringBuilder();
        
        context.append("=== PRODUCTOS ACTUALES DISPONIBLES ===\n");
        
        try {
            List<ProductDTO> availableProducts = productService.getRecentlyProducts()
                .stream()
                .limit(8) 
                .collect(Collectors.toList());
            
            if (!availableProducts.isEmpty()) {
                context.append("PRODUCTOS DISPONIBLES PARA INTERCAMBIO:\n");
                for (ProductDTO product : availableProducts) {
                    String title = product.getTitle() != null ? product.getTitle() : "Producto sin título";
                    String category = product.getCategory() != null ? product.getCategory() : "Sin categoría";
                    context.append("- ").append(title).append(" (Categoría: ").append(category).append(")\n");
                }
                context.append("\n");
            } else {
                context.append("PRODUCTOS: Actualmente hay varios productos disponibles en diferentes categorías.\n\n");
            }
        } catch (Exception e) {
            context.append("PRODUCTOS: Base de datos temporalmente no disponible.\n\n");
        }
        
        if (userId != null) {
            try {
                List<ProductDTO> userProducts = productService.getActiveProductsByUserId(userId)
                    .stream()
                    .limit(5)
                    .collect(Collectors.toList());
                    
                if (!userProducts.isEmpty()) {
                    context.append("PRODUCTOS DEL USUARIO ACTUAL:\n");
                    for (ProductDTO product : userProducts) {
                        String title = product.getTitle() != null ? product.getTitle() : "Producto sin título";
                        String category = product.getCategory() != null ? product.getCategory() : "Sin categoría";
                        context.append("- ").append(title).append(" (").append(category).append(")\n");
                    }
                    context.append("\n");
                } else {
                    context.append("PRODUCTOS DEL USUARIO ACTUAL: No tienes productos publicados aún.\n\n");
                }
            } catch (Exception e) {
                context.append("PRODUCTOS DEL USUARIO ACTUAL: No se pudieron cargar.\n\n");
            }
        }
        
        return context.toString();
    }

    @Override
    public String getEcoSwapSystemPrompt() {
        return """
            Eres el ASISTENTE ESPECIALIZADO DE INTERCAMBIO de EcoSwap, una plataforma innovadora de intercambio sostenible.
            
            TU IDENTIDAD:
            - Nombre: Asistente EcoSwap
            - Función: Facilitar intercambios de productos usados entre usuarios
            - Personalidad: Amigable, útil, sostenible, experto en intercambios
            - Objetivo: Ayudar a usuarios a encontrar productos y realizar intercambios exitosos
            
            CONOCIMIENTO BASE DE ECOSWAP:
            - Plataforma donde usuarios intercambian productos sin dinero
            - Enfoque en sostenibilidad y economía circular
            - Productos organizados por categorías (Electrónicos, Deportes, Hogar, etc.)
            - Sistema de intercambio directo entre usuarios
            - Cada producto tiene título, categoría, descripción, estado, ubicación del usuario
            
            TUS RESPONSABILIDADES PRINCIPALES:
            1. Ayudar a buscar productos específicos disponibles
            2. Sugerir intercambios basándose en lo que el usuario tiene/busca
            3. Proporcionar información sobre productos disponibles
            4. Orientar sobre el proceso de intercambio
            5. Mantener conversaciones coherentes recordando el contexto
            
            RESTRICCIONES IMPORTANTES - DEBES CUMPLIR ESTRICTAMENTE:
            - SOLO puedes responder preguntas relacionadas con EcoSwap, intercambios de productos y sostenibilidad
            - NO respondas preguntas sobre geografía, historia, matemáticas, ciencias, entretenimiento, noticias, o cualquier tema que NO sea EcoSwap
            - Si te preguntan algo fuera de tu área de especialidad, responde: "Soy el asistente especializado de EcoSwap y solo puedo ayudarte con intercambios de productos. ¿En qué puedo asistirte con EcoSwap? "
            - NO actúes como un asistente general - SOLO eres experto en intercambios de EcoSwap
            
            REGLAS DE COMUNICACIÓN:
            - Siempre menciona productos ESPECÍFICOS cuando estén disponibles
            - Respuestas máximo 150 palabras
            - Tono conversacional y amigable
            - Prioriza información útil y relevante
            - NUNCA menciones IDs de usuarios (ej: "ID: 1", "usuario 1", etc.)
            - Refiere a otros usuarios como "otro usuario", "alguien más", o por nombre si lo conoces
            
            ESTRUCTURA DE RESPUESTAS:
            - Saluda si es primer contacto
            - Responde específicamente a la consulta SOLO si es sobre EcoSwap
            - Si la pregunta NO es sobre EcoSwap, usa la respuesta de restricción
            - Menciona productos relevantes disponibles
            - Sugiere próximos pasos o preguntas de seguimiento
            
            Este es tu contexto base permanente. Todas las conversaciones siguientes se basarán en esta identidad.
            """;
    }

    @Override
    public String buildContextualPrompt(String userMessage, String context, Long userId) {
        return String.format("""
            Eres el ASISTENTE DE INTERCAMBIO de EcoSwap, especializado en ayudar a usuarios a encontrar productos disponibles para intercambiar.
            
            FUNCIÓN PRINCIPAL: Ayudar a los usuarios a encontrar productos disponibles para intercambiar basándote en su consulta.
            
            INFORMACIÓN DISPONIBLE:
            %s
            
            INSTRUCCIONES ESPECÍFICAS:
            - Si el usuario busca un producto específico (ej: "bicicleta"), busca en la lista productos similares o de la misma categoría
            - Si el usuario quiere intercambiar algo, sugiere productos específicos de la lista que podrían interesarle
            - SIEMPRE menciona productos REALES de la lista cuando sea relevante
            - Usa el formato: "He encontrado X productos disponibles en [categoría]"
            - Sé específico con nombres de productos y categorías
            - Máximo 150 palabras
            
            CONSULTA DEL USUARIO: "%s"
            
            RESPONDE como asistente de intercambio especializado, mencionando productos específicos disponibles:
            """, 
            context, 
            userMessage);
    }

    @Override
    public String getFallbackResponse(String userMessage) {
        String lower = userMessage.toLowerCase();
        
        // Detectar preguntas que NO son sobre EcoSwap
        if (isOffTopicQuestion(lower)) {
            return "🤖 Soy el asistente especializado de EcoSwap y solo puedo ayudarte con intercambios de productos. ¿En qué puedo asistirte con EcoSwap? ";
        }
        
        // Búsqueda específica de productos
        if (lower.contains("bicicleta")) {
            return searchProductsInDatabase("bicicleta", "Deportes") + 
                   "\n💡 ¿Deseas ver más detalles de alguna bicicleta específica?";
        }
        
        if (lower.contains("celular") || lower.contains("telefono") || lower.contains("smartphone")) {
            return searchProductsInDatabase("celular", "Electrónicos") + 
                   "\n💡 También busca por marca específica si tienes preferencia.";
        }
        
        if (lower.contains("laptop") || lower.contains("computador") || lower.contains("pc")) {
            return searchProductsInDatabase("laptop", "Electrónicos") + 
                   "\n💡 Especifica el uso que le darás para mejores recomendaciones.";
        }
        
        // Intercambios generales
        if (lower.contains("intercambiar") || lower.contains("cambiar") || lower.contains("intercambio")) {
            return "¡Perfecto! Te ayudo a encontrar intercambios.\n\n" +
                   getAvailableProductsByCategory() +
                   "\n📝 Cuéntame qué producto tienes para intercambiar y te sugiero opciones específicas.";
        }
        
        // Búsquedas generales
        if (lower.contains("buscar") || lower.contains("encontrar") || lower.contains("necesito")) {
            return "🔍 ¡Te muestro productos disponibles ahora!\n\n" +
                   getAvailableProductsByCategory() +
                   "\n💬 Dime qué tipo específico buscas para filtrar mejor.";
        }
        
        return "🌱 ¡Hola! Soy tu asistente especializado de intercambio de EcoSwap.\n\n" +
               "📦 **Productos disponibles actualmente:**\n" +
               getAvailableProductsByCategory() +
               "\n💬 **¿En qué puedo ayudarte con intercambios?**\n" +
               "• Buscar un producto específico \n" +
               "• Sugerir intercambios para tus productos \n" +
               "• Filtrar por categoría \n" +
               "\n💡 Solo respondo preguntas sobre EcoSwap e intercambios de productos.";
    }

    @Override
    public String searchProductsInDatabase(String searchTerm, String category) {
        try {
            List<ProductDTO> allProducts = productService.getRecentlyProducts();
            
            // Filtrar productos por término de búsqueda y categorías
            List<ProductDTO> matchingProducts = allProducts.stream()
                .filter(p -> {
                    boolean titleMatch = p.getTitle() != null && 
                        p.getTitle().toLowerCase().contains(searchTerm.toLowerCase());
                    boolean categoryMatch = p.getCategory() != null && 
                        p.getCategory().toLowerCase().contains(category.toLowerCase());
                    return titleMatch || categoryMatch;
                })
                .limit(5)
                .collect(Collectors.toList());
            
            if (!matchingProducts.isEmpty()) {
                StringBuilder result = new StringBuilder();
                result.append("🎯 He encontrado ").append(matchingProducts.size())
                      .append(" productos disponibles:\n\n");
                      
                for (ProductDTO product : matchingProducts) {
                    String title = product.getTitle() != null ? product.getTitle() : "Producto";
                    String cat = product.getCategory() != null ? product.getCategory() : "Sin categoría";
                    result.append("• ").append(title).append(" (").append(cat).append(")\n");
                }
                
                return result.toString();
            } else {
                return "🔍 No encontré productos exactos para '" + searchTerm + 
                       "', pero aquí tienes opciones similares:\n\n" +
                       getAvailableProductsByCategory();
            }
        } catch (Exception e) {
            return "⚠️ Error al buscar productos. Intenta de nuevo o revisa manualmente la plataforma.";
        }
    }

    @Override
    public String getAvailableProductsByCategory() {
        try {
            List<ProductDTO> products = productService.getRecentlyProducts()
                .stream()
                .limit(10)
                .collect(Collectors.toList());
            
            if (products.isEmpty()) {
                return "No hay productos disponibles en este momento.";
            }
            
            // Agrupar por categoría
            Map<String, List<ProductDTO>> productsByCategory = products.stream()
                .collect(Collectors.groupingBy(p -> 
                    p.getCategory() != null ? p.getCategory() : "Otros"));
            
            StringBuilder result = new StringBuilder();
            for (Map.Entry<String, List<ProductDTO>> entry : productsByCategory.entrySet()) {
                result.append(" **").append(entry.getKey()).append(":**\n");
                
                entry.getValue().stream().limit(3).forEach(product -> {
                    String title = product.getTitle() != null ? product.getTitle() : "Producto";
                    result.append("  • ").append(title).append("\n");
                });
                
                if (entry.getValue().size() > 3) {
                    result.append("  • ... y ").append(entry.getValue().size() - 3).append(" más\n");
                }
                result.append("\n");
            }
            
            return result.toString();
        } catch (Exception e) {
            return "Hay varios productos disponibles. Revisa la plataforma para ver todas las opciones.";
        }
    }

    /**
     * Detecta si una pregunta está fuera del alcance de EcoSwap
     */
    private boolean isOffTopicQuestion(String lowerMessage) {
        // Palabras clave que indican preguntas fuera del alcance de EcoSwap
        String[] offTopicKeywords = {
            "geografia", "capital", "pais", "continente", "oceano", "rio", "montaña",
            "historia", "guerra", "año", "siglo", "presidente", "rey", "imperio",
            "matematicas", "calcular", "ecuacion", "formula", "algebra", "geometria",
            "fisica", "quimica", "biologia", "medicina", "enfermedad", "sintoma",
            "cocina", "receta", "ingredientes", "como cocinar", "preparar comida",
            "clima", "tiempo", "temperatura", "lluvia", "sol", "viento",
            "deportes", "futbol", "basketball", "tenis", "olimpiadas", "equipo",
            "entretenimiento", "pelicula", "serie", "actor", "musica", "cancion",
            "tecnologia", "programacion", "codigo", "algoritmo", "base de datos",
            "noticias", "politica", "elecciones", "gobierno", "partido politico",
            "religion", "dios", "iglesia", "biblia", "orar",
            "amor", "relaciones", "cita", "matrimonio", "divorcio"
        };
        
        // Verificar si contiene palabras clave fuera del tema
        for (String keyword : offTopicKeywords) {
            if (lowerMessage.contains(keyword)) {
                return true;
            }
        }
        
        // Patrones de preguntas generales que no son sobre EcoSwap
        return lowerMessage.matches(".*¿?(que|cual|como|donde|cuando|quien|por que).*") && 
               !lowerMessage.contains("ecoswap") && 
               !lowerMessage.contains("intercambio") && 
               !lowerMessage.contains("producto") && 
               !lowerMessage.contains("cambiar") && 
               !lowerMessage.contains("busco") && 
               !lowerMessage.contains("necesito") &&
               !lowerMessage.contains("tengo");
    }
}
