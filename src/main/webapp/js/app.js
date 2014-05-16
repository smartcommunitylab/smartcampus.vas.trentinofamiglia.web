'use strict';
var app = angular.module('trentinofamiglia', ['ngSanitize', 'ngAnimate', 'trentinofamiglia.directives', 'trentinofamiglia.services']);

app.config(['$httpProvider',
    function($httpProvider) {
        $httpProvider.defaults.useXDomain = true;
        delete $httpProvider.defaults.headers.common['X-Requested-With'];
    }
]);

// app.config(['$routeProvider', '$locationProvider',
//     function($routeProvider, $locationProvider) {
//         $locationProvider
//             .html5Mode(true);
//         $routeProvider.
//         when('/', {
//             controller: 'mainCtrl'
//         }).
//         otherwise({
//             redirectTo: '/'
//         });
//     }
// ]);
