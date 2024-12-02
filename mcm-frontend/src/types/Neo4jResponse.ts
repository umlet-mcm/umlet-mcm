// todo check if this is the correct type
export type Neo4jResponse = {
    results: {
        columns: string[]
        data: {
            row: any[]
            meta: any[]
        }[]
    }[]
};

export const response =
    {
        "results": [ {
            "columns": [ "person", "test2" ],
            "data": [ {
                "row": [ {
                    "name": "Phil",
                    "age": 32,
                    "height": 1.80,
                    "birthday": "1983-12-25",
                    "isMarried": true,
                    "hasChildren": false,
                    "numberOfChildren": 0,
                    "hasPets": true,
                    "petNames": [ "Fluffy", "Spot" ],
                    "livesIn": {
                        "city": "Springfield",
                        "country": "USA"
                    },
                    "hasCar": {
                        "make": "Toyota",
                        "model": "Corolla",
                        "year": 2018
                    },
                    "hasHouse": {
                        "address": "742 Evergreen Terrace",
                        "city": "Springfield",
                        "country": "USA"
                    },
                    "hasParents": [ "Homer", "Marge" ],
                    "hasSiblings": [ "Lisa", "Maggie" ],
                    "hasFriends": [ "Carl", "Lenny" ],
                    "hasColleagues": [ "Barney", "Moe" ],
                    "hasAcquaintances": [ "Ned", "Maude" ],
                    "hasEnemies": [ "Sideshow Bob", "Mr. Burns" ],
                    "hasRivals": [ "Frank Grimes", "Hank Scorpio" ],
                    "hasIdols": [ "McBain", "Duffman" ],
                    "hasEnemiesOfTheState": [ "Kang", "Kodos" ],
                } ],
                "meta": [ {
                    "id": 11,
                    "elementId": "4:b7c0e943-1e73-474b-8ddc-e8ff3ae74cdd:11",
                    "type": "node",
                    "deleted": false
                } ]
            },
            {
                row: [ {
                    "test2": "test2"
                }],
                meta: [{}]
            }]
        } ],
        // other transactional data
    }