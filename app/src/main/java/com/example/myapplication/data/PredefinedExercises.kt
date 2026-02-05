package com.example.myapplication.data

data class PredefinedExercise(
    val name: String,
    val target: String,
    val category: String,
    val defaultSets: String = "4x8-12"
)

val predefinedExercisesList = listOf(
    // Peito
    PredefinedExercise("Supino Reto", "Peitoral Maior", "Peito"),
    PredefinedExercise("Supino Inclinado", "Peitoral Superior", "Peito"),
    PredefinedExercise("Supino Declinado", "Peitoral Inferior", "Peito"),
    PredefinedExercise("Crucifixo Reto", "Peitoral", "Peito"),
    PredefinedExercise("Crucifixo Inclinado", "Peitoral Superior", "Peito"),
    PredefinedExercise("Peck Deck", "Peitoral", "Peito"),
    PredefinedExercise("Cross Over", "Peitoral Inferior/Médio", "Peito"),
    PredefinedExercise("Flexão de Braços", "Peitoral/Tríceps", "Peito"),
    
    // Costas
    PredefinedExercise("Remada Curvada", "Dorsais", "Costas"),
    PredefinedExercise("Puxada Alta", "Latíssimo do Dorso", "Costas"),
    PredefinedExercise("Remada Baixa", "Costas Geral", "Costas"),
    PredefinedExercise("Levantamento Terra", "Cadeia Posterior", "Costas"),
    PredefinedExercise("Barra Fixa", "Latíssimo do Dorso", "Costas"),
    PredefinedExercise("Remada Cavalinho", "Costas Geral", "Costas"),
    PredefinedExercise("Pull Down", "Latíssimo do Dorso", "Costas"),
    PredefinedExercise("Serrote", "Dorsais", "Costas"),
    
    // Pernas
    PredefinedExercise("Agachamento Livre", "Quadríceps", "Pernas"),
    PredefinedExercise("Agachamento Sumô", "Adutores/Glúteos", "Pernas"),
    PredefinedExercise("Leg Press 45", "Quadríceps/Glúteos", "Pernas"),
    PredefinedExercise("Cadeira Extensora", "Quadríceps", "Pernas"),
    PredefinedExercise("Mesa Flexora", "Posterior de Coxa", "Pernas"),
    PredefinedExercise("Elevação Pélvica", "Glúteos", "Pernas"),
    PredefinedExercise("Afundo", "Quadríceps/Glúteos", "Pernas"),
    PredefinedExercise("Stiff", "Posterior de Coxa/Glúteos", "Pernas"),
    PredefinedExercise("Cadeira Adutora", "Adutores", "Pernas"),
    PredefinedExercise("Cadeira Abdutora", "Glúteo Médio", "Pernas"),
    
    // Panturrilhas
    PredefinedExercise("Gêmeos em Pé", "Panturrilhas", "Pernas"),
    PredefinedExercise("Gêmeos Sentado", "Sóleo", "Pernas"),
    
    // Ombros
    PredefinedExercise("Desenvolvimento Militar", "Deltoides", "Ombros"),
    PredefinedExercise("Arnold Press", "Deltoides", "Ombros"),
    PredefinedExercise("Elevação Lateral", "Deltoide Lateral", "Ombros"),
    PredefinedExercise("Elevação Frontal", "Deltoide Anterior", "Ombros"),
    PredefinedExercise("Crucifixo Inverso", "Deltoide Posterior", "Ombros"),
    PredefinedExercise("Encolhimento", "Trapézio", "Ombros"),
    PredefinedExercise("Remada Alta", "Trapézio/Deltoide", "Ombros"),
    
    // Braços
    PredefinedExercise("Rosca Direta", "Bíceps", "Braços"),
    PredefinedExercise("Rosca Martelo", "Braquiorradial", "Braços"),
    PredefinedExercise("Rosca Alternada", "Bíceps", "Braços"),
    PredefinedExercise("Rosca Concentrada", "Bíceps", "Braços"),
    PredefinedExercise("Rosca Scott", "Bíceps", "Braços"),
    PredefinedExercise("Tríceps Pulley", "Tríceps", "Braços"),
    PredefinedExercise("Tríceps Corda", "Tríceps", "Braços"),
    PredefinedExercise("Tríceps Testa", "Tríceps", "Braços"),
    PredefinedExercise("Tríceps Francês", "Tríceps", "Braços"),
    PredefinedExercise("Mergulho Paralelas", "Tríceps/Peito Inferior", "Braços"),
    
    // Abdominais
    PredefinedExercise("Abdominal Supra", "Reto Abdominal", "Core"),
    PredefinedExercise("Elevação de Pernas", "Abdominal Inferior", "Core"),
    PredefinedExercise("Plancha Isométrica", "Core Geral", "Core"),
    PredefinedExercise("Abdominal Oblíquo", "Oblíquos", "Core")
)
