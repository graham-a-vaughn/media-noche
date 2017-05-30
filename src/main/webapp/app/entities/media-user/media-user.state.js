(function() {
    'use strict';

    angular
        .module('medianocheApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('media-user', {
            parent: 'entity',
            url: '/media-user',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'MediaUsers'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/media-user/media-users.html',
                    controller: 'MediaUserController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
            }
        })
        .state('media-user-detail', {
            parent: 'media-user',
            url: '/media-user/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'MediaUser'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/media-user/media-user-detail.html',
                    controller: 'MediaUserDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'MediaUser', function($stateParams, MediaUser) {
                    return MediaUser.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'media-user',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('media-user-detail.edit', {
            parent: 'media-user-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/media-user/media-user-dialog.html',
                    controller: 'MediaUserDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['MediaUser', function(MediaUser) {
                            return MediaUser.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('media-user.new', {
            parent: 'media-user',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/media-user/media-user-dialog.html',
                    controller: 'MediaUserDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                username: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('media-user', null, { reload: 'media-user' });
                }, function() {
                    $state.go('media-user');
                });
            }]
        })
        .state('media-user.edit', {
            parent: 'media-user',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/media-user/media-user-dialog.html',
                    controller: 'MediaUserDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['MediaUser', function(MediaUser) {
                            return MediaUser.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('media-user', null, { reload: 'media-user' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('media-user.delete', {
            parent: 'media-user',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/media-user/media-user-delete-dialog.html',
                    controller: 'MediaUserDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['MediaUser', function(MediaUser) {
                            return MediaUser.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('media-user', null, { reload: 'media-user' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
