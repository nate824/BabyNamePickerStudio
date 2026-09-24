package com.example.data.seed

import com.example.data.model.BabyName
import com.example.data.model.Gender

object SeedGirlNames {
    val list: List<BabyName> = listOf(
        // Top Trends & Modern Classics
        g("olivia", "Olivia", "Latin", "Olive tree; peace, elegance, and beauty", "oh-LIV-ee-uh", 1, "Classic", "Melodic", "Royal"),
        g("emma", "Emma", "Germanic", "Universal, whole, and complete warmth", "EM-uh", 2, "Classic", "Timeless", "Short & Sweet"),
        g("charlotte", "Charlotte", "French", "Free spirit, strong, petite grandeur", "SHAR-lut", 3, "Royal", "Vintage", "Elegant"),
        g("amelia", "Amelia", "Germanic", "Industrious, hardworking striving for dreams", "uh-MEE-lee-uh", 4, "Vintage", "Melodic", "Classic"),
        g("sophia", "Sophia", "Greek", "Supreme divine wisdom and insight", "soh-FEE-uh", 5, "Wisdom", "Classic", "Melodic"),
        g("mia", "Mia", "Italian / Scandinavian", "Beloved mine, star of the sea", "MEE-uh", 6, "Short & Sweet", "Warm", "International"),
        g("isabella", "Isabella", "Hebrew / Italian", "Devoted promise of divine grace", "iz-uh-BEL-uh", 7, "Royal", "Romantic", "Classic"),
        g("ava", "Ava", "Latin / Persian", "Life, bird, breath of morning wind", "AY-vuh", 8, "Short & Sweet", "Classic", "Chic"),
        g("evelyn", "Evelyn", "English / French", "Wished for child, hazelnut clearing", "EV-lin", 9, "Vintage", "Gentle", "Classic"),
        g("luna", "Luna", "Latin", "The glowing silvery moon in starry sky", "LOO-nuh", 10, "Celestial", "Nature", "Short & Sweet"),
        g("harper", "Harper", "English", "Harp player, musical storyteller", "HAR-per", 11, "Musical", "Modern", "Creative"),
        g("camila", "Camila", "Latin / Spanish", "Ceremonial attendant, pure of heart", "kah-MEE-lah", 12, "Spanish", "Melodic", "Elegant"),
        g("sofia", "Sofia", "Greek / Italian", "Wisdom, clear discernment", "soh-FEE-uh", 13, "Classic", "Global", "Graceful"),
        g("scarlett", "Scarlett", "English", "Vibrant crimson, passion and joy", "SKAR-lit", 14, "Bold", "Vintage", "Color"),
        g("elizabeth", "Elizabeth", "Hebrew", "God is an oath, royal devotion", "eh-LIZ-uh-beth", 15, "Royal", "Biblical", "Timeless"),
        g("eleanor", "Eleanor", "Greek / French", "Shining sunbeam, bright torch", "EL-uh-nor", 16, "Vintage", "Royal", "Literary"),
        g("emily", "Emily", "Latin", "Industrious, eager admirer of beauty", "EM-uh-lee", 17, "Classic", "Literary", "Timeless"),
        g("chloe", "Chloe", "Greek", "Blooming green sprout of new life", "KLOH-ee", 18, "Mythological", "Nature", "Short & Sweet"),
        g("mila", "Mila", "Slavic", "Gracious, dear, deeply cherished", "MEE-luh", 19, "Short & Sweet", "Gentle", "International"),
        g("violet", "Violet", "Latin", "Sweet purple wildflower of faithfulness", "VY-lit", 20, "Flower", "Vintage", "Nature"),
        g("penelope", "Penelope", "Greek", "Weaver of dreams, loyal and clever", "puh-NEL-uh-pee", 21, "Literary", "Mythological", "Charming"),
        g("gianna", "Gianna", "Italian", "God is gracious, joyful gift", "jee-AH-nuh", 22, "Italian", "Spiritual", "Warm"),
        g("hazel", "Hazel", "English", "Hazel tree, symbol of protection and wisdom", "HAY-zul", 23, "Nature", "Vintage", "Warm"),
        g("nora", "Nora", "Irish / Greek", "Honor, radiant beacon of light", "NOR-uh", 24, "Short & Sweet", "Irish", "Vintage"),
        g("lily", "Lily", "Latin", "Pure white blossom of innocence and joy", "LIL-ee", 25, "Flower", "Short & Sweet", "Classic"),
        g("aurora", "Aurora", "Latin", "Dawn; goddess of glowing sunrise", "aw-ROH-ruh", 26, "Celestial", "Mythological", "Enchanting"),
        g("layla", "Layla", "Arabic", "Night, starry dark beauty, poetry", "LAY-luh", 27, "Melodic", "Poetic", "Arabic"),
        g("zoey", "Zoey", "Greek", "Life, abundant vitality and spirit", "ZOH-ee", 28, "Short & Sweet", "Spirited", "Modern"),
        g("isla", "Isla", "Scottish / Spanish", "Island; serene sanctuary in water", "EYE-luh", 29, "Nature", "Melodic", "Rising Fast"),
        g("elena", "Elena", "Greek / Spanish", "Bright shining torch of illumination", "eh-LAY-nuh", 30, "International", "Luminous", "Melodic"),

        // Celtic & Gaelic Heritage
        g("maeve", "Maeve", "Irish / Celtic", "She who intoxicates with joy; warrior queen", "MAYV", 35, "Celtic", "Royal", "One Syllable"),
        g("fiona", "Fiona", "Gaelic", "Fair, white, pure shining grace", "fee-OH-nuh", 82, "Celtic", "Classic", "Melodic"),
        g("saoirse", "Saoirse", "Irish", "Freedom, liberty, and triumphant spirit", "SEER-shuh", 125, "Celtic", "Empowered", "Poetic"),
        g("ciara", "Ciara", "Irish", "Dark-haired beauty, clear and bright", "KEER-uh", 145, "Celtic", "Short & Sweet", "Traditional"),
        g("brigid", "Brigid", "Irish", "Exalted one; Celtic goddess of poetry and fire", "BRIDGE-id", 195, "Celtic", "Mythological", "Spiritual"),
        g("aisling", "Aisling", "Irish", "Dream, poetic vision of hope", "ASH-ling", 220, "Celtic", "Poetic", "Dreamy"),
        g("morwenna", "Morwenna", "Cornish / Celtic", "Maiden of the sea wave", "mor-WEN-uh", 290, "Celtic", "Nature", "Unique"),
        g("rhiannon", "Rhiannon", "Welsh", "Great queen, divine celestial song", "ree-AN-un", 310, "Welsh", "Mythological", "Musical"),

        // Nordic & Scandinavian
        g("freya", "Freya", "Norse", "Noble lady; goddess of love and beauty", "FRAY-uh", 45, "Nordic", "Mythological", "Empowered"),
        g("astrid", "Astrid", "Scandinavian", "Divinely beautiful and beloved star", "AS-trid", 130, "Nordic", "Royal", "Vintage"),
        g("ingrid", "Ingrid", "Norse", "Hero's daughter; beautiful meadow", "ING-rid", 185, "Nordic", "Classic", "Strong"),
        g("sigrid", "Sigrid", "Norse", "Victory, glorious wisdom", "SEE-grid", 240, "Nordic", "Rare", "Empowered"),
        g("linnea", "Linnea", "Swedish", "Twinflower of the Scandinavian woodland", "lih-NAY-uh", 260, "Nordic", "Flower", "Delicate"),
        g("saga", "Saga", "Norse", "Seeing one; epic historical tale", "SAH-guh", 295, "Nordic", "Literary", "Short & Sweet"),
        g("thora", "Thora", "Norse", "Thunder goddess, brave and bright", "TOR-uh", 340, "Nordic", "Mythological", "Unique"),

        // Mediterranean, Latin & Spanish
        g("lucia", "Lucia", "Latin / Italian", "Light; born at dawn with clarity", "loo-CHEE-uh", 52, "Italian", "Radiant", "Classic"),
        g("valentina", "Valentina", "Latin / Spanish", "Strong, healthy, full of vitality", "vah-len-TEE-nuh", 58, "Romantic", "Melodic", "Strong"),
        g("clara", "Clara", "Latin", "Clear, luminous, and celebrated", "KLAHR-uh", 74, "Vintage", "Bright", "Timeless"),
        g("daphne", "Daphne", "Greek", "Laurel tree; victory and quiet grace", "DAF-nee", 110, "Mythological", "Nature", "Chic"),
        g("iris", "Iris", "Greek", "Rainbow; messenger bridging heaven and earth", "EYE-ris", 84, "Flower", "Mythological", "Vintage"),
        g("giulia", "Giulia", "Italian", "Youthful, lively, radiant heart", "JOO-lyuh", 160, "Italian", "Melodic", "Classic"),
        g("catalina", "Catalina", "Spanish", "Pure, serene island beauty", "kah-tah-LEE-nah", 175, "Spanish", "Melodic", "Warm"),
        g("marina", "Marina", "Latin", "From the sea, marine maiden", "mah-REE-nuh", 215, "Nature", "Ocean", "Melodic"),
        g("paloma", "Paloma", "Spanish", "Dove of tranquil peace", "pah-LOH-mah", 230, "Spanish", "Peaceful", "Gentle"),
        g("sienna", "Sienna", "Italian", "Earthy red-orange clay of Tuscany", "see-EN-uh", 140, "Italian", "Nature", "Warm"),
        g("inés", "Inés", "Spanish", "Pure, gentle and holy", "ee-NES", 280, "Spanish", "Short & Sweet", "Classic"),

        // French & Romance
        g("genevieve", "Genevieve", "French / Celtic", "Tribe woman, white wave, compassion", "ZHEN-uh-veev", 95, "French", "Elegant", "Vintage"),
        g("margot", "Margot", "French", "Precious pearl of the sea", "MAR-goh", 115, "French", "Chic", "Vintage"),
        g("eloise", "Eloise", "French", "Healthy, wide sun, radiant warrior", "EL-oh-eez", 105, "French", "Playful", "Literary"),
        g("camille", "Camille", "French", "Noble young attendant of virtues", "kah-MEEL", 170, "French", "Gentle", "Classic"),
        g("colette", "Colette", "French", "People of victory, stylish necklace", "koh-LET", 210, "French", "Chic", "Vintage"),
        g("celeste", "Celeste", "Latin / French", "Heavenly, celestial blue skies", "seh-LEST", 165, "Celestial", "Gentle", "Elegant"),
        g("adele", "Adele", "Germanic / French", "Noble, serene and luminous melody", "uh-DEL", 250, "Musical", "Royal", "Short & Sweet"),

        // Arabic & Middle Eastern
        g("amira", "Amira", "Arabic", "Princess, flourishing leader of prosperity", "ah-MEER-uh", 120, "Arabic", "Royal", "Melodic"),
        g("nour_g", "Nour", "Arabic", "Divine illumination and glowing light", "NOOR", 185, "Arabic", "Spiritual", "One Syllable"),
        g("yasmin", "Yasmin", "Persian / Arabic", "Jasmine flower with heavenly scent", "YAZ-min", 195, "Flower", "Nature", "Poetic"),
        g("farah", "Farah", "Arabic", "Pure celebration, joy and delight", "FAH-ruh", 225, "Arabic", "Joyful", "Short & Sweet"),
        g("soraya", "Soraya", "Persian", "Pleiades constellation, radiant gem", "soh-RY-uh", 260, "Celestial", "Persian", "Royal"),
        g("leila", "Leila", "Arabic", "Born in the enchanting night", "LAY-luh", 135, "Arabic", "Melodic", "Poetic"),
        g("mariam", "Mariam", "Arabic / Hebrew", "Star of the sea, beloved mother", "MAH-ree-um", 190, "Spiritual", "Biblical", "Timeless"),
        g("hana", "Hana", "Japanese / Arabic", "Blossoming flower, bliss and favor", "HAH-nah", 210, "Japanese", "Nature", "Short & Sweet"),

        // East Asian & Japanese
        g("yuna", "Yuna", "Japanese", "Gentle night, moonlight, loving kindness", "YOO-nuh", 180, "East Asian", "Short & Sweet", "Delicate"),
        g("mei", "Mei", "Japanese / Chinese", "Plum blossom, exquisite beauty", "MAY", 210, "Nature", "Short & Sweet", "Gentle"),
        g("aoi", "Aoi", "Japanese", "Hollyhock flower, blue sky harmony", "ah-OH-ee", 270, "Japanese", "Nature", "Unique"),
        g("sakura", "Sakura", "Japanese", "Cherry blossom of renewed spring", "sah-KOO-rah", 290, "Japanese", "Nature", "Poetic"),
        g("kaori", "Kaori", "Japanese", "Sweet fragrant aroma of springtime", "kah-OH-ree", 340, "Japanese", "Gentle", "Unique"),

        // South Asian / Sanskrit
        g("ananya", "Ananya", "Sanskrit", "Unique, matchless, without equal", "uh-NAHN-yuh", 220, "Sanskrit", "Poetic", "Unique"),
        g("priya", "Priya", "Sanskrit", "Deeply beloved, darling of the heart", "PREE-yuh", 240, "Sanskrit", "Warm", "Beloved"),
        g("diya", "Diya", "Sanskrit", "Small glowing clay lamp of light", "DEE-yuh", 280, "Sanskrit", "Luminous", "Short & Sweet"),
        g("aarya", "Aarya", "Sanskrit", "Noble goddess, benevolent prayer", "AHR-yuh", 310, "Sanskrit", "Spiritual", "Noble"),
        g("maya_g", "Maya", "Sanskrit / Greek", "Illusion, creative power of nature", "MY-uh", 62, "Sanskrit", "Melodic", "Global"),

        // African & Swahili Heritage
        g("amara", "Amara", "Igbo / Sanskrit", "Grace, immortal eternal love, kindness", "ah-MAH-ruh", 75, "Global", "Spiritual", "Graceful"),
        g("zuri", "Zuri", "Swahili", "Beautiful, radiant and full of grace", "ZOO-ree", 88, "African", "Short & Sweet", "Bright"),
        g("nia", "Nia", "Swahili", "Purpose, determination and vision", "NEE-uh", 155, "African", "Short & Sweet", "Empowered"),
        g("ayanna", "Ayanna", "Swahili", "Beautiful blooming flower", "eye-YAHN-uh", 240, "African", "Flower", "Melodic"),
        g("imani", "Imani", "Swahili", "Faith, belief, steadfast devotion", "ee-MAH-nee", 275, "African", "Spiritual", "Melodic"),
        g("zola", "Zola", "Zulu / Italian", "Quiet, tranquil, peaceful calmness", "ZOH-luh", 320, "Global", "Peaceful", "Short & Sweet"),

        // Vintage & Botanical Classics
        g("ivy", "Ivy", "English", "Climbing evergreen of fidelity", "EYE-vee", 42, "Nature", "Vintage", "Short & Sweet"),
        g("willow_g", "Willow", "English", "Slender grace, resilience in wind", "WIL-oh", 38, "Nature", "Modern", "Gentle"),
        g("ruby", "Ruby", "Latin", "Precious deep red gemstone of passion", "ROO-bee", 64, "Gemstone", "Vintage", "Joyful"),
        g("alice", "Alice", "Germanic / French", "Noble, exalted, curious adventurer", "AL-is", 72, "Classic", "Literary", "Vintage"),
        g("cora", "Cora", "Greek", "Maiden, heart, goddess of springtime", "KOR-uh", 80, "Mythological", "Short & Sweet", "Vintage"),
        g("beatrice", "Beatrice", "Latin", "She who brings happiness and blessings", "BEE-uh-tris", 125, "Vintage", "Joyful", "Literary"),
        g("ophelia", "Ophelia", "Greek", "Helper, savior, poetic muse", "oh-FEE-lee-uh", 145, "Literary", "Melodic", "Romantic"),
        g("flora", "Flora", "Latin", "Goddess of flowers and springtime", "FLOR-uh", 215, "Flower", "Nature", "Vintage"),
        g("pearl", "Pearl", "Latin", "Precious oceanic gem of pure light", "PERL", 245, "Gemstone", "Vintage", "One Syllable"),
        g("madeline", "Madeline", "French / Greek", "High tower, majestic presence", "MAD-uh-lin", 82, "French", "Classic", "Melodic"),
        g("delilah", "Delilah", "Hebrew", "Delicate, enticing, sweet breeze", "dih-LY-luh", 62, "Biblical", "Melodic", "Warm"),
        g("piper_g", "Piper", "English", "Player of the playful flute song", "PY-per", 78, "Musical", "Playful", "Modern"),
        g("lydia", "Lydia", "Greek", "Noble lady of Lydia, artistic merchant", "LID-ee-uh", 86, "Biblical", "Vintage", "Classic"),
        g("jade", "Jade", "Spanish", "Precious green gemstone of wisdom and healing", "JAYD", 92, "Gemstone", "One Syllable", "Nature"),
        g("rose", "Rose", "Latin", "Classic fragrant flower of boundless love", "ROHZ", 88, "Flower", "One Syllable", "Timeless"),
        g("autumn", "Autumn", "Latin", "Season of golden harvest and cozy warmth", "AW-tum", 66, "Nature", "Seasonal", "Warm"),
        g("serenity", "Serenity", "Latin", "Tranquil peaceful stillness and calm", "suh-REN-ih-tee", 72, "Virtue", "Melodic", "Spiritual"),
        g("summer", "Summer", "English", "Season of radiant sun, blossoming joy", "SUM-er", 112, "Nature", "Seasonal", "Bright"),
        g("daisy", "Daisy", "English", "Day's eye; wildflower opening to the dawn", "DAY-zee", 124, "Flower", "Joyful", "Vintage"),
        g("june", "June", "Latin", "Young, queen of gods; warm wedding month", "JOON", 140, "Seasonal", "Vintage", "One Syllable"),
        g("esther", "Esther", "Persian / Hebrew", "Star, brave courageous savior queen", "ES-ter", 136, "Biblical", "Vintage", "Heroic"),
        g("faye", "Faye", "French", "Fairy, enchanted faith and devotion", "FAY", 310, "French", "One Syllable", "Fairy Tale"),
        g("wrenley", "Wrenley", "English", "Meadow of the gentle singing birds", "REN-lee", 160, "Nature", "Modern", "Rising Fast"),
        g("poppy", "Poppy", "Latin", "Bright red field flower of joyous remembrance", "PAH-pee", 175, "Flower", "Playful", "British"),
        g("georgia", "Georgia", "Greek", "Earth worker, fertile agricultural beauty", "JOR-juh", 145, "Vintage", "Southern", "Classic"),
        g("sienna_g", "Sienna", "Italian", "Warm terracotta earth of Tuscany", "see-EN-uh", 138, "Italian", "Color", "Modern"),
        g("keira", "Keira", "Irish", "Little dark-haired maiden of bright fire", "KEER-uh", 210, "Celtic", "Short & Sweet", "Modern"),
        g("adeline", "Adeline", "French / German", "Noble, serene and luminous lady", "AD-uh-lyne", 102, "Vintage", "French", "Melodic"),
        g("bianca", "Bianca", "Italian", "White, shining, pure and fair", "bee-AHN-kuh", 240, "Italian", "Melodic", "Classic"),
        g("cleo_g", "Cleo", "Greek", "Glory, renown, celebrated brilliance", "KLEE-oh", 295, "Short & Sweet", "Mythological", "Vintage"),
        g("daphne_m", "Dalia", "Arabic / Hebrew", "Gentle flowering branch or star", "DAH-lee-uh", 315, "Flower", "Nature", "Short & Sweet"),
        g("evangeline", "Evangeline", "Greek", "Bringer of good joyful tidings", "ee-VAN-juh-leen", 185, "Spiritual", "Literary", "Romantic"),
        g("francesca", "Francesca", "Italian", "Free spirit, courageous and open", "fran-CHES-kuh", 215, "Italian", "Romantic", "Melodic"),
        g("guinevere", "Guinevere", "Welsh / Celtic", "White phantom, fair enchantress queen", "GWIN-uh-veer", 380, "Celtic", "Mythological", "Royal"),
        g("havana", "Havana", "Spanish", "Port of breezes, vibrant tropical heart", "huh-VAN-uh", 420, "Global", "Warm", "Modern"),
        g("isolde", "Isolde", "Celtic", "Ice ruler, legendary romantic devotion", "ee-ZOHL-duh", 460, "Celtic", "Mythological", "Poetic"),
        g("jocelyn", "Jocelyn", "Germanic / French", "Joyous one, cheerful companion", "JAS-lin", 280, "Classic", "Warm", "Melodic"),
        g("kassandra", "Kassandra", "Greek", "Shining upon humankind, visionary", "kuh-SAN-druh", 390, "Greek", "Mythological", "Strong"),
        g("louisa", "Louisa", "Germanic / French", "Renowned warrior of grace", "loo-EE-zuh", 410, "Vintage", "Literary", "Royal"),
        g("mabel", "Mabel", "Latin", "Lovable, worthy of sweetest affection", "MAY-bul", 290, "Vintage", "Warm", "Classic"),
        g("nadia", "Nadia", "Slavic / Arabic", "Hope, tender caller of the dawn", "NAH-dee-uh", 310, "International", "Spiritual", "Melodic"),
        g("odette", "Odette", "French / German", "Wealthy, swan queen of elegance", "oh-DET", 440, "French", "Fairy Tale", "Gentle"),
        g("priscilla", "Priscilla", "Latin", "Ancient, venerable, revered wisdom", "prih-SIL-uh", 460, "Biblical", "Vintage", "Classic"),
        g("ramona", "Ramona", "Spanish", "Wise protector, guardian of hearts", "rah-MOH-nuh", 480, "Spanish", "Vintage", "Literary"),
        g("sabrina", "Sabrina", "Celtic", "Legendary princess of river Severn", "suh-BREE-nuh", 380, "Celtic", "Mythological", "Melodic"),
        g("talia", "Talia", "Hebrew", "Gentle dew of heaven", "TAH-lee-uh", 320, "Biblical", "Nature", "Short & Sweet"),
        g("veronica", "Veronica", "Latin / Greek", "True image; she who brings victory", "vuh-RAHN-ih-kuh", 370, "Classic", "Heroic", "Melodic"),
        g("zelda", "Zelda", "Germanic / Yiddish", "Gray warrior or dark blessed happiness", "ZEL-duh", 490, "Vintage", "Heroic", "Playful")
    )

    private fun g(id: String, name: String, origin: String, meaning: String, pron: String, rank: Int, vararg tags: String): BabyName {
        return BabyName(
            id = "girl_$id",
            name = name,
            gender = Gender.GIRL,
            origin = origin,
            meaning = meaning,
            pronunciation = pron,
            popularityRank = rank,
            styleTags = tags.toList()
        )
    }
}
