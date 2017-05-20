static def safeGetValue(rawString) {
    if (rawString.equals("M")) 0
    else Double.parseDouble(rawString)
}

def slurper = new groovy.json.JsonSlurper()
def foods = slurper.parseFile(new File("foods.json"), "UTF-8").foods
def data = slurper.parseFile(new File("data.json"), "UTF-8")

def List<HashMap> nutrients = data.nutrients
def nutrientNames = nutrients.collect { it.name }

def interestingNutrients = ["Vitamin A", "Beta-karoten", "Vitamin E", "Tiamin", "Riboflavin", "Niacin", "Vitamin B6", "Folat", "Vitamin C", "Kalsium", "Jern", "Natrium", "Kalium", "Magnesium", "Sink", "Selen", "Kopper", "Fosfor", "Jod",
                            "C12:0",
                            "C14:0",
                            "C16:0",
                            "C18:0",
                            "Cis-enumettede fettsyrer",
                            "C16:1 sum",
                            "C18:1 sum",
                            "Cis-flerumettede fettsyrer",
                            "C18:2n-6",
                            "C18:3n-3",
                            "C20:3n-3",
                            "C20:3n-6",
                            "C20:4n-3",
                            "C20:4n-6"]
nutrientsDefinitions = nutrients.findAll { interestingNutrients.contains(it.name) }

def foodGroupNames = data.foodgroups.collect { it.name }

def interestingFoodGroups = [
        "Korn- og bakevarer, frø og nøtter",
        "Poteter, grønnsaker, frukt og bær",
        "Sukker og søte produkter",
        "Margarin, smør, matolje o.l.",
        "Drikke",
        "Spedbarnsmat",
        "Diverse retter, produkter og ingredienser",
        "Mel",
        "Gryn, ris, pasta",
        "Gryn, ris, pasta, råvare",
        "Gryn, ris, pasta, tilberedt",
        "Kornblanding, frokostkorn",
        "Brødvarer, hjemmebakt",
        "Brødvarer, industribakt",
        "Knekkebrød, smørbrødkjeks o.l.",
        "Småkaker, søt kjeks, kavring",
        "Søt gjærbakst, lefser, vafler o.l.",
        "Andre kaker o.l.",
        "Nøtter, mandler og frø",
        "Poteter",
        "Grønnsaker",
        "Grønnsaker, rå og fryst",
        "Grønnsaker, tilberedt",
        "Grønnsaksprodukter",
        "Belgfrukter",
        "Frukt og bær",
        "Frukt og bær, rå/fersk",
        "Produkter av frukt og bær",
        "Margarin og smør",
        "Matolje, frityrfett o.l.",
        "Vann, kaffe, te",
        "Juice, saft, brus o.l.",
        "Pizza, pai, taco o.l.",
        "Grøt",
        "Supper, saus og grytebaser",
        "Dessert, iskrem og tilbehør",
        "Snacks",
        "Vegetarprodukter og retter",
        "Diverse ingredienser",
        "Pulver, tørr vare"
]
interestingFoodGroupDefinitions = data.foodgroups.findAll { interestingFoodGroups.contains(it.name) }
interestingFoodGroupIds = interestingFoodGroupDefinitions.collect { it.id }
filteredFoods = foods.findAll {

    interestingFoodGroupIds.contains(it.groupId) &&
            safeGetValue(it["Kolest"].value) == 0 &&
            safeGetValue(it["Vit B12"].value) == 0 &&
            safeGetValue(it["Retinol"].value) == 0 &&
            safeGetValue(it["Trans"].value) == 0 &&
            safeGetValue(it["Vit D"].value) == 0 &&
            safeGetValue(it["C20:5n-3Eikosapentaensyre"].value) == 0 &&
            safeGetValue(it["C22:5n-3Dokosapentaensyre"].value) == 0 &&
            safeGetValue(it["C22:6n-3Dokosaheksaensyre"].value) == 0 &&
            !it["langualCodes"].contains("A0113") && // herbs or spices
            !it["langualCodes"].contains("A0857") && // herbs or spices
            !it["langualCodes"].contains("A0261") && // egg
            !it["langualCodes"].contains("A0791") && // egg
            !it["langualCodes"].contains("C0241") && // egg whites, albumin
            !it["langualCodes"].contains("A1114") && // honey
            !it["langualCodes"].contains("A1261") && // honey
            !it["langualCodes"].contains("C0188") && // honey
            !it["langualCodes"].contains("H0149") && // honey added
            !it["langualCodes"].contains("H0153") && // seafood added
            !it["langualCodes"].contains("H0184") && // milk added
            !it["langualCodes"].contains("H0143") && // cheese added
            !it["langualCodes"].contains("H0165") && // gelatin added
            !it["langualCodes"].contains("H0157") && // lactose added
            !it["langualCodes"].contains("H0185") && // egg yolk added
            !it["langualCodes"].contains("A0134") && // Salt or salt substitute (US CFR)
            !it["langualCodes"].contains("A0856") && // Seasoning or extract (EUROFIR)
            !it["langualCodes"].contains("A0181") && // Food additive (US CFR)
            !it["langualCodes"].contains("A0854") && // Baking ingredient (EUROFIR)
            !it["langualCodes"].contains("H0186") // egg added
}

// todo loop over data.lingual_codes, get all codes relating to lacto-ovo, honey etc

filteredFoodsNames = filteredFoods.collect { it.name }
println("Fant ${filteredFoods.size()} veganske matvarer på matvaretabellen.no\n")

topLists = [:]
nutrientsDefinitions.each { nutrient ->
    containsTheNutrient = filteredFoods.findAll { safeGetValue(it[nutrient.id].value) > 0 }
    topLists[nutrient.name] = containsTheNutrient.sort { -safeGetValue(it[nutrient.id].value) }
}

def pointScoringNutrientNames = ["Vitamin A", "Riboflavin", "Vitamin B6", "Kalsium", "Jern", "Sink", "Selen"]
def pointScoringNutrients = data.nutrients.findAll { pointScoringNutrientNames.contains(it.name) }

cutoff = 15
pointScoringNutrients.each { nutrient ->
    println("\nTopp $cutoff matvarer med ${nutrient.name}")
    println("**********************************")
    topLists[nutrient.name][0..(cutoff - 1)].each { food ->
        println(food.name)
    }
}

println("\nTopp 50 næringsrike veganske matvarer:")
println("**********************************")
foodScores = [:]
filteredFoods.each { food ->
    sum = 0
    pointScoringNutrients.each { nutrient ->
        position = topLists[nutrient.name].findIndexOf { it -> it.id.equals(food.id) }
        if (position >= 0)
            sum += position
        else sum += filteredFoods.size/2 // penalty for not having the nutrient
    }
    foodScores.put(food, sum)
}
topScoringFoods = foodScores.sort { it.value }.collect { it.key }

topScoringFoods[0..49].each {
    println("${it.name}")
}
