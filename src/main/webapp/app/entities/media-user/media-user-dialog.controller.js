(function() {
    'use strict';

    angular
        .module('medianocheApp')
        .controller('MediaUserDialogController', MediaUserDialogController);

    MediaUserDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'MediaUser', 'Playlist'];

    function MediaUserDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, MediaUser, Playlist) {
        var vm = this;

        vm.mediaUser = entity;
        vm.clear = clear;
        vm.save = save;
        vm.playlists = Playlist.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.mediaUser.id !== null) {
                MediaUser.update(vm.mediaUser, onSaveSuccess, onSaveError);
            } else {
                MediaUser.save(vm.mediaUser, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('medianocheApp:mediaUserUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
