package com.elearning.app.presentation.main

object MainRoutes {
    private const val PLAYER_PREFIX = "player/"
    private const val QUIZ_PREFIX = "quiz/"
    private const val INSTRUCTOR_PREFIX = "instructor"

    const val HOME = "home"
    const val MY_TRAININGS = "my_trainings"
    const val FAVORITES = "favorites"
    const val CERTIFICATES = "certificates"
    const val CATALOGUE = "catalogue"
    const val CATALOGUE_WITH_FILTER = "catalogue?categoryId={categoryId}"
    const val FORMATION_DETAIL = "formation_detail/{formationId}"
    const val PLAYER = "player/{seanceId}"
    const val SCANNER = "scanner"
    const val PROFILE = "profile"
    const val QUIZ = "quiz/{quizId}"
    const val QUIZ_HISTORY = "quiz_history"
    const val NOTIFICATIONS = "notifications"
    const val INSTRUCTOR_DASHBOARD = "instructor"
    const val INSTRUCTOR_FORMATION_NEW = "instructor/formation/new"
    const val INSTRUCTOR_FORMATION_EDIT = "instructor/formation/{formationId}/edit"
    const val INSTRUCTOR_SEANCE_EDIT = "instructor/seance/{seanceId}/edit"

    fun isCertificatesRoute(route: String?) = route == CERTIFICATES
    fun isProfileRoute(route: String?) = route == PROFILE
    fun isQuizHistoryRoute(route: String?) = route == QUIZ_HISTORY
    fun formationDetail(id: String) = "formation_detail/$id"
    fun catalogue(categoryId: String? = null) = if (categoryId.isNullOrBlank()) CATALOGUE else "catalogue?categoryId=$categoryId"
    fun player(seanceId: String) = "$PLAYER_PREFIX$seanceId"
    fun quiz(quizId: String) = "$QUIZ_PREFIX$quizId"
    fun instructorFormationEdit(formationId: String) = "instructor/formation/$formationId/edit"
    fun instructorSeanceEdit(seanceId: String) = "instructor/seance/$seanceId/edit"
    fun isPlayerRoute(route: String?) = route?.startsWith(PLAYER_PREFIX) == true
    fun isQuizRoute(route: String?) = route?.startsWith(QUIZ_PREFIX) == true
    fun isInstructorRoute(route: String?) = route?.startsWith(INSTRUCTOR_PREFIX) == true

    fun resolveDeepLink(deepLink: String?): String? {
        val value = deepLink?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val route = value
            .removePrefix("elearning://")
            .removePrefix("app://")
            .removePrefix("/")
        return when {
            route == CERTIFICATES || route == "certificate" -> CERTIFICATES
            route == PROFILE -> PROFILE
            route == QUIZ_HISTORY || route == "quiz-history" || route == "quizzes/history" -> QUIZ_HISTORY
            route == NOTIFICATIONS -> NOTIFICATIONS
            route.startsWith(PLAYER_PREFIX) -> route
            route.startsWith(QUIZ_PREFIX) -> route
            else -> null
        }
    }
}
