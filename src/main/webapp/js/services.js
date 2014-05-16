'use strict';
var services = angular.module('trentinofamiglia.services', ['ngResource']);

services.factory('categoriesFactory', ['$rootScope', '$http',
    function($rootScope, $http) {
        $http.get('data/categories.json').success(function(cats) {
            $rootScope.categories = cats;
        });

        var getType = function(typeId) {
            for (var i = 0; i < $rootScope.categories.length; i++) {
                var cat = $rootScope.categories[i];
                for (var j = 0; j < cat.types.length; j++) {
                    var type = cat.types[j];
                    if (type.id === typeId) {
                        return type;
                    }
                }
            }
        };

        return {
            getType: getType
        };
    }
]);

services.factory('territoryFactory', ['$http', '$resource',
    function($http, $resource) {
        var baseUrl = 'https://tn.smartcampuslab.it';
        var authHeaders = {
            'Authorization': 'Bearer 1f7879b7-57f5-4a44-aa0a-c9987ab2d1aa'
        };

        var pois = $resource(baseUrl + '/trentinofamiglia/pois?filter={"limit":1000,"type":":typeId"}', {}, {
            'query': {
                method: 'GET',
                headers: authHeaders,
                isArray: true
            }
        });

        var events = $resource(baseUrl + '/trentinofamiglia/events?filter={"limit":1000,"type":":typeId","fromTime":":fromTime"}', {}, {
            'query': {
                method: 'GET',
                headers: authHeaders,
                isArray: true
            }
        });

        var tracks = $resource(baseUrl + '/trentinofamiglia/tracks?filter={"limit":1000,"type":":typeId"}', {}, {
            'query': {
                method: 'GET',
                headers: authHeaders,
                isArray: true
            }
        });

        return {
            getPois: pois.query,
            getEvents: events.query,
            getTracks: tracks.query
        };
    }
]);

services.factory('infowindowFactory', ['$compile',
    function($compile) {
        var initialized = false;
        var includables = ['iw_multiple', 'iw_default', 'iw_pois_vacanzealmare', 'iw_pois_familyaudit', 'iw_pois_familyintrentino',
            'iw_pois_littlehome', 'iw_events_altogarda', 'iw_tracks', 'iw_tracks_passeggiate'
        ];
        var compiled = {};

        var getPartial = function(marker, isArray) {
            var include = 'iw_default';
            if (isArray) {
                include = 'iw_multiple';
            } else if ( !! marker && !! marker.type && !! marker.obj && angular.equals(marker.type.objectType, 'pois')) {
                if (angular.equals(marker.obj.type, 'Vacanze al mare')) {
                    include = 'iw_pois_vacanzealmare';
                } else if (angular.equals(marker.obj.type, '\"Family Audit\"')) {
                    include = 'iw_pois_familyaudit';
                } else if (angular.equals(marker.obj.type, '\"Family in Trentino\"')) {
                    include = 'iw_pois_familyintrentino';
                } else if (angular.equals(marker.obj.type, '\"Baby little home\"')) {
                    include = 'iw_pois_littlehome';
                }
            } else if ( !! marker && !! marker.type && !! marker.obj && angular.equals(marker.type.objectType, 'events')) {
                if (angular.equals(marker.obj.type, 'Alto Garda')) {
                    include = 'iw_events_altogarda';
                }
            } else if ( !! marker && !! marker.type && !! marker.obj && angular.equals(marker.type.objectType, 'tracks')) {
                include = 'iw_tracks';
                if (angular.equals(marker.obj.type, 'Passeggiate')) {
                    include = 'iw_tracks_passeggiate';
                }
            }
            return include;
        };

        var getContent = function(scope, marker, isArray) {
            if (!initialized) {
                angular.forEach(includables, function(includable) {
                    var toBeCompiled = '<div class="row"><div class="col-md-12" ng-include="\'./partials/' + includable + '.html\'"></div></div>';
                    compiled[includable] = $compile(toBeCompiled)(scope);
                });
                initialized = true;
            }

            var include = getPartial(marker, isArray);
            return compiled[include][0];
        };

        return {
            getPartial: getPartial,
            getContent: getContent
        };
    }
]);

app.filter('htmlLinky', function($sanitize, linkyFilter) {
    var ELEMENT_NODE = 1;
    var TEXT_NODE = 3;
    var linkifiedDOM = document.createElement('div');
    var inputDOM = document.createElement('div');

    var linkify = function linkify(startNode) {
        var i, currentNode;

        for (i = 0; i < startNode.childNodes.length; i++) {
            currentNode = startNode.childNodes[i];

            switch (currentNode.nodeType) {
                case ELEMENT_NODE:
                    linkify(currentNode);
                    break;
                case TEXT_NODE:
                    linkifiedDOM.innerHTML = linkyFilter(currentNode.textContent, '_blank');
                    i += linkifiedDOM.childNodes.length - 1;
                    while (linkifiedDOM.childNodes.length) {
                        startNode.insertBefore(linkifiedDOM.childNodes[0], currentNode);
                    }
                    startNode.removeChild(currentNode);
            }
        }

        return startNode;
    };

    return function(input) {
        // strip <img/>
        // input = input.replace(/<img[^>]*>/g, '');

        // strip <img/> with relative path src
        if ( !! input) {
            input = input.replace(/<img.*src="(\/|\.)[^>]*>/g, '');
            input = input.replace('<a ', '<a target="_blank" ');
        }

        inputDOM.innerHTML = input;
        return linkify(inputDOM).innerHTML;
    };
});
