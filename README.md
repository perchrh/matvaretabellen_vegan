Looks through nutrition data for food items found at matvaretabellen.no

Finds vegan food items especially high in selected nutrients. 

Downloaded data by:

    curl 'http://www.matvaretabellen.no/api/foods?language=no&version=72' --compressed -o foods.json
    curl 'http://www.matvaretabellen.no/api/data?language=no&version=72' --compressed -o data.json


