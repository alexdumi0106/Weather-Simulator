package com.example.weathersimulator.data.repository

import android.content.Context
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

data class WeatherQuizQuestion(
    val id: String,
    val question: String,
    val answers: List<String>,
    val correctIndex: Int,
    val category: String,
    val difficulty: String,
    val explanation: String
)

data class WeatherQuizQuestionSet(
    val questions: List<WeatherQuizQuestion>,
    val loadedFromFallback: Boolean
)

@Singleton
class WeatherQuizRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val prefs = context.getSharedPreferences(PrefsName, Context.MODE_PRIVATE)

    suspend fun loadRandomQuestions(count: Int): WeatherQuizQuestionSet {
        val firebaseQuestions = runCatching {
            firestore.collection(CollectionName)
                .get()
                .await()
                .documents
                .mapNotNull { it.toWeatherQuizQuestion() }
        }.getOrDefault(emptyList())

        val pool = when {
            firebaseQuestions.size >= count -> firebaseQuestions
            firebaseQuestions.isEmpty() -> fallbackQuestions
            else -> {
                val firebaseIds = firebaseQuestions.map { it.id }.toSet()
                firebaseQuestions + fallbackQuestions.filter { it.id !in firebaseIds }
            }
        }
        val selected = selectWithoutRecentRepeats(pool, count.coerceIn(1, 10))

        return WeatherQuizQuestionSet(
            questions = selected,
            loadedFromFallback = firebaseQuestions.size < count
        )
    }

    private fun selectWithoutRecentRepeats(
        allQuestions: List<WeatherQuizQuestion>,
        count: Int
    ): List<WeatherQuizQuestion> {
        if (allQuestions.isEmpty()) return emptyList()

        val usedIds = prefs.getStringSet(UsedQuestionIdsKey, emptySet()).orEmpty().toSet()
        val unusedQuestions = allQuestions.filter { it.id !in usedIds }
        val selectionPool = if (unusedQuestions.size >= count) {
            unusedQuestions
        } else {
            prefs.edit().remove(UsedQuestionIdsKey).apply()
            allQuestions
        }

        val selectedQuestions = selectionPool
            .shuffled()
            .take(count.coerceAtMost(selectionPool.size))

        rememberUsedQuestions(selectedQuestions.map { it.id })
        return selectedQuestions
    }

    private fun rememberUsedQuestions(questionIds: List<String>) {
        val currentIds = prefs.getStringSet(UsedQuestionIdsKey, emptySet()).orEmpty().toMutableSet()
        currentIds.addAll(questionIds)
        prefs.edit()
            .putStringSet(UsedQuestionIdsKey, currentIds)
            .apply()
    }

    private fun DocumentSnapshot.toWeatherQuizQuestion(): WeatherQuizQuestion? {
        val question = getString("question")?.trim().orEmpty()
        val answers = (get("answers") as? List<*>)
            ?.mapNotNull { it as? String }
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            .orEmpty()
        val correctIndex = getLong("correctIndex")?.toInt()
            ?: (get("correctIndex") as? Number)?.toInt()
            ?: -1
        val explanation = getString("explanation")?.trim().orEmpty()

        if (
            question.isBlank() ||
            answers.size != RequiredAnswerCount ||
            correctIndex !in answers.indices ||
            explanation.isBlank()
        ) {
            return null
        }

        return WeatherQuizQuestion(
            id = id,
            question = question,
            answers = answers,
            correctIndex = correctIndex,
            category = getString("category")?.trim().orEmpty(),
            difficulty = getString("difficulty")?.trim().orEmpty(),
            explanation = explanation
        )
    }

    private companion object {
        const val CollectionName = "quiz_questions"
        const val PrefsName = "weather_quiz_prefs"
        const val UsedQuestionIdsKey = "used_question_ids"
        const val RequiredAnswerCount = 3

        val fallbackQuestions = listOf(
            WeatherQuizQuestion(
                id = "fallback_pressure",
                question = "Ce este presiunea atmosferica?",
                answers = listOf(
                    "Greutatea aerului asupra suprafetei Pamantului",
                    "Cantitatea de ploaie dintr-o zi",
                    "Viteza vantului"
                ),
                correctIndex = 0,
                category = "meteorologie",
                difficulty = "easy",
                explanation = "Presiunea atmosferica reprezinta forta exercitata de coloana de aer asupra unei suprafete."
            ),
            WeatherQuizQuestion(
                id = "fallback_atmosphere_gas",
                question = "Care este principalul gaz din atmosfera Pamantului?",
                answers = listOf(
                    "Oxigen",
                    "Azot",
                    "Dioxid de carbon"
                ),
                correctIndex = 1,
                category = "meteorologie",
                difficulty = "easy",
                explanation = "Azotul reprezinta cea mai mare parte din atmosfera terestra."
            ),
            WeatherQuizQuestion(
                id = "fallback_thermometer",
                question = "Ce instrument masoara temperatura aerului?",
                answers = listOf(
                    "Barometru",
                    "Termometru",
                    "Anemometru"
                ),
                correctIndex = 1,
                category = "meteorologie",
                difficulty = "easy",
                explanation = "Termometrul este instrumentul folosit pentru masurarea temperaturii."
            ),
            WeatherQuizQuestion(
                id = "fallback_rainbow",
                question = "Ce fenomen produce curcubeul?",
                answers = listOf(
                    "Refractia si reflexia luminii in picaturile de apa",
                    "Umbra norilor",
                    "Poluarea atmosferica"
                ),
                correctIndex = 0,
                category = "meteorologie",
                difficulty = "easy",
                explanation = "Curcubeul apare cand lumina solara este refractata si reflectata in picaturile de ploaie."
            ),
            WeatherQuizQuestion(
                id = "fallback_humidity_100",
                question = "Ce indica o umiditate relativa de 100%?",
                answers = listOf(
                    "Aer foarte uscat",
                    "Aer saturat cu vapori de apa",
                    "Vant foarte puternic"
                ),
                correctIndex = 1,
                category = "meteorologie",
                difficulty = "easy",
                explanation = "La 100% umiditate relativa, aerul este saturat cu vapori de apa."
            ),
            WeatherQuizQuestion(
                id = "fallback_cumulonimbus",
                question = "Care nor este asociat cel mai des cu furtunile?",
                answers = listOf(
                    "Cirrus",
                    "Stratus",
                    "Cumulonimbus"
                ),
                correctIndex = 2,
                category = "nori",
                difficulty = "easy",
                explanation = "Norii cumulonimbus se dezvolta vertical si pot produce furtuni, fulgere si grindina."
            ),
            WeatherQuizQuestion(
                id = "fallback_anemometer",
                question = "Ce instrument masoara viteza vantului?",
                answers = listOf(
                    "Anemometru",
                    "Termometru",
                    "Pluviometru"
                ),
                correctIndex = 0,
                category = "meteorologie",
                difficulty = "easy",
                explanation = "Anemometrul este instrumentul utilizat pentru masurarea vitezei vantului."
            ),
            WeatherQuizQuestion(
                id = "fallback_rain",
                question = "Cum se numeste apa care cade din atmosfera sub forma lichida?",
                answers = listOf(
                    "Ninsoare",
                    "Ploaie",
                    "Grindina"
                ),
                correctIndex = 1,
                category = "meteorologie",
                difficulty = "easy",
                explanation = "Ploaia este forma lichida a precipitatiilor."
            ),
            WeatherQuizQuestion(
                id = "fallback_cirrus",
                question = "Ce tip de nor este subtire si fibros?",
                answers = listOf(
                    "Cirrus",
                    "Cumulus",
                    "Nimbostratus"
                ),
                correctIndex = 0,
                category = "nori",
                difficulty = "easy",
                explanation = "Norii cirrus sunt subtiri, fibrosi si apar la altitudini mari."
            ),
            WeatherQuizQuestion(
                id = "fallback_evaporation",
                question = "Cum se numeste fenomenul prin care apa se transforma in vapori?",
                answers = listOf(
                    "Condensare",
                    "Evaporare",
                    "Sublimare"
                ),
                correctIndex = 1,
                category = "climatologie",
                difficulty = "easy",
                explanation = "Evaporarea este procesul prin care apa lichida se transforma in vapori."
            ),
            WeatherQuizQuestion(
                id = "fallback_anticyclone",
                question = "Ce reprezinta un anticiclon?",
                answers = listOf(
                    "Zona cu presiune ridicata",
                    "Zona cu presiune scazuta",
                    "Un tip de nor"
                ),
                correctIndex = 0,
                category = "meteorologie",
                difficulty = "medium",
                explanation = "Anticiclonii sunt asociati de obicei cu vreme stabila si cer mai senin."
            ),
            WeatherQuizQuestion(
                id = "fallback_cyclone",
                question = "Ce reprezinta un ciclon?",
                answers = listOf(
                    "Zona cu presiune ridicata",
                    "Zona cu presiune scazuta",
                    "O forma de precipitatie"
                ),
                correctIndex = 1,
                category = "meteorologie",
                difficulty = "medium",
                explanation = "Ciclonii sunt zone cu presiune scazuta si favorizeaza formarea norilor si precipitatiilor."
            ),
            WeatherQuizQuestion(
                id = "fallback_fog",
                question = "Ce este ceata?",
                answers = listOf(
                    "Un nor aflat la nivelul solului",
                    "O furtuna de praf",
                    "O ploaie slaba"
                ),
                correctIndex = 0,
                category = "meteorologie",
                difficulty = "easy",
                explanation = "Ceata este formata din picaturi foarte mici de apa suspendate aproape de sol."
            ),
            WeatherQuizQuestion(
                id = "fallback_tornado_scale",
                question = "Ce scara este utilizata pentru clasificarea tornadelor?",
                answers = listOf(
                    "Richter",
                    "Fujita Imbunatatita",
                    "Beaufort"
                ),
                correctIndex = 1,
                category = "fenomene_extreme",
                difficulty = "medium",
                explanation = "Scara Fujita Imbunatatita clasifica tornadele in functie de daunele produse."
            ),
            WeatherQuizQuestion(
                id = "fallback_rain_gauge",
                question = "Ce masoara pluviometrul?",
                answers = listOf(
                    "Cantitatea de precipitatii",
                    "Presiunea atmosferica",
                    "Umiditatea aerului"
                ),
                correctIndex = 0,
                category = "meteorologie",
                difficulty = "easy",
                explanation = "Pluviometrul masoara cantitatea de precipitatii cazuta intr-o anumita perioada."
            ),
            WeatherQuizQuestion(
                id = "fallback_hail",
                question = "Ce este grindina?",
                answers = listOf(
                    "Picaturi de apa foarte reci",
                    "Bucati de gheata formate in norii de furtuna",
                    "Cristale de zapada"
                ),
                correctIndex = 1,
                category = "fenomene_extreme",
                difficulty = "easy",
                explanation = "Grindina se formeaza in norii de furtuna, mai ales in cumulonimbus."
            ),
            WeatherQuizQuestion(
                id = "fallback_sun_energy",
                question = "Care este sursa principala de energie a vremii pe Pamant?",
                answers = listOf(
                    "Luna",
                    "Soarele",
                    "Vantul"
                ),
                correctIndex = 1,
                category = "climatologie",
                difficulty = "easy",
                explanation = "Soarele furnizeaza energia care pune in miscare procesele atmosferice."
            ),
            WeatherQuizQuestion(
                id = "fallback_condensation",
                question = "Cum se numeste procesul prin care vaporii de apa devin picaturi?",
                answers = listOf(
                    "Evaporare",
                    "Condensare",
                    "Topire"
                ),
                correctIndex = 1,
                category = "meteorologie",
                difficulty = "easy",
                explanation = "Condensarea este procesul prin care vaporii de apa se transforma in picaturi si pot forma nori."
            ),
            WeatherQuizQuestion(
                id = "fallback_pressure_unit",
                question = "Care este unitatea uzuala pentru presiunea atmosferica?",
                answers = listOf(
                    "hPa",
                    "km/h",
                    "Grade Celsius"
                ),
                correctIndex = 0,
                category = "meteorologie",
                difficulty = "medium",
                explanation = "Presiunea atmosferica este exprimata frecvent in hectopascali, adica hPa."
            ),
            WeatherQuizQuestion(
                id = "fallback_cold_front",
                question = "Ce tip de front aduce adesea furtuni puternice?",
                answers = listOf(
                    "Front rece",
                    "Front cald",
                    "Front stationar"
                ),
                correctIndex = 0,
                category = "meteorologie",
                difficulty = "medium",
                explanation = "Fronturile reci pot ridica rapid aerul cald, favorizand averse si furtuni."
            ),
            WeatherQuizQuestion(
                id = "fallback_troposphere_weather",
                question = "Cum se numeste stratul atmosferic in care se produce vremea?",
                answers = listOf(
                    "Stratosfera",
                    "Troposfera",
                    "Mezosfera"
                ),
                correctIndex = 1,
                category = "meteorologie",
                difficulty = "medium",
                explanation = "Majoritatea fenomenelor meteorologice au loc in troposfera."
            ),
            WeatherQuizQuestion(
                id = "fallback_pressure_drop",
                question = "Ce poate indica o presiune atmosferica in scadere?",
                answers = listOf(
                    "Posibila deteriorare a vremii",
                    "Cer complet senin garantat",
                    "Lipsa vantului"
                ),
                correctIndex = 0,
                category = "meteorologie",
                difficulty = "medium",
                explanation = "Presiunea in scadere poate indica apropierea unui sistem depresionar si vreme instabila."
            ),
            WeatherQuizQuestion(
                id = "fallback_heat_wave",
                question = "Ce este un val de caldura?",
                answers = listOf(
                    "Perioada cu temperaturi mult peste normal",
                    "O furtuna tropicala",
                    "O perioada cu umiditate scazuta"
                ),
                correctIndex = 0,
                category = "climatologie",
                difficulty = "easy",
                explanation = "Valul de caldura este o perioada cu temperaturi ridicate, peste valorile normale ale zonei."
            ),
            WeatherQuizQuestion(
                id = "fallback_convection",
                question = "Ce fenomen apare atunci cand aerul cald urca?",
                answers = listOf(
                    "Convectie",
                    "Reflexie",
                    "Eroziune"
                ),
                correctIndex = 0,
                category = "meteorologie",
                difficulty = "medium",
                explanation = "Convectia este miscarea aerului cald in sus si are un rol important in formarea norilor."
            ),
            WeatherQuizQuestion(
                id = "fallback_snow",
                question = "Ce tip de precipitatie este format din cristale de gheata?",
                answers = listOf(
                    "Ploaia",
                    "Ninsoarea",
                    "Burnita"
                ),
                correctIndex = 1,
                category = "meteorologie",
                difficulty = "easy",
                explanation = "Ninsoarea este formata din cristale de gheata care cad sub forma de fulgi."
            ),
            WeatherQuizQuestion(
                id = "fallback_lowest_layer",
                question = "Care este cel mai apropiat strat al atmosferei de suprafata Pamantului?",
                answers = listOf(
                    "Troposfera",
                    "Stratosfera",
                    "Termosfera"
                ),
                correctIndex = 0,
                category = "meteorologie",
                difficulty = "easy",
                explanation = "Troposfera incepe de la nivelul solului si contine majoritatea fenomenelor meteo."
            ),
            WeatherQuizQuestion(
                id = "fallback_greenhouse_effect",
                question = "Ce este efectul de sera?",
                answers = listOf(
                    "Procesul prin care anumite gaze retin caldura in atmosfera",
                    "Formarea norilor",
                    "Masurarea temperaturii"
                ),
                correctIndex = 0,
                category = "climatologie",
                difficulty = "medium",
                explanation = "Efectul de sera este procesul prin care anumite gaze retin o parte din caldura radiata de Pamant."
            ),
            WeatherQuizQuestion(
                id = "fallback_lightning",
                question = "Ce fenomen meteo produce descarcari electrice?",
                answers = listOf(
                    "Ceata",
                    "Furtuna",
                    "Burnita"
                ),
                correctIndex = 1,
                category = "fenomene_extreme",
                difficulty = "easy",
                explanation = "Fulgerele apar in timpul furtunilor electrice."
            ),
            WeatherQuizQuestion(
                id = "fallback_forecast",
                question = "Ce inseamna prognoza meteo?",
                answers = listOf(
                    "Estimarea conditiilor atmosferice viitoare",
                    "Masurarea temperaturii curente",
                    "Analiza solului"
                ),
                correctIndex = 0,
                category = "meteorologie",
                difficulty = "easy",
                explanation = "Prognoza meteo foloseste observatii si modele pentru a estima evolutia vremii."
            ),
            WeatherQuizQuestion(
                id = "fallback_freezing_point",
                question = "Care este temperatura la care apa ingheata la presiune normala?",
                answers = listOf(
                    "0 grade Celsius",
                    "10 grade Celsius",
                    "-10 grade Celsius"
                ),
                correctIndex = 0,
                category = "meteorologie",
                difficulty = "easy",
                explanation = "Apa ingheata la 0 grade Celsius in conditii normale de presiune."
            )
        )
    }
}
