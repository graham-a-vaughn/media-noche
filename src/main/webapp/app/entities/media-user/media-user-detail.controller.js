(function() {
    'use strict';

    angular
        .module('medianocheApp')
        .controller('MediaUserDetailController', MediaUserDetailController);

    MediaUserDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'MediaUser', 'Playlist'];

    function MediaUserDetailController($scope, $rootScope, $stateParams, previousState, entity, MediaUser, Playlist) {
        var vm = this;

        vm.mediaUser = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('medianocheApp:mediaUserUpdate', function(event, result) {
            vm.mediaUser = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
