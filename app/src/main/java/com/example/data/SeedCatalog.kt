package com.example.data

object SeedCatalog {

    suspend fun populateIfEmpty(dao: InkwellDao) {
        if (dao.getStoryCount() > 0) return

        val now = System.currentTimeMillis()
        val hour = 3_600_000L

        // 1. Fantasy Featured Story
        val s1 = dao.insertStory(
            StoryEntity(
                title = "The Gate of Twin Moons",
                authorName = "Elena Vance",
                authorHandle = "@elenavance",
                synopsis = "For three centuries, the Archway at Cinder Pass remained dormant stone. When apprentice cartographer Lyra Kael touches its weathered runes during the eclipse of the twin moons, the seal breaks—dragging her into an empire of floating citadels where memories are traded as currency.",
                genre = "Fantasy",
                tagsCsv = "fantasy,magic,portal,slowburn,adventure",
                coverKey = "res:starlight",
                status = "Ongoing",
                readsCount = 148_400,
                votesCount = 12_940,
                inLibrary = true,
                readingListName = "Currently Reading",
                lastReadChapterIndex = 0,
                lastReadScrollProgress = 0.35f,
                isFeatured = true,
                updatedAt = now - 2 * hour
            )
        )

        val s1c1 = dao.insertChapter(
            ChapterEntity(
                storyId = s1,
                chapterNumber = 1,
                title = "Chapter 1: Charcoal & Starlight",
                body = """
The wind above Cinder Pass tasted of copper and oncoming snow. Lyra pulled her wool cloak tighter around her shoulders and knelt beside the cracked obsidian threshold, steadying her brass lantern against the rock.

Everyone in the valley knew the Archway was nothing more than an overgrown ruin—a monument left behind by builders whose names had eroded off every marble pillar in the northern reach. Yet tonight, with both the pale Amber Moon and the crescent Silver Moon aligned directly overhead, the grooves carved into the stone were faintly humming.

She pressed the flat of her charcoal stick against a sheet of vellum, trying to trace the spiral script before the frost numbed her fingers. Halfway through the third glyph, her wrist slipped. Bare skin brushed the carved channel.

Gold light erupted from the fissure—not scorching, not blinding, but warm as summer rain. The air folded inward with the sound of a struck harp string, and for the first time in three hundred years, the Gate opened onto a sky filled with drifting lanterns.
                """.trimIndent(),
                authorNote = "Welcome to Book One! Let me know what you think of Lyra's world in the inline comments.",
                isPublished = true,
                votesCount = 4_810,
                wordCount = 189,
                updatedAt = now - 20 * hour
            )
        )

        val s1c2 = dao.insertChapter(
            ChapterEntity(
                storyId = s1,
                chapterNumber = 2,
                title = "Chapter 2: The Market of Borrowed Hours",
                body = """
On the far side of the threshold, gravity felt half a breath lighter. Beneath Lyra's boots stretched a bridge of white basalt suspended over clouds tinted violet by distant fires.

A boy no older than twenty leaned against the balustrade, tossing a glass vial from hand to hand. Inside the vial, something glowed like trapped lightning.

"You're either terribly lost," he said without looking up, "or someone in the Lower Reach finally figured out how to wake a sleeping stone."

"My lantern went out," Lyra answered, staring at the floating spires rising across the mist. "Where is this?"

He tucked the vial into his coat and offered a crooked bow. "Call me Cassian. And if you want to keep your name by sunrise, keep your gloves on and don't sell anyone a single memory."
                """.trimIndent(),
                authorNote = "Cassian finally enters the story! Next update drops Friday.",
                isPublished = true,
                votesCount = 4_120,
                wordCount = 141,
                updatedAt = now - 8 * hour
            )
        )

        val s1c3 = dao.insertChapter(
            ChapterEntity(
                storyId = s1,
                chapterNumber = 3,
                title = "Chapter 3: Glass & Ash",
                body = """
The bell tower at the center of the High Archway tolled thirteen times—once for every district still anchored above the cloud line.

Cassian led Lyra down a narrow stairwell flanked by apothecary stalls where merchants weighed bottled laughter on brass scales. Even the air smelled unfamiliar: crushed cardamom, ozone, and rain on hot slate.

"Why can't I sell a memory?" Lyra asked, hurrying to match his stride.

"Because the buyers never tell you which threads come attached," Cassian replied quietly. "You think you're trading away one winter afternoon, and by morning you've forgotten your mother's voice."
                """.trimIndent(),
                authorNote = "Thank you for 140K reads!!",
                isPublished = true,
                votesCount = 4_010,
                wordCount = 104,
                updatedAt = now - 2 * hour
            )
        )

        dao.insertComment(
            CommentEntity(
                chapterId = s1c1,
                storyId = s1,
                authorName = "Maya Lin",
                authorInitials = "ML",
                text = "The sensory detail in this opening paragraph hooked me immediately!",
                paragraphIndex = 0,
                likesCount = 84,
                createdAt = now - 5 * hour
            )
        )
        dao.insertComment(
            CommentEntity(
                chapterId = s1c1,
                storyId = s1,
                authorName = "Devon Cross",
                authorInitials = "DC",
                text = "That harp-string image when the portal wakes up gave me chills.",
                paragraphIndex = 3,
                likesCount = 112,
                createdAt = now - 3 * hour
            )
        )
        dao.insertComment(
            CommentEntity(
                chapterId = s1c2,
                storyId = s1,
                authorName = "Sora Takahashi",
                authorInitials = "ST",
                text = "Cassian is already my favorite character, zero hesitation.",
                paragraphIndex = 4,
                likesCount = 67,
                createdAt = now - 1 * hour
            )
        )

        // 2. Sci-Fi Featured Story
        val s2 = dao.insertStory(
            StoryEntity(
                title = "Neon Rain Over Neo-Kyoto",
                authorName = "Kenji Sato",
                authorHandle = "@kenjisato",
                synopsis = "In Sector 9, where the rain never stops and synthetic memories are sold on street corners, courier Kai Mori intercepts a black-market data drive containing his own voice—from three years before he woke up with no past.",
                genre = "Sci-Fi",
                tagsCsv = "cyberpunk,scifi,thriller,noir,mystery",
                coverKey = "res:neon_rain",
                status = "Completed",
                readsCount = 215_900,
                votesCount = 19_400,
                inLibrary = true,
                readingListName = "Favorites",
                isFeatured = true,
                updatedAt = now - 6 * hour
            )
        )

        val s2c1 = dao.insertChapter(
            ChapterEntity(
                storyId = s2,
                chapterNumber = 1,
                title = "Part I: Frequency 404",
                body = """
The holographic kanji above Crosswalk 14 flickered coral against the wet asphalt, painting every puddle the color of cooling embers. Kai adjusted his collar mic and stepped beneath the awning of a closed ramen stand.

His neural hud pinged once. Package delivered. Payment pending.

Except the courier satchel slung across his chest wasn't empty. Tucked inside the waterproof lining sat an unlabelled titanium drive—still warm, as if someone had slipped it there less than five minutes ago in the crowded monorail car.

When he slotted it into his wrist deck, only one audio file appeared. Timestamp: November 24, three years before the accident. And when he pressed play, the voice whispering back through the static was unmistakably his own.
                """.trimIndent(),
                authorNote = "Put on your favorite lo-fi synthwave playlist while reading this chapter!",
                isPublished = true,
                votesCount = 9_800,
                wordCount = 128,
                updatedAt = now - 48 * hour
            )
        )

        dao.insertChapter(
            ChapterEntity(
                storyId = s2,
                chapterNumber = 2,
                title = "Part II: Ghost in the Monorail",
                body = """
"If you're listening to this, Kai, don't go back to the Spire."

The recording cut cleanly into silence, leaving only the hiss of real rain drumming on corrugated tin. Across the street, a drone swept its cyan searchlight over the elevated tracks, pausing half a second too long on the awning where Kai stood.

He slipped the drive out, pocketed it deep inside his jacket, and melted into the midnight crowd heading toward the Underground Market. Whoever had planted it wanted him running—and in Neo-Kyoto, running was the fastest way to stay alive.
                """.trimIndent(),
                isPublished = true,
                votesCount = 9_600,
                wordCount = 100,
                updatedAt = now - 24 * hour
            )
        )

        dao.insertComment(
            CommentEntity(
                chapterId = s2c1,
                storyId = s2,
                authorName = "Ariana Sol",
                authorInitials = "AS",
                text = "The cyberpunk atmosphere here is unreal. I can hear the rain!",
                paragraphIndex = 0,
                likesCount = 152,
                createdAt = now - 10 * hour
            )
        )

        // 3. Romance Featured Story
        val s3 = dao.insertStory(
            StoryEntity(
                title = "October at The Nook",
                authorName = "Clara Beaumont",
                authorHandle = "@clarabwrites",
                synopsis = "Two rival bookshop owners on opposite sides of a cobblestone street in Edinburgh accidentally get locked into the same autumn literary festival committee—and discover they've been annotating each other's returned library books for years.",
                genre = "Romance",
                tagsCsv = "romance,cozy,bookshop,autumn,enemiestolovers",
                coverKey = "res:autumn_cafe",
                status = "Ongoing",
                readsCount = 96_300,
                votesCount = 11_250,
                inLibrary = true,
                readingListName = "Read Later",
                isFeatured = true,
                updatedAt = now - 12 * hour
            )
        )

        val s3c1 = dao.insertChapter(
            ChapterEntity(
                storyId = s3,
                chapterNumber = 1,
                title = "Chapter 1: Marginalia",
                body = """
Nora had three strict rules for surviving October in Old Town: never run out of cinnamon syrup, never trust a forecast that promised dry cobblestones, and never, under any circumstances, lend Julian Mercer a ladder.

Unfortunately, Julian was currently standing beneath her shop's awning, holding a tangle of warm fairy lights and wearing an expression of deeply irritating confidence.

"Before you shut the door," he said, shaking rain from his wool scarf, "the festival committee moved our booths next to each other. So technically, we're co-hosts."

Nora stared at the espresso steaming in her hand, debating whether throwing it on the cobblestones would count as dramatic or merely wasteful. Inside her coat pocket sat a battered paperback she had borrowed from the municipal archive—its margins covered in a stranger's familiar, sharp-angled handwriting.
                """.trimIndent(),
                authorNote = "Cozy autumn vibes ahead! Grab a mug of tea.",
                isPublished = true,
                votesCount = 5_700,
                wordCount = 136,
                updatedAt = now - 18 * hour
            )
        )

        dao.insertChapter(
            ChapterEntity(
                storyId = s3,
                chapterNumber = 2,
                title = "Chapter 2: Cinnamon & Second Drafts",
                body = """
By seven o'clock, the string lights along the curved window frame finally glowed amber against the indigo dusk.

Julian balanced on the second rung of the brass step-stool, taping the last cable neatly out of sight. "Admit it," he called down. "My lighting design makes your romance display look twice as inviting."

"It makes the glare terrible," Nora shot back—though she couldn't stop the smile tugging at the corner of her mouth as maple leaves drifted past the warm glass.
                """.trimIndent(),
                isPublished = true,
                votesCount = 5_550,
                wordCount = 81,
                updatedAt = now - 12 * hour
            )
        )

        dao.insertComment(
            CommentEntity(
                chapterId = s3c1,
                storyId = s3,
                authorName = "Tessa Reed",
                authorInitials = "TR",
                text = "Wait—THEY'VE BEEN ANNOTATING THE SAME BOOKS?! Instant library add!",
                paragraphIndex = 3,
                likesCount = 204,
                createdAt = now - 4 * hour
            )
        )

        // 4. Mystery Story
        val s4 = dao.insertStory(
            StoryEntity(
                title = "The Thirteenth Hour Key",
                authorName = "Arthur Pendelton",
                authorHandle = "@apendelton",
                synopsis = "When an eccentric horologist vanishes from his locked study in Vienna, he leaves behind only a stopped brass pocket watch and a skeleton key engraved with his estranged granddaughter's initials.",
                genre = "Mystery",
                tagsCsv = "mystery,historical,detective,lockedroom,suspense",
                coverKey = "res:clockwork",
                status = "Completed",
                readsCount = 74_800,
                votesCount = 6_890,
                updatedAt = now - 24 * hour
            )
        )

        dao.insertChapter(
            ChapterEntity(
                storyId = s4,
                chapterNumber = 1,
                title = "Case File I: Stopped at Midnight",
                body = """
The tallow candle on Grandfather's mahogany desk had burned down to a pool of amber wax, yet the study door remained bolted from the inside.

Inspector Richter stepped back from the bookshelves, letting Mara examine the brass pocket watch resting beside the inkwell. Its hinged lid stood open, revealing Roman numerals and hands frozen at three minutes past twelve.

"There is no second exit," Richter murmured. "No hidden stairwell behind the leather volumes."

Mara picked up the iron skeleton key beside the watch. Underneath its ornate bow, etched in letters so fine they caught the candlelight like spider silk, were three initials—M.V.K.—and a date she hadn't seen since childhood.
                """.trimIndent(),
                isPublished = true,
                votesCount = 3_500,
                wordCount = 115,
                updatedAt = now - 30 * hour
            )
        )

        dao.insertChapter(
            ChapterEntity(
                storyId = s4,
                chapterNumber = 2,
                title = "Case File II: The Clockmaker's Cipher",
                body = """
When Mara wound the watch crown counterclockwise—something every apprentice clockmaker knew was forbidden—the tiny seconds dial clicked loose, revealing a folded strip of onion-skin paper coiled beneath the escapement.

Written in iron-gall ink were four coordinates in the Old Quarter and a single sentence: *If the Inspector asks for the key, trust only the bell-founder.*
                """.trimIndent(),
                isPublished = true,
                votesCount = 3_390,
                wordCount = 58,
                updatedAt = now - 24 * hour
            )
        )

        // 5. Drama Story (Artistic Gradient Cover)
        val s5 = dao.insertStory(
            StoryEntity(
                title = "Letters from the Lighthouse",
                authorName = "Nadia Okafor",
                authorHandle = "@nadiaokafor",
                synopsis = "After inheriting her estranged aunt's coastal radio station on a windswept Maine island, Mara discovers a crate of unsent letters spanning forty summers—and a community waiting for someone to broadcast again.",
                genre = "Drama",
                tagsCsv = "drama,coastal,family,literary,healing",
                coverKey = "gradient:ocean",
                status = "Ongoing",
                readsCount = 51_200,
                votesCount = 4_920,
                updatedAt = now - 36 * hour
            )
        )

        dao.insertChapter(
            ChapterEntity(
                storyId = s5,
                chapterNumber = 1,
                title = "1. Static at Low Tide",
                body = """
The ferry horn echoed across the harbor long after the fog swallowed its hull. Standing on the timber pier with two canvas duffel bags and a key ring heavy with salt-pitted brass, Juno wondered whether her aunt had left the radio tower standing out of stubbornness or hope.

Inside the weatherboard cottage at the end of the headland, dust motes floated in pale morning light. On the oak console sat a reel-to-reel tape deck, a ceramic mug, and a note taped neatly beside the microphone switch: *Start with the weather. Everything else will follow.*
                """.trimIndent(),
                isPublished = true,
                votesCount = 4_920,
                wordCount = 97,
                updatedAt = now - 36 * hour
            )
        )

        // 6. Adventure / Historical Story (Artistic Gradient Cover)
        val s6 = dao.insertStory(
            StoryEntity(
                title = "Salt & Astrolabe",
                authorName = "Mateo Silva",
                authorHandle = "@mateosilva",
                synopsis = "A young navigator aboard a sixteenth-century spice caravel charts an uncharted archipelago where the stars refuse to match any map in Lisbon.",
                genre = "Fantasy",
                tagsCsv = "adventure,nautical,historical,stars",
                coverKey = "gradient:amber",
                status = "Ongoing",
                readsCount = 38_700,
                votesCount = 3_410,
                updatedAt = now - 48 * hour
            )
        )

        dao.insertChapter(
            ChapterEntity(
                storyId = s6,
                chapterNumber = 1,
                title = "Log Entry 1: Beyond the Trade Winds",
                body = """
By the third week south of the equator, the compass needle stopped pointing north altogether. Instead, it trembled gently toward the eastern horizon, as if drawn by an unseen lodestone beneath the turquoise swell.

Captain Navarro refused to turn back. Up in the crow's nest, Inês held her brass astrolabe up to the twilight sky and squinted at a constellation no Lisbon astronomer had ever named.
                """.trimIndent(),
                isPublished = true,
                votesCount = 3_410,
                wordCount = 67,
                updatedAt = now - 48 * hour
            )
        )
    }
}
