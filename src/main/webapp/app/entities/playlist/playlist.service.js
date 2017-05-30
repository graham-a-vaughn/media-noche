(function() {
    'use strict';
    angular
        .module('medianocheApp')
        .factory('Playlist', Playlist);

    Playlist.$inject = ['$resource', 'DateUtils'];

    function Playlist ($resource, DateUtils) {
        var resourceUrl =  'api/playlists/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.created = DateUtils.convertDateTimeFromServer(data.created);
                        data.lastModified = DateUtils.convertDateTimeFromServer(data.lastModified);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
