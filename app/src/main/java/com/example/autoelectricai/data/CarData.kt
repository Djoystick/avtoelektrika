package com.example.autoelectricai.data

data class CarModel(
    val name: String,
    val startYear: Int,
    val endYear: Int? = null // null means it is still in production
)

object CarData {
    val cars: Map<String, List<CarModel>> = mapOf(
        "Audi" to listOf(
            CarModel("A3", 1996, null),
            CarModel("A4", 1994, null),
            CarModel("A5", 2007, null),
            CarModel("A6", 1994, null),
            CarModel("A7", 2010, null),
            CarModel("A8", 1994, null),
            CarModel("Q3", 2011, null),
            CarModel("Q5", 2008, null),
            CarModel("Q7", 2005, null),
            CarModel("Q8", 2018, null),
            CarModel("TT", 1998, 2023)
        ),
        "BMW" to listOf(
            CarModel("1 Series", 2004, null),
            CarModel("3 Series", 1975, null),
            CarModel("4 Series", 2013, null),
            CarModel("5 Series", 1972, null),
            CarModel("6 Series", 1976, 2018),
            CarModel("7 Series", 1977, null),
            CarModel("X1", 2009, null),
            CarModel("X3", 2003, null),
            CarModel("X4", 2014, null),
            CarModel("X5", 1999, null),
            CarModel("X6", 2008, null),
            CarModel("X7", 2018, null)
        ),
        "Chery" to listOf(
            CarModel("Arrizo 8", 2022, null),
            CarModel("Omoda C5", 2022, null),
            CarModel("Tiggo 4", 2017, null),
            CarModel("Tiggo 7 Pro", 2020, null),
            CarModel("Tiggo 8 Pro", 2021, null)
        ),
        "Chevrolet" to listOf(
            CarModel("Aveo", 2002, null),
            CarModel("Captiva", 2006, null),
            CarModel("Cobalt", 2004, null),
            CarModel("Cruze", 2008, null),
            CarModel("Epica", 2006, 2013),
            CarModel("Lacetti", 2002, 2013),
            CarModel("Lanos", 1997, 2017),
            CarModel("Niva", 2002, 2020),
            CarModel("Orlando", 2011, null),
            CarModel("Spark", 1998, null),
            CarModel("Tahoe", 1995, null),
            CarModel("TrailBlazer", 2001, null)
        ),
        "Citroen" to listOf(
            CarModel("Berlingo", 1996, null),
            CarModel("C3", 2002, null),
            CarModel("C4", 2004, null),
            CarModel("C5", 2001, null),
            CarModel("Jumper", 1994, null)
        ),
        "Ford" to listOf(
            CarModel("EcoSport", 2003, 2022),
            CarModel("Escape", 2000, null),
            CarModel("Explorer", 1990, null),
            CarModel("Fiesta", 1976, 2023),
            CarModel("Focus", 1998, null),
            CarModel("Fusion", 2002, 2020),
            CarModel("Kuga", 2008, null),
            CarModel("Mondeo", 1993, null),
            CarModel("Mustang", 1964, null),
            CarModel("Transit", 1965, null)
        ),
        "GAZ (ГАЗ)" to listOf(
            CarModel("Gazelle", 1994, null),
            CarModel("Gazelle NEXT", 2013, null),
            CarModel("Sobol", 1998, null),
            CarModel("Volga", 1956, 2010)
        ),
        "Geely" to listOf(
            CarModel("Atlas", 2016, null),
            CarModel("Coolray", 2018, null),
            CarModel("Emgrand", 2009, null),
            CarModel("Monjaro", 2022, null),
            CarModel("Tugella", 2019, null)
        ),
        "Haval" to listOf(
            CarModel("Dargo", 2020, null),
            CarModel("F7", 2018, null),
            CarModel("F7x", 2019, null),
            CarModel("H9", 2014, null),
            CarModel("Jolion", 2020, null)
        ),
        "Honda" to listOf(
            CarModel("Accord", 1976, null),
            CarModel("Civic", 1972, null),
            CarModel("CR-V", 1995, null),
            CarModel("Fit", 2001, null),
            CarModel("Freed", 2008, null),
            CarModel("HR-V", 1998, null),
            CarModel("Odyssey", 1994, null),
            CarModel("Pilot", 2002, null),
            CarModel("Stepwgn", 1996, null)
        ),
        "Hyundai" to listOf(
            CarModel("Accent", 1994, null),
            CarModel("Creta", 2014, null),
            CarModel("Elantra", 1990, null),
            CarModel("Getz", 2002, 2011),
            CarModel("H-1", 1997, 2021),
            CarModel("Santa Fe", 2000, null),
            CarModel("Solaris", 2010, 2022),
            CarModel("Sonata", 1985, null),
            CarModel("Starex", 1997, 2021),
            CarModel("Tucson", 2004, null)
        ),
        "Kia" to listOf(
            CarModel("Ceed", 2006, null),
            CarModel("Cerato", 2003, null),
            CarModel("K5", 2000, null),
            CarModel("Optima", 2000, 2020),
            CarModel("Picanto", 2004, null),
            CarModel("Rio", 1999, null),
            CarModel("Seltos", 2019, null),
            CarModel("Sorento", 2002, null),
            CarModel("Soul", 2008, null),
            CarModel("Sportage", 1993, null)
        ),
        "Lada (ВАЗ)" to listOf(
            CarModel("2107", 1982, 2012),
            CarModel("2110", 1995, 2007),
            CarModel("2114", 2001, 2013),
            CarModel("Granta", 2011, null),
            CarModel("Kalina", 2004, 2018),
            CarModel("Largus", 2012, null),
            CarModel("Niva Legend", 1977, null),
            CarModel("Niva Travel", 2020, null),
            CarModel("Priora", 2007, 2018),
            CarModel("Vesta", 2015, null),
            CarModel("XRAY", 2015, 2022)
        ),
        "Lexus" to listOf(
            CarModel("ES", 1989, null),
            CarModel("GS", 1991, 2020),
            CarModel("IS", 1998, null),
            CarModel("LS", 1989, null),
            CarModel("LX", 1995, null),
            CarModel("NX", 2014, null),
            CarModel("RX", 1997, null)
        ),
        "Mazda" to listOf(
            CarModel("3", 2003, null),
            CarModel("6", 2002, null),
            CarModel("CX-5", 2012, null),
            CarModel("CX-7", 2006, 2012),
            CarModel("CX-9", 2006, 2023),
            CarModel("Demio", 1996, 2019),
            CarModel("Familia", 1963, 2003)
        ),
        "Mercedes-Benz" to listOf(
            CarModel("A-Class", 1997, null),
            CarModel("C-Class", 1993, null),
            CarModel("E-Class", 1993, null),
            CarModel("G-Class", 1979, null),
            CarModel("GLA", 2013, null),
            CarModel("GLC", 2015, null),
            CarModel("GLE", 2015, null),
            CarModel("GLS", 2015, null),
            CarModel("S-Class", 1972, null),
            CarModel("Sprinter", 1995, null),
            CarModel("V-Class", 1996, null)
        ),
        "Mitsubishi" to listOf(
            CarModel("ASX", 2010, null),
            CarModel("Colt", 1962, 2012),
            CarModel("Delica", 1968, null),
            CarModel("Galant", 1969, 2012),
            CarModel("L200", 1978, null),
            CarModel("Lancer", 1973, 2017),
            CarModel("Outlander", 2001, null),
            CarModel("Pajero", 1982, 2021),
            CarModel("Pajero Sport", 1996, null)
        ),
        "Nissan" to listOf(
            CarModel("Almera", 1995, 2018),
            CarModel("Juke", 2010, null),
            CarModel("Leaf", 2010, null),
            CarModel("Murano", 2002, null),
            CarModel("Note", 2004, null),
            CarModel("Pathfinder", 1985, null),
            CarModel("Patrol", 1951, null),
            CarModel("Qashqai", 2006, null),
            CarModel("Teana", 2003, 2020),
            CarModel("Terrano", 1986, 2022),
            CarModel("Tiida", 2004, 2015),
            CarModel("X-Trail", 2000, null)
        ),
        "Opel" to listOf(
            CarModel("Astra", 1991, null),
            CarModel("Corsa", 1982, null),
            CarModel("Insignia", 2008, 2022),
            CarModel("Mokka", 2012, null),
            CarModel("Vectra", 1988, 2008),
            CarModel("Zafira", 1999, null)
        ),
        "Peugeot" to listOf(
            CarModel("206", 1998, 2012),
            CarModel("307", 2001, 2008),
            CarModel("308", 2007, null),
            CarModel("3008", 2008, null),
            CarModel("408", 2010, null),
            CarModel("Boxer", 1994, null),
            CarModel("Partner", 1996, null)
        ),
        "Renault" to listOf(
            CarModel("Arkana", 2019, null),
            CarModel("Duster", 2010, null),
            CarModel("Fluence", 2009, 2019),
            CarModel("Kaptur", 2016, null),
            CarModel("Logan", 2004, null),
            CarModel("Megane", 1995, null),
            CarModel("Sandero", 2007, null),
            CarModel("Symbol", 1999, 2021)
        ),
        "Skoda" to listOf(
            CarModel("Fabia", 1999, null),
            CarModel("Karoq", 2017, null),
            CarModel("Kodiaq", 2016, null),
            CarModel("Octavia", 1996, null),
            CarModel("Rapid", 2012, null),
            CarModel("Roomster", 2006, 2015),
            CarModel("Superb", 2001, null),
            CarModel("Yeti", 2009, 2017)
        ),
        "Subaru" to listOf(
            CarModel("Forester", 1997, null),
            CarModel("Impreza", 1992, null),
            CarModel("Legacy", 1989, null),
            CarModel("Outback", 1994, null),
            CarModel("XV", 2012, 2023)
        ),
        "Suzuki" to listOf(
            CarModel("Grand Vitara", 1998, 2015),
            CarModel("Jimny", 1970, null),
            CarModel("Swift", 1983, null),
            CarModel("SX4", 2006, null),
            CarModel("Vitara", 1988, null)
        ),
        "Toyota" to listOf(
            CarModel("Alphard", 2002, null),
            CarModel("Camry", 1982, null),
            CarModel("Celica", 1970, 2006),
            CarModel("Corolla", 1966, null),
            CarModel("Crown", 1955, null),
            CarModel("Harrier", 1997, null),
            CarModel("Highlander", 2000, null),
            CarModel("Hilux", 1968, null),
            CarModel("Land Cruiser", 1951, null),
            CarModel("Land Cruiser Prado", 1990, null),
            CarModel("Mark II", 1968, 2004),
            CarModel("Prius", 1997, null),
            CarModel("RAV4", 1994, null),
            CarModel("Vitz", 1999, 2019),
            CarModel("Yaris", 1999, null)
        ),
        "UAZ (УАЗ)" to listOf(
            CarModel("Buhanka", 1965, null),
            CarModel("Hunter", 2003, null),
            CarModel("Patriot", 2005, null),
            CarModel("Pickup", 2008, null),
            CarModel("Profi", 2017, null)
        ),
        "Volkswagen" to listOf(
            CarModel("Amarok", 2010, null),
            CarModel("Caddy", 1980, null),
            CarModel("Golf", 1974, null),
            CarModel("Jetta", 1979, null),
            CarModel("Multivan", 2003, null),
            CarModel("Passat", 1973, null),
            CarModel("Polo", 1975, null),
            CarModel("Tiguan", 2007, null),
            CarModel("Touareg", 2002, null),
            CarModel("Transporter", 1950, null)
        ),
        "Volvo" to listOf(
            CarModel("S40", 1995, 2012),
            CarModel("S60", 2000, null),
            CarModel("S80", 1998, 2016),
            CarModel("XC40", 2017, null),
            CarModel("XC60", 2008, null),
            CarModel("XC90", 2002, null)
        )
    )

    val brands = cars.keys.toList().sorted()

    fun getModels(brand: String): List<String> {
        return cars[brand]?.map { it.name }?.sorted() ?: emptyList()
    }
    
    fun getYearsForModel(brand: String, model: String): List<String> {
        val carModel = cars[brand]?.find { it.name == model }
        if (carModel != null) {
            val endYear = carModel.endYear ?: 2026 // Current actual year
            return (carModel.startYear..endYear).map { it.toString() }.reversed()
        }
        return (1990..2026).map { it.toString() }.reversed()
    }

    // Default fallback
    val defaultYears = (1990..2026).map { it.toString() }.reversed()
}
