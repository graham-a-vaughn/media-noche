(function() {
    'use strict';
    angular
        .module('medianocheApp')
        .factory('MediaUser', MediaUser);

    MediaUser.$inject = ['$resource'];

    function MediaUser ($resource) {
        var resourceUrl =  'api/media-users/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
