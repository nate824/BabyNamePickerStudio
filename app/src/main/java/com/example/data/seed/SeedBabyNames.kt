package com.example.data.seed

import com.example.data.model.BabyName
import com.example.data.model.Gender

object SeedBabyNames {

    private val extendedNames: List<BabyName> = listOf(
        // Additional Boys
        b("b_gabriel", "Gabriel", Gender.BOY, "Hebrew", "God is my strength, radiant archangel", "GAY-bree-ul", 36, "Biblical", "Gentle", "Classic"),
        b("b_anthony", "Anthony", Gender.BOY, "Latin", "Priceless, of inestimable worth", "AN-thuh-nee", 44, "Classic", "Timeless", "Strong"),
        b("b_isaac", "Isaac", Gender.BOY, "Hebrew", "He will laugh; joy and blessed laughter", "EYE-zik", 39, "Biblical", "Joyful", "Classic"),
        b("b_caleb", "Caleb", Gender.BOY, "Hebrew", "Devoted, faithful, brave of whole heart", "KAY-lub", 51, "Biblical", "Faithful", "Warm"),
        b("b_nathan", "Nathan", Gender.BOY, "Hebrew", "He gave, generous gift of heaven", "NAY-thun", 59, "Biblical", "Gentle", "Classic"),
        b("b_leo_m", "Leon", Gender.BOY, "Greek", "Lion, bold leader with golden heart", "LEE-on", 160, "Classic", "Strong", "Short & Sweet"),
        b("b_elias_s", "Eli", Gender.BOY, "Hebrew", "Ascended, high, uplifting spirit", "EE-lye", 63, "Biblical", "Short & Sweet", "Gentle"),
        b("b_nolan", "Nolan", Gender.BOY, "Irish", "Champion, famous and noble", "NOH-lun", 66, "Celtic", "Modern", "Friendly"),
        b("b_ian", "Ian", Gender.BOY, "Scottish", "The Lord is gracious, highland leader", "EE-un", 77, "Scottish", "Short & Sweet", "Classic"),
        b("b_adam", "Adam", Gender.BOY, "Hebrew", "Of the earth, red clay, first life", "AD-um", 96, "Biblical", "Classic", "Earthy"),
        b("b_wesley", "Wesley", Gender.BOY, "Old English", "Western meadow, open breeze", "WES-lee", 84, "Vintage", "Nature", "Gentle"),
        b("b_silas_m", "Silas", Gender.BOY, "Latin", "Forest, man of the green woodlands", "SY-lus", 91, "Nature", "Biblical", "Warm"),
        b("b_rowan_b", "Ronin", Gender.BOY, "Japanese / Celtic", "Masterless wanderer, honorable wave", "ROH-nin", 270, "Japanese", "Cool", "Modern"),
        b("b_darius", "Darius", Gender.BOY, "Persian", "Possessing goodness, wealthy protector", "duh-RY-us", 310, "Persian", "Royal", "Historic"),
        b("b_cyrus", "Cyrus", Gender.BOY, "Persian", "Sun, radiant throne of majesty", "SY-rus", 340, "Persian", "Celestial", "Vintage"),
        b("b_alden", "Alden", Gender.BOY, "English", "Old wise friend, steadfast protector", "AWL-dun", 380, "Vintage", "Gentle", "Literary"),
        b("b_conrad", "Conrad", Gender.BOY, "Germanic", "Brave counsel, wise advisor", "KAHN-rad", 420, "Vintage", "Strong", "Classic"),
        b("b_dorian", "Dorian", Gender.BOY, "Greek", "Of the sea, from the ocean gift", "DOR-ee-un", 440, "Greek", "Literary", "Melodic"),
        b("b_evander", "Evander", Gender.BOY, "Scottish / Greek", "Good man, bow warrior of legends", "ee-VAN-der", 460, "Scottish", "Heroic", "Unique"),
        b("b_alister", "Alistair", Gender.BOY, "Scottish", "Defender of humankind, noble", "AL-is-ter", 390, "Scottish", "Classic", "Chivalrous"),
        b("b_bastian", "Bastian", Gender.BOY, "Latin", "Revered, venerable dreamer", "BAS-tee-un", 410, "Literary", "Modern", "Unique"),
        b("b_casper", "Casper", Gender.BOY, "Persian / Dutch", "Treasurer, bringer of precious joy", "KAS-per", 450, "Vintage", "Friendly", "Charming"),
        b("b_fintan", "Fintan", Gender.BOY, "Irish", "White fire; legendary salmon of wisdom", "FIN-tun", 510, "Celtic", "Mythological", "Unique"),
        b("b_hamza", "Hamza", Gender.BOY, "Arabic", "Lion, steadfast and steadfastly strong", "HAHM-zuh", 530, "Arabic", "Strong", "Historical"),
        b("b_ignacio", "Ignacio", Gender.BOY, "Spanish", "Fiery, luminous flame of heart", "eeg-NAH-syoh", 550, "Spanish", "Warm", "Classic"),
        b("b_joaquin", "Joaquin", Gender.BOY, "Spanish / Hebrew", "Raised up by God, noble leader", "wah-KEEN", 230, "Spanish", "Melodic", "Classic"),
        b("b_maximilian", "Maximilian", Gender.BOY, "Latin", "The greatest, royal excellence", "mak-sih-MIL-yun", 380, "Royal", "Classic", "Historic"),
        b("b_orson", "Orson", Gender.BOY, "Latin", "Little bear, courageous guardian", "OR-sun", 590, "Nature", "Vintage", "Warm"),
        b("b_reuben", "Reuben", Gender.BOY, "Hebrew", "Behold, a cherished son", "ROO-bun", 610, "Biblical", "Warm", "Vintage"),
        b("b_tiberius", "Tiberius", Gender.BOY, "Latin", "Of the sacred river Tiber", "ty-BEER-ee-us", 650, "Ancient Roman", "Historic", "Noble"),

        // Additional Girls
        b("g_claire", "Claire", Gender.GIRL, "French / Latin", "Clear, luminous, bright and pure", "KLAIR", 55, "French", "One Syllable", "Classic"),
        b("g_audrey", "Audrey", Gender.GIRL, "English", "Noble strength, enduring elegance", "AW-dree", 60, "Classic", "Vintage", "Graceful"),
        b("g_stella", "Stella", Gender.GIRL, "Latin", "Radiant star shining in night sky", "STEL-uh", 41, "Celestial", "Vintage", "Bright"),
        b("g_leah", "Leah", Gender.GIRL, "Hebrew", "Weary or delicate meadow gazelle", "LEE-uh", 46, "Biblical", "Gentle", "Classic"),
        b("g_lucy", "Lucy", Gender.GIRL, "Latin", "Light; bringer of dawn and cheer", "LOO-see", 47, "Classic", "Bright", "Cheerful"),
        b("g_hannah", "Hannah", Gender.GIRL, "Hebrew", "Grace, divine favor, gentle warmth", "HAN-uh", 43, "Biblical", "Gentle", "Timeless"),
        b("g_maya_s", "Maya", Gender.GIRL, "Greek / Sanskrit", "Illusion, water, mother of blossoms", "MY-uh", 54, "Global", "Short & Sweet", "Melodic"),
        b("g_eva", "Eva", Gender.GIRL, "Hebrew", "Life, living one, breath of spring", "EE-vuh", 68, "Biblical", "Short & Sweet", "Classic"),
        b("g_naomi", "Naomi", Gender.GIRL, "Hebrew", "Pleasantness, sweetness and delight", "nay-OH-mee", 50, "Biblical", "Melodic", "Warm"),
        b("g_elena_v", "Althea", Gender.GIRL, "Greek", "With healing power, soothing remedy", "al-THEE-uh", 520, "Mythological", "Healing", "Unique"),
        b("g_celeste_g", "Celine", Gender.GIRL, "French / Latin", "Heavenly, moonbeam on water", "seh-LEEN", 310, "French", "Chic", "Melodic"),
        b("g_dahlia", "Dahlia", Gender.GIRL, "Scandinavian", "Dahl's flower; elegance and inner dignity", "DAHL-yuh", 260, "Flower", "Nature", "Vintage"),
        b("g_estelle", "Estelle", Gender.GIRL, "French / Latin", "Star of hope, shining diamond", "eh-STEL", 340, "Celestial", "French", "Vintage"),
        b("g_gemma", "Gemma", Gender.GIRL, "Italian", "Precious jewel, sparkling gemstone", "JEM-uh", 215, "Italian", "Gemstone", "Short & Sweet"),
        b("g_helena", "Helena", Gender.GIRL, "Greek", "Bright, shining light of truth", "heh-LAY-nuh", 370, "Classic", "Royal", "Melodic"),
        b("g_juliette", "Juliette", Gender.GIRL, "French", "Youthful, deeply romantic devotion", "zhoo-lee-ET", 280, "French", "Romantic", "Classic"),
        b("g_kalliope", "Calliope", Gender.GIRL, "Greek", "Beautiful voice; muse of epic poetry", "kuh-LY-oh-pee", 410, "Mythological", "Musical", "Artistic"),
        b("g_lorelei", "Lorelei", Gender.GIRL, "Germanic", "Enchanting river song, alluring voice", "LOR-uh-lye", 430, "Germanic", "Melodic", "Fairy Tale"),
        b("g_magnolia", "Magnolia", Gender.GIRL, "Latin", "Sweet fragrant blossoming tree of nobility", "mag-NOH-lee-uh", 140, "Flower", "Nature", "Southern"),
        b("g_nerissa", "Nerissa", Gender.GIRL, "Greek", "Sea nymph of the gentle waves", "nuh-RIS-uh", 560, "Mythological", "Ocean", "Literary"),
        b("g_octavia", "Octavia", Gender.GIRL, "Latin", "Eighth born; noble Roman empress", "ok-TAY-vee-uh", 310, "Ancient Roman", "Royal", "Unique"),
        b("g_persephone", "Persephone", Gender.GIRL, "Greek", "Bringer of spring and blooming flora", "per-SEF-uh-nee", 620, "Mythological", "Nature", "Regal"),
        b("g_rosalind", "Rosalind", Gender.GIRL, "Germanic / Latin", "Gentle beautiful horse or lovely rose", "ROZ-uh-lind", 640, "Literary", "Vintage", "Romantic"),
        b("g_selene", "Selene", Gender.GIRL, "Greek", "Goddess of the glowing moon", "seh-LEEN", 480, "Celestial", "Mythological", "Graceful"),
        b("g_thalia", "Thalia", Gender.GIRL, "Greek", "To blossom, flourish with joyous laughter", "thuh-LY-uh", 520, "Mythological", "Joyful", "Melodic"),
        b("g_vivienne", "Vivienne", Gender.GIRL, "French / Latin", "Alive, vibrant with boundless life", "vih-vee-EN", 230, "French", "Chic", "Classic"),
        b("g_willa", "Willa", Gender.GIRL, "Germanic", "Resolute protection, strong will", "WIL-uh", 320, "Short & Sweet", "Literary", "Vintage"),
        b("g_zenaida", "Zenaida", Gender.GIRL, "Greek", "Born of Zeus, white winged dove", "zeh-NY-duh", 690, "Greek", "Rare", "Spiritual"),

        // Additional Unisex
        b("u_ciel", "Ciel", Gender.UNISEX, "French", "Sky, heaven, infinite horizon", "see-EL", 480, "French", "Celestial", "One Syllable"),
        b("u_sol", "Sol", Gender.UNISEX, "Spanish / Latin", "The golden shining sun", "SOHL", 420, "Celestial", "Short & Sweet", "Warm"),
        b("u_zephyr", "Zephyr", Gender.UNISEX, "Greek", "Gentle compassionate west wind", "ZEF-er", 380, "Nature", "Mythological", "Unique"),
        b("u_indigo", "Indigo", Gender.UNISEX, "Greek", "Deep mysterious dye of starlight", "IN-dih-goh", 410, "Color", "Artistic", "Modern"),
        b("u_lake", "Lake", Gender.UNISEX, "English", "Tranquil body of calm water", "LAYK", 440, "Nature", "One Syllable", "Serene"),
        b("u_lennon", "Lennon", Gender.UNISEX, "Irish", "Dear sweetheart, poetic lover", "LEN-un", 210, "Celtic", "Musical", "Modern"),
        b("u_milan_u", "Milan", Gender.UNISEX, "Slavic", "Gracious, beloved, eager spirit", "mee-LAHN", 290, "International", "Chic", "Warm"),
        b("u_remington", "Remington", Gender.UNISEX, "English", "Settlement on a raven ridge", "REM-ing-tun", 230, "Modern", "Strong", "Aristocratic"),
        b("u_rowan_u", "Rory", Gender.UNISEX, "Irish / Gaelic", "Red-haired legendary heroic king", "ROR-ee", 270, "Celtic", "Playful", "Classic"),
        b("u_salem", "Salem", Gender.UNISEX, "Hebrew / Arabic", "Peace, safety, complete wholeness", "SAY-lum", 360, "Biblical", "Spiritual", "Unique"),
        b("u_true", "True", Gender.UNISEX, "English", "Honest, loyal, sincere faithfulness", "TROO", 510, "Virtue", "One Syllable", "Modern"),
        b("u_winter", "Winter", Gender.UNISEX, "English", "Season of frost, wonder and renewal", "WIN-ter", 320, "Nature", "Seasonal", "Poetic")
    )

    val initialNames: List<BabyName> = SeedBoyNames.list + SeedGirlNames.list + SeedUnisexNames.list + extendedNames + SeedWorldNames.list

    private fun b(id: String, name: String, gender: Gender, origin: String, meaning: String, pron: String, rank: Int, vararg tags: String): BabyName {
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
