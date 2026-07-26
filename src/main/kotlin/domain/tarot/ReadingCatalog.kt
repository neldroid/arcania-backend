package domain.tarot

/**
 * Authoritative, backend-owned definition of every tarot reading the product
 * offers. The frontend keeps its own catalog for presentation (names, hooks,
 * prices, the "technical info" dialog) in lib/tarotServices.ts and
 * lib/tarotCardPositions.ts; this one owns the *interpretation logic*:
 *
 *  - [positions] is the ordered, localized (Español neutro) meaning of each
 *    drawn card. Its size — NOT any client-supplied number — is how many cards
 *    a reading draws, keeping the deck honest even if a request is tampered with.
 *  - [promptGuidance] is the per-reading interpretive lens injected into the
 *    agent's system prompt so each spread reads with its own intent and voice.
 *
 * [positions] here mirrors TAROT_CARD_POSITIONS in the frontend and the two
 * must stay in sync; a divergence only affects the semantic labels, never the
 * output contract.
 */
data class ReadingDefinition(
    val id: String,
    val displayName: String,
    val positions: List<String>,
    val promptGuidance: String,
    /** Whether the querent must supply a question. False for open, topic-less readings like "signal". */
    val questionRequired: Boolean = true,
) {
    val cardsQuantity: Int get() = positions.size
}

object ReadingCatalog {

