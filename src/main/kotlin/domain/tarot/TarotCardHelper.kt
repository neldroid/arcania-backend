package domain.tarot

object TarotDeck {
    val cards = listOf(
        // ---------------- MAJOR ARCANA ----------------
        TarotCard(0, "El Loco", Arcana.MAJOR, null, 0,
            listOf("beginnings","freedom","leap of faith","curiosity","spontaneity"),
            listOf("recklessness","naivety","poor planning","hesitation","risk")
        ),
        TarotCard(1, "El Mago", Arcana.MAJOR, null, 1,
            listOf("manifestation","power","skill","action","resourcefulness"),
            listOf("manipulation","deception","misuse","trickery","illusion")
        ),
        TarotCard(2, "La Sacerdotisa", Arcana.MAJOR, null, 2,
            listOf("intuition","mystery","inner voice","subconscious","wisdom"),
            listOf("secrets","confusion","blocked intuition","disconnection","doubt")
        ),
        TarotCard(3, "La Emperatriz", Arcana.MAJOR, null, 3,
            listOf("nurturing","abundance","fertility","creativity","care"),
            listOf("dependence","smothering","stagnation","neglect","insecurity")
        ),
        TarotCard(4, "El Emperador", Arcana.MAJOR, null, 4,
            listOf("authority","structure","control","stability","leadership"),
            listOf("rigidity","domination","control issues","inflexibility","tyranny")
        ),
        TarotCard(5, "El Hierofante", Arcana.MAJOR, null, 5,
            listOf("tradition","guidance","belief","conformity","teaching"),
            listOf("rebellion","nonconformity","restriction","dogma","rigidity")
        ),
        TarotCard(6, "Los Enamorados", Arcana.MAJOR, null, 6,
            listOf("relationships","alignment","union","values","choice"),
            listOf("imbalance","conflict","misalignment","temptation","indecision")
        ),
        TarotCard(7, "El Carro", Arcana.MAJOR, null, 7,
            listOf("direction","willpower","victory","determination","control"),
            listOf("lack of direction","aggression","obstacles","defeat","chaos")
        ),
        TarotCard(8, "La Fuerza", Arcana.MAJOR, null, 8,
            listOf("courage","patience","compassion","resilience","control"),
            listOf("weakness","self-doubt","insecurity","fear","imbalance")
        ),
        TarotCard(9, "El Ermitaño", Arcana.MAJOR, null, 9,
            listOf("introspection","solitude","wisdom","guidance","reflection"),
            listOf("isolation","loneliness","withdrawal","avoidance","disconnection")
        ),
        TarotCard(10, "La Rueda de la Fortuna", Arcana.MAJOR, null, 10,
            listOf("change","cycles","fate","luck","turning point"),
            listOf("bad luck","resistance","stagnation","delay","misfortune")
        ),
        TarotCard(11, "La Justicia", Arcana.MAJOR, null, 11,
            listOf("fairness","truth","balance","accountability","clarity"),
            listOf("injustice","bias","dishonesty","imbalance","avoidance")
        ),
        TarotCard(12, "El Colgado", Arcana.MAJOR, null, 12,
            listOf("pause","perspective","surrender","release","insight"),
            listOf("delay","resistance","stagnation","indecision","sacrifice")
        ),
        TarotCard(13, "La Muerte", Arcana.MAJOR, null, 13,
            listOf("transformation","endings","rebirth","transition","change"),
            listOf("resistance","fear of change","stagnation","clinging","delay")
        ),
        TarotCard(14, "La Templanza", Arcana.MAJOR, null, 14,
            listOf("balance","harmony","moderation","patience","flow"),
            listOf("imbalance","excess","conflict","impatience","extremes")
        ),
        TarotCard(15, "El Diablo", Arcana.MAJOR, null, 15,
            listOf("attachment","temptation","materialism","bondage","desire"),
            listOf("freedom","release","detachment","awareness","liberation")
        ),
        TarotCard(16, "La Torre", Arcana.MAJOR, null, 16,
            listOf("disruption","awakening","chaos","revelation","breakdown"),
            listOf("avoidance","fear of change","delayed collapse","denial","tension")
        ),
        TarotCard(17, "La Estrella", Arcana.MAJOR, null, 17,
            listOf("hope","healing","renewal","inspiration","faith"),
            listOf("despair","lack of faith","disconnection","doubt","discouragement")
        ),
        TarotCard(18, "La Luna", Arcana.MAJOR, null, 18,
            listOf("illusion","fear","uncertainty","intuition","subconscious"),
            listOf("clarity","truth","confusion","anxiety","misinterpretation")
        ),
        TarotCard(19, "El Sol", Arcana.MAJOR, null, 19,
            listOf("success","joy","clarity","positivity","vitality"),
            listOf("pessimism","delay","lack of clarity","sadness","ego")
        ),
        TarotCard(20, "El Juicio", Arcana.MAJOR, null, 20,
            listOf("reflection","awakening","evaluation","rebirth","calling"),
            listOf("self-doubt","avoidance","denial","stagnation","regret")
        ),
        TarotCard(21, "El Mundo", Arcana.MAJOR, null, 21,
            listOf("completion","fulfillment","achievement","integration","closure"),
            listOf("lack of closure","delay","incompletion","stagnation","frustration")
        ),

        // ---------------- CUPS ----------------
        TarotCard(22, "As de Copas", Arcana.MINOR, Suit.CUPS, 1,
            listOf("love","new feelings","compassion","connection","openness"),
            listOf("emptiness","blocked emotions","coldness","rejection","disconnection")
        ),
        TarotCard(23, "Dos de Copas", Arcana.MINOR, Suit.CUPS, 2,
            listOf("partnership","union","harmony","attraction","connection"),
            listOf("imbalance","separation","tension","miscommunication","distance")
        ),
        TarotCard(24, "Tres de Copas", Arcana.MINOR, Suit.CUPS, 3,
            listOf("celebration","friendship","joy","community","support"),
            listOf("overindulgence","gossip","isolated","excess","superficiality")
        ),
        TarotCard(25, "Cuatro de Copas", Arcana.MINOR, Suit.CUPS, 4,
            listOf("contemplation","apathy","reevaluation","boredom","withdrawal"),
            listOf("awareness","acceptance","interest","clarity","engagement")
        ),
        TarotCard(26, "Cinco de Copas", Arcana.MINOR, Suit.CUPS, 5,
            listOf("loss","grief","regret","disappointment","sadness"),
            listOf("acceptance","healing","moving on","forgiveness","recovery")
        ),
        TarotCard(27, "Seis de Copas", Arcana.MINOR, Suit.CUPS, 6,
            listOf("nostalgia","memories","innocence","past","kindness"),
            listOf("stuck in past","immaturity","unrealistic","regression","avoidance")
        ),
        TarotCard(28, "Siete de Copas", Arcana.MINOR, Suit.CUPS, 7,
            listOf("choices","illusion","fantasy","options","confusion"),
            listOf("clarity","focus","decision","reality","awareness")
        ),
        TarotCard(29, "Ocho de Copas", Arcana.MINOR, Suit.CUPS, 8,
            listOf("walking away","transition","leaving","search","growth"),
            listOf("avoidance","fear of change","stagnation","indecision","return")
        ),
        TarotCard(30, "Nueve de Copas", Arcana.MINOR, Suit.CUPS, 9,
            listOf("satisfaction","wish fulfilled","pleasure","contentment","joy"),
            listOf("dissatisfaction","greed","overindulgence","emptiness","lack")
        ),
        TarotCard(31, "Diez de Copas", Arcana.MINOR, Suit.CUPS, 10,
            listOf("happiness","harmony","fulfillment","family","alignment"),
            listOf("conflict","misalignment","tension","broken harmony","instability")
        ),
        TarotCard(32, "Sota de Copas", Arcana.MINOR, Suit.CUPS, 11,
            listOf("creativity","curiosity","sensitivity","emotion","surprise"),
            listOf("immaturity","emotional instability","insecurity","doubt","naivety")
        ),
        TarotCard(33, "Caballero de Copas", Arcana.MINOR, Suit.CUPS, 12,
            listOf("romance","charm","idealism","pursuit","emotion"),
            listOf("moodiness","unrealistic","disappointment","manipulation","inconsistency")
        ),
        TarotCard(34, "Reina de Copas", Arcana.MINOR, Suit.CUPS, 13,
            listOf("compassion","empathy","intuition","care","support"),
            listOf("emotional overwhelm","dependency","insecurity","neglect","moodiness")
        ),
        TarotCard(35, "Rey de Copas", Arcana.MINOR, Suit.CUPS, 14,
            listOf("control","balance","wisdom","diplomacy","emotional mastery"),
            listOf("coldness","repression","manipulation","instability","detachment")
        ),

        // ---------------- SWORDS ----------------
        TarotCard(36, "As de Espadas", Arcana.MINOR, Suit.SWORDS, 1,
            listOf("clarity","truth","breakthrough","insight","focus"),
            listOf("confusion","chaos","misinformation","doubt","misjudgment")
        ),
        TarotCard(37, "Dos de Espadas", Arcana.MINOR, Suit.SWORDS, 2,
            listOf("indecision","stalemate","avoidance","denial","tension"),
            listOf("clarity","decision","release","truth","resolution")
        ),
        TarotCard(38, "Tres de Espadas", Arcana.MINOR, Suit.SWORDS, 3,
            listOf("heartbreak","sorrow","pain","grief","separation"),
            listOf("healing","forgiveness","recovery","release","reconciliation")
        ),
        TarotCard(39, "Cuatro de Espadas", Arcana.MINOR, Suit.SWORDS, 4,
            listOf("rest","recovery","pause","healing","reflection"),
            listOf("burnout","stress","exhaustion","unrest","anxiety")
        ),
        TarotCard(40, "Cinco de Espadas", Arcana.MINOR, Suit.SWORDS, 5,
            listOf("conflict","tension","defeat","ego","argument"),
            listOf("resolution","compromise","peace","reconciliation","release")
        ),
        TarotCard(41, "Seis de Espadas", Arcana.MINOR, Suit.SWORDS, 6,
            listOf("transition","moving on","journey","change","recovery"),
            listOf("resistance","baggage","delay","difficulty","stuck")
        ),
        TarotCard(42, "Siete de Espadas", Arcana.MINOR, Suit.SWORDS, 7,
            listOf("deception","strategy","secrecy","avoidance","trickery"),
            listOf("honesty","truth","exposure","confession","accountability")
        ),
        TarotCard(43, "Ocho de Espadas", Arcana.MINOR, Suit.SWORDS, 8,
            listOf("restriction","fear","limitation","trapped","anxiety"),
            listOf("freedom","release","empowerment","clarity","control")
        ),
        TarotCard(44, "Nueve de Espadas", Arcana.MINOR, Suit.SWORDS, 9,
            listOf("anxiety","worry","fear","stress","guilt"),
            listOf("relief","recovery","hope","healing","calm")
        ),
        TarotCard(45, "Diez de Espadas", Arcana.MINOR, Suit.SWORDS, 10,
            listOf("endings","betrayal","collapse","pain","finality"),
            listOf("recovery","resilience","survival","rebuilding","renewal")
        ),
        TarotCard(46, "Sota de Espadas", Arcana.MINOR, Suit.SWORDS, 11,
            listOf("curiosity","ideas","communication","alertness","learning"),
            listOf("gossip","impulsiveness","immaturity","haste","miscommunication")
        ),
        TarotCard(47, "Caballero de Espadas", Arcana.MINOR, Suit.SWORDS, 12,
            listOf("action","ambition","speed","determination","drive"),
            listOf("recklessness","aggression","impatience","burnout","chaos")
        ),
        TarotCard(48, "Reina de Espadas", Arcana.MINOR, Suit.SWORDS, 13,
            listOf("independence","clarity","perception","honesty","truth"),
            listOf("coldness","bitterness","isolation","harshness","detachment")
        ),
        TarotCard(49, "Rey de Espadas", Arcana.MINOR, Suit.SWORDS, 14,
            listOf("authority","logic","truth","intellect","discipline"),
            listOf("manipulation","abuse","coldness","rigidity","control")
        ),

        // ---------------- WANDS ----------------
        TarotCard(50, "As de Bastos", Arcana.MINOR, Suit.WANDS, 1,
            listOf("inspiration","creation","energy","new idea","passion"),
            listOf("delay","lack of motivation","blockage","doubt","hesitation")
        ),
        TarotCard(51, "Dos de Bastos", Arcana.MINOR, Suit.WANDS, 2,
            listOf("planning","decisions","direction","progress","vision"),
            listOf("fear","indecision","lack of planning","stagnation","hesitation")
        ),
        TarotCard(52, "Tres de Bastos", Arcana.MINOR, Suit.WANDS, 3,
            listOf("expansion","growth","progress","foresight","opportunity"),
            listOf("delays","obstacles","lack of progress","frustration","limits")
        ),
        TarotCard(53, "Cuatro de Bastos", Arcana.MINOR, Suit.WANDS, 4,
            listOf("celebration","stability","home","harmony","success"),
            listOf("instability","conflict","tension","delay","imbalance")
        ),
        TarotCard(54, "Cinco de Bastos", Arcana.MINOR, Suit.WANDS, 5,
            listOf("competition","conflict","tension","struggle","chaos"),
            listOf("resolution","cooperation","harmony","agreement","peace")
        ),
        TarotCard(55, "Seis de Bastos", Arcana.MINOR, Suit.WANDS, 6,
            listOf("victory","success","recognition","achievement","pride"),
            listOf("failure","lack of recognition","ego","delay","insecurity")
        ),
        TarotCard(56, "Siete de Bastos", Arcana.MINOR, Suit.WANDS, 7,
            listOf("defense","challenge","perseverance","courage","resistance"),
            listOf("overwhelm","giving up","exhaustion","defeat","doubt")
        ),
        TarotCard(57, "Ocho de Bastos", Arcana.MINOR, Suit.WANDS, 8,
            listOf("speed","movement","progress","action","momentum"),
            listOf("delay","slowdown","obstacles","misalignment","frustration")
        ),
        TarotCard(58, "Nueve de Bastos", Arcana.MINOR, Suit.WANDS, 9,
            listOf("resilience","persistence","defense","endurance","strength"),
            listOf("exhaustion","burnout","giving up","fatigue","weakness")
        ),
        TarotCard(59, "Diez de Bastos", Arcana.MINOR, Suit.WANDS, 10,
            listOf("burden","responsibility","stress","overload","pressure"),
            listOf("release","delegation","relief","burnout","collapse")
        ),
        TarotCard(60, "Sota de Bastos", Arcana.MINOR, Suit.WANDS, 11,
            listOf("exploration","excitement","curiosity","discovery","energy"),
            listOf("lack of direction","immaturity","delay","frustration","distraction")
        ),
        TarotCard(61, "Caballero de Bastos", Arcana.MINOR, Suit.WANDS, 12,
            listOf("passion","action","adventure","impulsiveness","drive"),
            listOf("recklessness","impatience","inconsistency","burnout","chaos")
        ),
        TarotCard(62, "Reina de Bastos", Arcana.MINOR, Suit.WANDS, 13,
            listOf("confidence","independence","warmth","charisma","courage"),
            listOf("insecurity","jealousy","selfishness","doubt","control")
        ),
        TarotCard(63, "Rey de Bastos", Arcana.MINOR, Suit.WANDS, 14,
            listOf("leadership","vision","authority","innovation","power"),
            listOf("arrogance","impulsiveness","dominance","rigidity","control")
        ),

        // ---------------- PENTACLES ----------------
        TarotCard(64, "As de Oros", Arcana.MINOR, Suit.PENTACLES, 1,
            listOf("opportunity","prosperity","new venture","stability","growth"),
            listOf("missed opportunity","delay","loss","instability","lack")
        ),
        TarotCard(65, "Dos de Oros", Arcana.MINOR, Suit.PENTACLES, 2,
            listOf("balance","adaptability","juggling","flexibility","change"),
            listOf("imbalance","overwhelm","disorganization","stress","instability")
        ),
        TarotCard(66, "Tres de Oros", Arcana.MINOR, Suit.PENTACLES, 3,
            listOf("teamwork","collaboration","skill","growth","learning"),
            listOf("lack of teamwork","conflict","misalignment","poor quality","ego")
        ),
        TarotCard(67, "Cuatro de Oros", Arcana.MINOR, Suit.PENTACLES, 4,
            listOf("security","control","saving","stability","protection"),
            listOf("greed","control issues","fear","insecurity","release")
        ),
        TarotCard(68, "Cinco de Oros", Arcana.MINOR, Suit.PENTACLES, 5,
            listOf("hardship","loss","struggle","poverty","isolation"),
            listOf("recovery","support","improvement","hope","rebuilding")
        ),
        TarotCard(69, "Seis de Oros", Arcana.MINOR, Suit.PENTACLES, 6,
            listOf("generosity","giving","balance","charity","fairness"),
            listOf("inequality","strings attached","debt","imbalance","exploitation")
        ),
        TarotCard(70, "Siete de Oros", Arcana.MINOR, Suit.PENTACLES, 7,
            listOf("patience","investment","growth","evaluation","waiting"),
            listOf("impatience","poor investment","lack of growth","frustration","delay")
        ),
        TarotCard(71, "Ocho de Oros", Arcana.MINOR, Suit.PENTACLES, 8,
            listOf("mastery","work","dedication","skill","focus"),
            listOf("lack of focus","boredom","burnout","low quality","distraction")
        ),
        TarotCard(72, "Nueve de Oros", Arcana.MINOR, Suit.PENTACLES, 9,
            listOf("independence","luxury","self-sufficiency","success","comfort"),
            listOf("dependence","insecurity","loss","instability","overindulgence")
        ),
        TarotCard(73, "Diez de Oros", Arcana.MINOR, Suit.PENTACLES, 10,
            listOf("wealth","legacy","stability","family","success"),
            listOf("instability","loss","conflict","breakdown","insecurity")
        ),
        TarotCard(74, "Sota de Oros", Arcana.MINOR, Suit.PENTACLES, 11,
            listOf("learning","opportunity","growth","curiosity","ambition"),
            listOf("lack of progress","distraction","immaturity","missed chance","delay")
        ),
        TarotCard(75, "Caballero de Oros", Arcana.MINOR, Suit.PENTACLES, 12,
            listOf("discipline","reliability","consistency","effort","patience"),
            listOf("stagnation","laziness","rigidity","boredom","resistance")
        ),
        TarotCard(76, "Reina de Oros", Arcana.MINOR, Suit.PENTACLES, 13,
            listOf("nurturing","practicality","stability","care","support"),
            listOf("neglect","imbalance","dependency","insecurity","overwhelm")
        ),
        TarotCard(77, "Rey de Oros", Arcana.MINOR, Suit.PENTACLES, 14,
            listOf("success","abundance","leadership","security","wealth"),
            listOf("greed","materialism","control","stubbornness","excess")
        )

    )
}


object TarotCardHelper {

    /**
     * Draws one card per position, in order. The number of cards dealt is the
     * size of [positions], which comes from the reading's [ReadingDefinition]
     * — the deck never trusts a client-supplied count.
     */
    fun getCards(positions: List<String>): List<TarotCard> {
        require(positions.isNotEmpty()) { "positions must not be empty" }
        require(positions.size <= TarotDeck.cards.size) { "Not enough cards in deck" }

        return TarotDeck.cards
            .shuffled()
            .take(positions.size)
            .mapIndexed { index, card ->
                card.copy(
                    isReversed = kotlin.random.Random.nextBoolean(),
                    position = positions[index]
                )
            }
    }

}
