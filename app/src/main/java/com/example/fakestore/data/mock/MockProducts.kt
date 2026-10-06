package com.example.fakestore.data.mock

/**
 * Sample catalogue returned by [MockBackendInterceptor], in the exact JSON shape of
 * `GET https://fakestoreapi.com/products`. Images come from picsum.photos because the
 * FakeStore image host is down together with its API.
 */
internal object MockProducts {

    private data class Item(
        val id: Int,
        val title: String,
        val price: Double,
        val category: String,
        val description: String,
        val rate: Double,
        val count: Int,
    )

    private val items = listOf(
        Item(1, "Fjallraven Foldsack No. 1 Backpack", 109.95, "men's clothing",
            "Your perfect pack for everyday use and walks in the forest. Stash your laptop (up to 15 inches) in the padded sleeve.", 3.9, 120),
        Item(2, "Mens Casual Premium Slim Fit T-Shirts", 22.30, "men's clothing",
            "Slim-fitting style, contrast raglan long sleeve, three-button henley placket, light weight and soft fabric.", 4.1, 259),
        Item(3, "Mens Cotton Jacket", 55.99, "men's clothing",
            "Great outerwear jacket for spring, autumn and winter, suitable for working, hiking, camping and climbing.", 4.7, 500),
        Item(4, "John Hardy Women's Legends Naga Bracelet", 695.00, "jewelery",
            "From the Legends Collection, the Naga was inspired by the mythical water dragon that protects the ocean's pearl.", 4.6, 400),
        Item(5, "Solid Gold Petite Micropave", 168.00, "jewelery",
            "Satisfaction guaranteed. Return or exchange any order within 30 days.", 3.9, 70),
        Item(6, "WD 2TB Elements Portable External Hard Drive", 64.00, "electronics",
            "USB 3.0 and USB 2.0 compatibility, fast data transfers, improved PC performance and high capacity.", 3.3, 203),
        Item(7, "SanDisk SSD PLUS 1TB Internal SSD", 109.00, "electronics",
            "Easy upgrade for faster boot up, shutdown, application load and response.", 2.9, 470),
        Item(8, "Samsung 49-Inch CHG90 Curved Gaming Monitor", 999.99, "electronics",
            "49 inch super ultrawide 32:9 curved gaming monitor with dual 27 inch screen side by side.", 2.2, 140),
        Item(9, "BIYLACLESEN Women's 3-in-1 Snowboard Jacket", 56.99, "women's clothing",
            "Detachable liner fabric: warm fleece. Materials: 100% polyester. Detachable functional liner.", 2.6, 235),
        Item(10, "DANVOUY Womens T Shirt Casual Cotton Short", 12.99, "women's clothing",
            "95% cotton, 5% spandex. Features: casual, short sleeve, letter print, v-neck, fashion tees.", 3.6, 145),
    )

    val ids: Set<Int> = items.map { it.id }.toSet()

    fun listJson(): String = items.joinToString(prefix = "[", postfix = "]", separator = ",") { it.toJson() }

    fun productJson(id: Int): String? = items.firstOrNull { it.id == id }?.toJson()

    private fun Item.toJson(): String =
        """{"id":$id,"title":${quote(title)},"price":$price,"description":${quote(description)},""" +
            """"category":${quote(category)},"image":"https://picsum.photos/seed/fakestore$id/400",""" +
            """"rating":{"rate":$rate,"count":$count}}"""

    private fun quote(text: String) = "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
}
