'use strict';
app.controller('mainController', ['$rootScope', '$scope',
    function ($rootScope, $scope) {
    $rootScope.objects = {};
    $scope.autoZoom = true;

    $scope.loading = false;

    $scope.toggleAutoZoom = function () {
      $scope.autoZoom = !$scope.autoZoom;
    };

    toastr.options = {
      // 'toastClass': 'alert-info',
      // 'iconClasses': {
      //     'error': 'alert-error',
      //     'info': 'alert-info',
      //     'success': 'alert-success',
      //     'warning': 'alert-warning'
      // },
      'closeButton': false,
      'debug': false,
      'positionClass': 'toast-top-right',
      'onclick': null,
      'showDuration': '300',
      'hideDuration': '1000',
      'timeOut': '5000',
      'extendedTimeOut': '1000',
      'showEasing': 'swing',
      'hideEasing': 'linear',
      'showMethod': 'fadeIn',
      'hideMethod': 'fadeOut'
    }
    }
]);

app.controller('mapController', ['$rootScope', '$scope', '$filter', 'categoriesFactory', 'infowindowFactory',
    function ($rootScope, $scope, $filter, categoriesFactory, infowindowFactory) {
    var zoomMax = 21;
    var trentoLatLng = new google.maps.LatLng(46.0696727540531, 11.1212700605392);
    var mapOptions = {
      streetViewControl: false,
      center: trentoLatLng,
      zoom: 10
    };
    var map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);
    var markerClusterer = new MarkerClusterer(map, [], {
      zoomOnClick: false
    });
    var drawables = {};
    var infowindow = new google.maps.InfoWindow({
      content: infowindowFactory.getContent($scope)
    });

    $scope.getPartial = infowindowFactory.getPartial;

    $scope.activeMarker = null;
    $scope.activeMultipleMarkers = [];
    $scope.setActiveMarker = function (marker) {
      $scope.activeMarker = marker;
    };

    $scope.activePolyline = null;
    $scope.togglePolyline = function () {
      if (!!$scope.activePolyline) {
        if (!!$scope.activePolyline.getMap()) {
          $scope.activePolyline.setMap(null);
        } else {
          $scope.activePolyline.setMap(map);
        }
      }
    };

    $scope.showPolyline = function () {
      if (!!$scope.activePolyline.setMap()) {
        return true;
      }
      return false;
    };

    $scope.$watch('objects', function (objs, prevObjs) {
      angular.forEach(objs, function (typeObjs, typeId) {
        // checking for changed types
        if (!angular.equals(typeObjs, prevObjs[typeId])) {
          // typeId changed!
          if (!!drawables[typeId]) {
            // remove drawables
            angular.forEach(drawables[typeId], function (drawable) {
              drawable.setMap(null);
            });
            drawables[typeId].length = 0;
          }

          var type = categoriesFactory.getType(typeId);

          var getMarkerByType = function () {
            if (!!categoriesFactory.getType(typeId).marker) {
              return categoriesFactory.getType(typeId).marker;
            }
            return {
              path: google.maps.SymbolPath.CIRCLE,
              scale: 5,
              fillColor: type.color,
              fillOpacity: 1,
              strokeWeight: 1
            };
          };

          // create new drawable
          angular.forEach(typeObjs, function (obj) {
            var newDrawables = [];
            var marker;
            var position;
            var polyline;


            if (type.objectType === 'tracks') {
              var points = google.maps.geometry.encoding.decodePath(obj.track);
              polyline = new google.maps.Polyline({
                // map: map,
                path: points,
                geodesic: true,
                strokeColor: type.color,
                strokeOpacity: 1.0,
                strokeWeight: 5
              });
              newDrawables.push(polyline);

              position = new google.maps.LatLng(points[0].lat(), points[0].lng());
            } else {
              position = new google.maps.LatLng(obj.location[0], obj.location[1]);
            }

            // WORKAROUND: avoid points on (0,0)
            if (position.lat() !== 0 && position.lng() !== 0) {
              marker = new google.maps.Marker({
                icon: getMarkerByType(),
                // animation: google.maps.Animation.DROP,
                position: position,
                title: obj.title,
                map: map
              });
              marker.type = type;
              marker.obj = obj;
              newDrawables.push(marker);

              // infowindow
              google.maps.event.addListener(marker, 'click', function () {
                $scope.setActiveMarker(marker);
                $scope.activePolyline = null;
                if (!!polyline) {
                  $scope.activePolyline = polyline;
                }

                infowindow.setContent(infowindowFactory.getContent($scope, marker));
                $scope.$apply();
                infowindow.open(map, marker);
              });

              if (!drawables[typeId]) {
                drawables[typeId] = [];
              }

              if (!!newDrawables) {
                for (var i = 0; i < newDrawables.length; i++) {
                  drawables[typeId].push(newDrawables[i]);
                }
              }
            }
          });
        }
      });

      // clustering
      markerClusterer.clearMarkers();
      angular.forEach(drawables, function (drawablesByType) {
        for (var i = 0; i < drawablesByType.length; i++) {
          var d = drawablesByType[i];
          if (typeof d.getPosition === 'function') {
            markerClusterer.addMarker(d);
          }
        }
      });

      // fit map to markers
      if ($scope.autoZoom && markerClusterer.getTotalMarkers() > 0) {
        markerClusterer.fitMapToMarkers();
      }

      // force repaint
      markerClusterer.repaint();
    }, true);

    // infowindow on cluster
    google.maps.event.addListener(markerClusterer, 'click', function (cluster) {
      if (map.getZoom() < zoomMax) {
        var bounds = cluster.getBounds();
        map.fitBounds(bounds);
      } else if (map.getZoom() === zoomMax) {
        $scope.activeMultipleMarkers = cluster.getMarkers();
        $scope.setActiveMarker($scope.activeMultipleMarkers[0]);
        infowindow.setContent(infowindowFactory.getContent($scope, null, true));
        $scope.$apply();
        infowindow.open(map, $scope.activeMultipleMarkers[0]);
      }
    });
    }
]);

app.controller('categoriesController', ['$rootScope', '$scope', 'territoryFactory',
    function ($rootScope, $scope, territoryFactory) {
    $scope.getObjects = function (type) {
      var getter;
      var params = {
        typeId: type.id.replace(/\"/g, '\\"')
      };

      if (type.objectType === 'events') {
        getter = territoryFactory.getEvents;
        var fromTime = new Date();
        fromTime.setHours(0);
        fromTime.setMinutes(0);
        fromTime.setSeconds(0);
        params.fromTime = fromTime.getTime();
      } else if (type.objectType === 'pois') {
        getter = territoryFactory.getPois;
      } else if (type.objectType === 'tracks') {
        getter = territoryFactory.getTracks;
      }

      if (!!getter) {
        $scope.loading = true;
        getter(params, function (objects) {
          $rootScope.objects[type.id] = objects;
          $scope.loading = false;
        });
      }
    };

    $scope.isActiveType = function (typeId) {
      return !!$rootScope.objects[typeId];
    };

    $scope.toggleActiveType = function (type) {
      if ($scope.isActiveType(type.id)) {
        // uncheck
        $rootScope.objects[type.id] = null;
      } else {
        // check
        $scope.getObjects(type);
        if (!!type.alert) {
          toastr['info'](type.alert, type.id);
        }
      }
    };

    $scope.toggleActiveType({
      "objectType": "events",
      "id": "Estate giovani e famiglia",
      "color": "red",
      "icon": "imgs/icons/ic_summer.png",
      "marker": "imgs/markers/poi_estate.png"
    });
    }
]);