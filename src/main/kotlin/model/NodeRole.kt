package model

enum class NodeRole(val role : Int) {
    NORMAL(0), // Обычный узел, лист в топологии "звезда"
    MASTER(1), // Главный узел, центр в топологии "звезда"
    DEPUTY(2), // Заместитель главного узла
    VIEWER(3)
}