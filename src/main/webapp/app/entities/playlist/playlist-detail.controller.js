(function() {
    'use strict';

    angular
        .module('medianocheApp')
        .controller('PlaylistDetailController', PlaylistDetailController);

    PlaylistDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Playlist', 'Song'];

    function PlaylistDetailController($scope, $rootScope, $stateParams, previousState, entity, Playlist, Song) {
        var vm = this;

        vm.playlist = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('medianocheApp:playlistUpdate', function(event, result) {
            vm.playlist = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
