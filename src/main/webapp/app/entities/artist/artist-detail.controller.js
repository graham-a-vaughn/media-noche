(function() {
    'use strict';

    angular
        .module('medianocheApp')
        .controller('ArtistDetailController', ArtistDetailController);

    ArtistDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Artist', 'Album'];

    function ArtistDetailController($scope, $rootScope, $stateParams, previousState, entity, Artist, Album) {
        var vm = this;

        vm.artist = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('medianocheApp:artistUpdate', function(event, result) {
            vm.artist = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
