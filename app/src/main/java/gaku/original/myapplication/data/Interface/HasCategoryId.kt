package gaku.original.myapplication.data.Interface

interface HasCategoryId<T> {
    val categoryId: String?

    fun updateCategoryId(newCategoryId: String?): T
}