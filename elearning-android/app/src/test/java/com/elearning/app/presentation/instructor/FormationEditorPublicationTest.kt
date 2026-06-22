package com.elearning.app.presentation.instructor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FormationEditorPublicationTest {

    @Test
    fun publicationBlockers_requireTitleAndCurriculum() {
        val state = FormationEditorState()

        val blockers = publicationBlockers(state)

        assertTrue(blockers.contains("Titre et description obligatoires"))
        assertTrue(blockers.contains("Ajoutez au moins un module"))
    }

    @Test
    fun publicationBlockers_requireEveryCourseToHaveSeances() {
        val state = FormationEditorState(
            title = "Kotlin avance",
            description = "Parcours complet",
            courses = listOf(
                EditableCourseState(
                    id = "course-1",
                    key = "course-1",
                    title = "Module 1",
                    description = "",
                    orderIndex = 0,
                    seances = emptyList()
                )
            )
        )

        assertEquals(listOf("Chaque module doit contenir au moins une seance"), publicationBlockers(state))
    }

    @Test
    fun publicationWarnings_includeMissingMediaAndPaidPrice() {
        val state = FormationEditorState(
            title = "Kotlin avance",
            description = "Parcours complet",
            priceMode = "PAID",
            price = "0",
            courses = listOf(
                EditableCourseState(
                    id = "course-1",
                    key = "course-1",
                    title = "Module 1",
                    description = "",
                    orderIndex = 0,
                    seances = listOf(
                        EditableSeanceState(
                            id = "seance-1",
                            title = "Intro",
                            type = "VIDEO",
                            durationSeconds = 600,
                            orderIndex = 0
                        )
                    )
                )
            )
        )

        val warnings = publicationWarnings(state)

        assertTrue(warnings.contains("Image de couverture manquante"))
        assertTrue(warnings.contains("Prix payant non renseigne"))
    }
}
