package com.example.data.seed

import com.example.data.model.BabyName
import com.example.data.model.Gender

object SeedWorldNames {
    val list: List<BabyName> = listOf(
        // Scandinavian & Germanic
        n("w_soren_v", "Stian", Gender.BOY, "Scandinavian", "Swift wanderer of the northern fjords", "STEE-ahn", 420, "Nordic", "Nature"),
        n("w_freja", "Freja", Gender.GIRL, "Scandinavian", "Lady of light, love and summer dew", "FRAY-yah", 240, "Nordic", "Mythological"),
        n("w_torstein", "Torstein", Gender.BOY, "Norse", "Thor's sacred steadfast stone", "TOR-stayn", 480, "Nordic", "Strong"),
        n("w_solveig", "Solveig", Gender.GIRL, "Scandinavian", "Daughter of the sun; house protector", "SOOL-vay", 310, "Nordic", "Poetic"),
        n("w_espen", "Espen", Gender.BOY, "Scandinavian", "God bear, courageous voyager", "ES-pen", 390, "Nordic", "Nature"),
        n("w_ronja", "Ronja", Gender.GIRL, "Scandinavian", "Robber's daughter, free forest child", "ROHN-yah", 270, "Literary", "Nordic"),
        n("w_einar", "Einar", Gender.BOY, "Norse", "Lone warrior, bold champion", "EYE-nar", 460, "Nordic", "Heroic"),
        n("w_liv", "Liv", Gender.GIRL, "Scandinavian", "Life, living shelter and protection", "LIV", 160, "Nordic", "One Syllable"),
        n("w_arvid", "Arvid", Gender.BOY, "Swedish", "Eagle of the forest tree", "AR-vid", 490, "Nordic", "Nature"),
        n("w_tuva", "Tuva", Gender.GIRL, "Scandinavian", "Tuft of heather, wild grass", "TOO-vah", 350, "Nordic", "Nature"),

        // Celtic & Gaelic
        n("w_sorcha", "Sorcha", Gender.GIRL, "Irish / Gaelic", "Bright, radiant, shining clear light", "SUR-kuh", 380, "Celtic", "Luminous"),
        n("w_padraig", "Padraig", Gender.BOY, "Irish", "Noble born, honorable heart", "PAW-drig", 340, "Celtic", "Classic"),
        n("w_tadhg", "Tadhg", Gender.BOY, "Irish", "Poet, philosopher of ancient lore", "TYG", 260, "Celtic", "Literary"),
        n("w_orla", "Orla", Gender.GIRL, "Irish", "Golden princess of dawn", "OR-luh", 190, "Celtic", "Royal"),
        n("w_daire", "Daire", Gender.BOY, "Irish", "Fruitful, oak grove abundance", "DAH-ruh", 430, "Celtic", "Nature"),
        n("w_niamh", "Niamh", Gender.GIRL, "Irish", "Bright shining daughter of sea god", "NEEV", 210, "Celtic", "Mythological"),
        n("w_tiernan", "Tiernan", Gender.BOY, "Irish", "Little lord, sovereign of peace", "TEER-nun", 370, "Celtic", "Royal"),
        n("w_sinead", "Sinead", Gender.GIRL, "Irish", "God is gracious, musical voice", "shih-NAYD", 320, "Celtic", "Classic"),

        // Greek & Classical
        n("w_althea", "Alethea", Gender.GIRL, "Greek", "Truth, sincerity and clarity of spirit", "uh-LEE-thee-uh", 410, "Greek", "Virtue"),
        n("w_linus", "Linus", Gender.BOY, "Greek", "Flax-colored hair, gentle musician", "LY-nus", 490, "Greek", "Vintage"),
        n("w_clio", "Clio", Gender.GIRL, "Greek", "Proclaimer of glorious history and glory", "KLY-oh", 360, "Greek", "Mythological"),
        n("w_damon", "Damon", Gender.BOY, "Greek", "To tame, loyal and steadfast friend", "DAY-mun", 380, "Greek", "Classic"),
        n("w_phaedra", "Phaedra", Gender.GIRL, "Greek", "Bright, glowing, shining wonder", "FAY-druh", 480, "Greek", "Mythological"),
        n("w_leander", "Leander", Gender.BOY, "Greek", "Lion of a man, heroic swimmer", "lee-AN-der", 390, "Greek", "Romantic"),
        n("w_xanthe", "Xanthe", Gender.GIRL, "Greek", "Golden, yellow bloom of dawn", "ZAN-thee", 460, "Greek", "Rare"),
        n("w_theron", "Theron", Gender.BOY, "Greek", "Hunter, courageous protector", "THEER-on", 520, "Greek", "Strong"),

        // Latin & Romance
        n("w_cosimo", "Cosimo", Gender.BOY, "Italian", "Order, beauty, universal harmony", "KOH-zee-moh", 540, "Italian", "Historic"),
        n("w_alessia", "Alessia", Gender.GIRL, "Italian", "Defending warrior of kindness", "uh-LES-syuh", 260, "Italian", "Melodic"),
        n("w_matteo_it", "Mattea", Gender.GIRL, "Italian", "Gift of God, cherished joy", "mah-TAY-uh", 370, "Italian", "Warm"),
        n("w_valerio", "Valerio", Gender.BOY, "Italian", "Strong, healthy, valiant spirit", "vah-LEH-ree-oh", 480, "Italian", "Melodic"),
        n("w_lucilla", "Lucilla", Gender.GIRL, "Latin", "Little light, gentle spark of hope", "loo-CHIL-uh", 430, "Ancient Roman", "Gentle"),
        n("w_fausto", "Fausto", Gender.BOY, "Spanish", "Auspicious, blessed with good fortune", "FOW-stoh", 510, "Spanish", "Classic"),

        // East Asian & Polynesian
        n("w_asahi", "Asahi", Gender.UNISEX, "Japanese", "Morning sun rising over the hills", "ah-SAH-hee", 390, "Japanese", "Celestial"),
        n("w_kohana", "Kohana", Gender.GIRL, "Japanese", "Little delicate spring blossom", "koh-HAH-nah", 340, "Japanese", "Flower"),
        n("w_ryo", "Ryo", Gender.BOY, "Japanese", "Cool, refreshing, clear breeze", "RYOH", 410, "Japanese", "Short & Sweet"),
        n("w_moana", "Moana", Gender.GIRL, "Polynesian", "Vast expanse of deep blue ocean", "moh-AH-nah", 280, "Polynesian", "Ocean"),
        n("w_keanu", "Keanu", Gender.BOY, "Hawaiian", "Cool mountain breeze over cliffs", "kay-AH-noo", 210, "Hawaiian", "Nature"),
        n("w_leilani", "Leilani", Gender.GIRL, "Hawaiian", "Heavenly child of radiant blossoms", "lay-LAH-nee", 115, "Hawaiian", "Flower"),
        n("w_alani", "Alani", Gender.GIRL, "Hawaiian", "Orange blossom, gentle citrus bloom", "uh-LAH-nee", 195, "Hawaiian", "Nature"),
        n("w_koa", "Koa", Gender.BOY, "Hawaiian", "Brave, bold warrior; sturdy acacia tree", "KOH-uh", 145, "Hawaiian", "Nature"),

        // South Asian / Sanskrit
        n("w_advait", "Advait", Gender.BOY, "Sanskrit", "Unique, extraordinary non-dual spirit", "ud-VAYT", 320, "Sanskrit", "Philosophical"),
        n("w_ishani", "Ishani", Gender.GIRL, "Sanskrit", "Consort of Shiva, divine ruler", "ee-SHAH-nee", 360, "Sanskrit", "Spiritual"),
        n("w_kabir", "Kabir", Gender.BOY, "Sanskrit / Arabic", "Great poet, mystic of love and unity", "kah-BEER", 290, "Sanskrit", "Literary"),
        n("w_tara", "Tara", Gender.GIRL, "Sanskrit", "Star, celestial savior guiding path", "TAH-ruh", 220, "Sanskrit", "Celestial"),
        n("w_vihaan", "Vihaan", Gender.BOY, "Sanskrit", "Dawn, beginning of a new bright era", "vee-HAHN", 240, "Sanskrit", "Bright"),
        n("w_kavya", "Kavya", Gender.GIRL, "Sanskrit", "Poetry in motion, lyrical beauty", "KAHV-yuh", 310, "Sanskrit", "Artistic"),

        // Arabic & Middle Eastern
        n("w_layth", "Layth", Gender.BOY, "Arabic", "Brave lion cub of the desert", "LAYTH", 390, "Arabic", "Strong"),
        n("w_rim", "Reem", Gender.GIRL, "Arabic", "Graceful pure white gazelle", "REEM", 290, "Arabic", "Nature"),
        n("w_zaki", "Zaki", Gender.BOY, "Arabic", "Pure, intelligent, virtuous leader", "ZAH-kee", 430, "Arabic", "Wisdom"),
        n("w_salma", "Salma", Gender.GIRL, "Arabic", "Peaceful sanctuary, calm and whole", "SAHL-muh", 270, "Arabic", "Peaceful"),
        n("w_basil", "Basil", Gender.BOY, "Arabic / Greek", "Brave, royal kingly defender", "BAH-zil", 360, "Royal", "Classic"),
        n("w_dunia", "Dunia", Gender.GIRL, "Arabic", "The world, whole radiant earth", "DOO-nyuh", 410, "Arabic", "Poetic"),

        // African & Diaspora
        n("w_bayo", "Bayo", Gender.BOY, "Yoruba", "To find immense joy and celebration", "BAH-yoh", 380, "African", "Joyful"),
        n("w_chiamaka", "Chiamaka", Gender.GIRL, "Igbo", "God is splendidly beautiful", "chee-ah-MAH-kuh", 420, "African", "Spiritual"),
        n("w_dakarai", "Dakarai", Gender.BOY, "Shona", "Rejoice, happiness in community", "dah-kah-RYE", 470, "African", "Joyful"),
        n("w_zola_b", "Zikora", Gender.BOY, "Igbo", "Show the world the goodness of God", "zee-KOR-uh", 510, "African", "Spiritual"),
        n("w_subira", "Subira", Gender.GIRL, "Swahili", "Patience, steadfast gentle heart", "soo-BEE-ruh", 460, "African", "Virtue")
    )

    private fun n(id: String, name: String, gender: Gender, origin: String, meaning: String, pron: String, rank: Int, vararg tags: String): BabyName {
        return BabyName(
            id = id,
            name = name,
            gender = gender,
            origin = origin,
            meaning = meaning,
            pronunciation = pron,
            popularityRank = rank,
            styleTags = tags.toList()
        )
    }
}
