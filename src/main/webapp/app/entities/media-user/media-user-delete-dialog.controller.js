(function() {
    'use strict';

    angular
        .module('medianocheApp')
        .controller('MediaUserDeleteController',MediaUserDeleteController);

    MediaUserDeleteController.$inject = ['$uibModalInstance', 'entity', 'MediaUser'];

    function MediaUserDeleteController($uibModalInstance, entity, MediaUser) {
        var vm = this;

        vm.mediaUser = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            MediaUser.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