    private val definitions: List<ReadingDefinition> = listOf(
        ReadingDefinition(
            id = "single",
            displayName = "Lectura del presente",
            positions = listOf("El presente"),
            promptGuidance = "Tirada de una sola carta para el momento actual. Da una respuesta directa y enfocada a la pregunta, sin dispersarte: una idea central, clara y accionable.",
        ),
        ReadingDefinition(
            id = "three",
            displayName = "Pasado - Presente - Futuro",
            positions = listOf("Pasado", "Presente", "Futuro"),
            promptGuidance = "Tirada de línea temporal. Muestra cómo lo vivido (Pasado) da forma al momento actual (Presente) y hacia dónde tiende la situación (Futuro). Enfatiza la continuidad y el hilo que conecta las tres cartas.",
        ),
        ReadingDefinition(
            id = "key",
            displayName = "La Llave",
            positions = listOf(
                "Situación actual",
                "Lo que bloquea el camino",
                "Causa raíz del bloqueo",
                "Recursos internos disponibles",
                "Apoyo externo disponible",
                "Acción a tomar",
                "Riesgo a evitar",
                "Resultado si abres la llave",
            ),
            promptGuidance = "Tirada de desbloqueo. Algo no avanza y esta lectura revela por qué. Distingue con claridad entre el bloqueo (síntoma) y su causa raíz, contrasta los recursos internos con el apoyo externo, y cierra con una acción concreta, el riesgo a evitar y el resultado posible si la persona actúa.",
        ),
        ReadingDefinition(
            id = "mirror",
            displayName = "El Espejo",
            positions = listOf("Autoimagen", "Creencias limitantes", "Identidad", "Sombra"),
            promptGuidance = "Tirada de autoconocimiento. Contrapone cómo se ve la persona (Autoimagen) con lo que la frena por dentro (Creencias limitantes), lo que realmente es (Identidad) y lo que aún no reconoce de sí misma (Sombra). El foco es interno: nada de lo que la limita viene de afuera.",
        ),
        ReadingDefinition(
            id = "celtic-cross",
            displayName = "Cruz Celta",
            positions = listOf("Situación actual", "Obstáculos", "Pasado", "Futuro", "Entorno", "Resultado"),
            promptGuidance = "La tirada más completa. Traza un panorama integral: la situación actual y sus obstáculos, cómo el pasado influye y hacia dónde apunta el futuro, el entorno que la rodea y el resultado más probable. Interpreta carta a carta pero teje una narrativa amplia y cohesionada.",
        ),
        ReadingDefinition(
            id = "relationship",
            displayName = "Tirada de la Relación",
            positions = listOf("Dinámica de pareja", "Sentimientos", "Conflictos", "Futuro"),
            promptGuidance = "Tirada de vínculo entre dos personas. Explora la dinámica que comparten, los sentimientos en juego, los conflictos o tensiones presentes y hacia dónde puede evolucionar la relación. Mantén el equilibrio: no tomes partido por una de las partes.",
        ),
        ReadingDefinition(
            id = "tree-of-life",
            displayName = "El Árbol de la Vida",
            positions = listOf("Propósito", "Energía vital", "Equilibrio", "Aprendizaje"),
            promptGuidance = "Mapa espiritual de quién es la persona. Conecta su propósito con su energía vital, el equilibrio que busca y el aprendizaje que está atravesando. Tono elevado y reflexivo, orientado al sentido y al crecimiento del alma.",
        ),
        ReadingDefinition(
            id = "yes-no",
            displayName = "Tirada del Sí o No",
            positions = listOf("La respuesta"),
            promptGuidance = "Tirada de respuesta binaria. Ofrece una orientación clara hacia el sí o el no según la orientación y energía de la carta, y explica brevemente el porqué. Sé directo y sin rodeos, recordando que es una guía reflexiva y no una certeza absoluta.",
        ),
        ReadingDefinition(
            id = "horseshoe",
            displayName = "La Herradura",
            positions = listOf("Influencias externas", "Obstáculos", "Resultado probable"),
            promptGuidance = "Tirada de lo que se aproxima. Explica con claridad las influencias externas en juego, los obstáculos a sortear y el resultado más probable si la situación sigue su curso. Foco práctico y orientado a lo que viene.",
        ),
        ReadingDefinition(
            id = "soul-reading",
            displayName = "Tirada del Alma",
            positions = listOf("Misión de vida", "Aprendizajes", "Bloqueos energéticos"),
            promptGuidance = "Tirada de conexión con la misión de vida. Une el propósito profundo de la persona con los aprendizajes de su camino y los bloqueos energéticos que la frenan. Tono espiritual, cálido y liberador.",
        ),
        ReadingDefinition(
            id = "the-path",
            displayName = "El Camino",
            positions = listOf("Opciones", "Decisiones", "Consecuencias", "Dirección"),
            promptGuidance = "Tirada de decisión. La persona tiene opciones frente a sí. Ilumina las alternativas disponibles, las decisiones que implican, sus consecuencias y la dirección que más la acerca a lo que busca. Ayuda a elegir con claridad sin imponer la elección.",
        ),
        ReadingDefinition(
            id = "shadow",
            displayName = "Tirada de la Sombra",
            positions = listOf("Miedos", "Traumas", "Patrones inconscientes"),
            promptGuidance = "Trabajo de sombra. Explora con cuidado y sin juicio lo que la persona evita mirar: sus miedos, sus heridas o traumas y los patrones inconscientes que repite. Aquello que no quiere ver es justo lo que más la está afectando; nómbralo con delicadeza y en clave de sanación.",
        ),
        ReadingDefinition(
            id = "the-star",
            displayName = "La Estrella",
            positions = listOf("Esperanza", "Sanación", "Claridad emocional"),
            promptGuidance = "Tirada de calma después del caos. Ofrece esperanza, un camino de sanación y claridad emocional. Tono luminoso, sereno y reconfortante, orientado a devolver paz y dirección.",
        ),
        ReadingDefinition(
            id = "three-wishes",
            displayName = "Tirada de 3 deseos",
            positions = listOf("Primer deseo", "Segundo deseo", "Tercer deseo"),
            promptGuidance = "Tirada de manifestación. Cada carta corresponde a uno de los tres deseos de la persona: para cada uno indica qué lo frena, qué tan cerca está de concretarse y qué acción lo acerca. Trata cada deseo por separado pero cierra con una visión de conjunto.",
        ),
        ReadingDefinition(
            id = "labyrinth",
            displayName = "El Laberinto",
            positions = listOf("Caminos posibles", "Errores", "Claridad"),
            promptGuidance = "Tirada para la confusión. Cuando todo se siente enredado, traza los caminos posibles, señala los errores o desvíos a evitar y ofrece la claridad que permite encontrar la salida. Ordena lo caótico paso a paso.",
        ),
        ReadingDefinition(
            id = "karma",
            displayName = "Tirada del Karma",
            positions = listOf("Deudas kármicas", "Aprendizajes", "Conexiones pasadas"),
            promptGuidance = "Tirada kármica. Revela las deudas o patrones que la persona arrastra, el aprendizaje que vienen a dejarle y las conexiones pasadas que aún resuenan. Tono espiritual, con la idea de que ciertos vínculos vienen de más lejos de lo que se cree.",
        ),
        ReadingDefinition(
            id = "portal",
            displayName = "El Portal",
            positions = listOf("Nuevas etapas", "Cierres", "Oportunidades"),
            promptGuidance = "Tirada de umbral. La persona está cruzando a una etapa nueva. Muestra qué comienza, qué se cierra y qué oportunidades se abren del otro lado. Tono de transición y expansión.",
        ),
        ReadingDefinition(
            id = "money",
            displayName = "Tirada del Dinero",
            positions = listOf("Ingresos", "Bloqueos", "Inversiones", "Estabilidad"),
            promptGuidance = "Tirada del flujo económico. Lee la relación de la persona con el dinero a través de sus ingresos, sus bloqueos, sus inversiones y su estabilidad. Enfoca la energía y los patrones en torno al dinero; no des consejo financiero concreto ni prometas resultados.",
        ),
        ReadingDefinition(
            id = "universe-message",
            displayName = "El Mensaje del Universo",
            positions = listOf("Señales", "Consejo", "Energía predominante"),
            promptGuidance = "Tirada de escucha, sin pregunta puntual. Transmite las señales que el universo le está mostrando a la persona, un consejo y la energía predominante del momento. Tono de mensaje recibido, cálido y revelador.",
        ),
        ReadingDefinition(
            id = "pyramid",
            displayName = "La Pirámide",
            positions = listOf("Base", "Desarrollo", "Obstáculos", "Resultado"),
            promptGuidance = "Tirada de construcción de una meta. Explica la base que sostiene el objetivo, cómo se desarrolla, los obstáculos que enfrenta y el resultado posible. Da claridad estructurada, de lo cimiento hacia el resultado.",
        ),
        ReadingDefinition(
            id = "self-love",
            displayName = "Tirada del Amor Propio",
            positions = listOf("Autovaloración", "Heridas", "Crecimiento"),
            promptGuidance = "Tirada de la relación con uno mismo. Explora la autovaloración de la persona, las heridas que carga y el crecimiento posible. Tono tierno y compasivo: la relación más importante es la que tiene consigo misma.",
        ),
        ReadingDefinition(
            id = "judgment",
            displayName = "El Juicio",
            positions = listOf("Cambios drásticos", "Renacimiento", "Consecuencias"),
            promptGuidance = "Tirada de un momento decisivo. Señala el cambio drástico que se avecina o se necesita, el renacimiento que trae y sus consecuencias. Tono de punto de inflexión: hay momentos que lo cambian todo.",
        ),
        ReadingDefinition(
            id = "the-wheel",
            displayName = "La Rueda",
            positions = listOf("Cambios", "Ciclos", "Destino", "Timing"),
            promptGuidance = "Tirada de ciclos. La vida se mueve en etapas: muestra los cambios en curso, el ciclo en el que está la persona, la tendencia del destino y el timing de lo que viene. Ayuda a ubicarse en el momento del ciclo y a anticipar la siguiente etapa.",
        ),
        ReadingDefinition(
            id = "conflict",
            displayName = "Tirada del Conflicto",
            positions = listOf("Tensiones", "Causas", "Resolución"),
            promptGuidance = "Tirada para desactivar un problema. Identifica las tensiones visibles, las causas que hay detrás y el camino hacia la resolución. Nada es casual: ayuda a comprender la raíz antes de resolver.",
        ),
        ReadingDefinition(
            id = "inner-oracle",
            displayName = "El Oráculo Interno",
            positions = listOf("Voz interna", "Claridad mental", "Decisiones"),
            promptGuidance = "Tirada de intuición. La respuesta ya está dentro de la persona. Ayúdala a escuchar su voz interna, a alcanzar claridad mental y a tomar sus decisiones desde ahí. Tono de acompañamiento a la propia sabiduría, no de dictado externo.",
        ),
        ReadingDefinition(
            id = "duality",
            displayName = "La Dualidad",
            positions = listOf("Camino A", "Camino B", "Ventajas y riesgos"),
            promptGuidance = "Tirada de dos caminos. Contrasta la opción A con la opción B y expón con equilibrio sus ventajas y riesgos, para que la persona vea con claridad cuál resuena más con ella. Presenta ambos caminos con justicia; la elección es suya.",
        ),
        ReadingDefinition(
            id = "signal",
            displayName = "La Señal",
            positions = listOf("Energía presente", "Influencia activa", "Dirección próxima"),
            promptGuidance = "Lectura general y abierta: no asumas un tema específico (amor, trabajo, autoconocimiento, etc.) — la interpretación debe poder resonar en distintas áreas de la vida de quien consulta. La pregunta es opcional: si la persona la hizo, intégrala de forma natural sin limitar la lectura solo a eso; si no la hizo, lee igualmente su momento actual de forma significativa. Que la apertura conecte con su momento presente, que la síntesis se sienta personal y relevante mostrando una pequeña tensión o insight que intrigue y abra una puerta a una reflexión más profunda (sin cerrarla del todo), y que la guía cierre con una invitación suave — sin vender de forma agresiva — a explorar otras tiradas si quiere profundizar más.",
            questionRequired = false,
        ),
    )

    private val byId: Map<String, ReadingDefinition> = definitions.associateBy { it.id }

    /** Returns the reading definition for [id], or null if the id is unknown. */
    fun get(id: String): ReadingDefinition? = byId[id]
}

/**
 * The free-promo credit token for a given reading type, mirrored from
 * getFreeReadingFlag in the frontend's lib/pricing.ts — the two must stay in
 * sync, since this is the exact string stored in a user's tarot.readings array.
 *
 * "the-path" -> "FREE_THE_PATH", "key" -> "FREE_KEY"
 */
fun freeReadingFlag(readingType: String): String =
    "FREE_${readingType.uppercase().replace("-", "_")}"
