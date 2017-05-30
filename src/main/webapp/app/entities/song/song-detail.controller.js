(function() {
    'use strict';

    angular
        .module('medianocheApp')
        .controller('SongDetailController', SongDetailController);

    SongDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Song'];

    function SongDetailController($scope, $rootScope, $stateParams, previousState, entity, Song) {
        var vm = this;

        vm.song = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('medianocheApp:songUpdate', function(event, result) {
            vm.song = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
