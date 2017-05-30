(function() {
    'use strict';

    angular
        .module('medianocheApp')
        .controller('AlbumDetailController', AlbumDetailController);

    AlbumDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Album', 'Song', 'Artist'];

    function AlbumDetailController($scope, $rootScope, $stateParams, previousState, entity, Album, Song, Artist) {
        var vm = this;

        vm.album = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('medianocheApp:albumUpdate', function(event, result) {
            vm.album = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
