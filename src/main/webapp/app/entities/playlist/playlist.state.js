(function() {
    'use strict';

    angular
        .module('medianocheApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('playlist', {
            parent: 'entity',
            url: '/playlist',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'Playlists'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/playlist/playlists.html',
                    controller: 'PlaylistController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
            }
        })
        .state('playlist-detail', {
            parent: 'playlist',
            url: '/playlist/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'Playlist'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/playlist/playlist-detail.html',
                    controller: 'PlaylistDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                entity: ['$stateParams', 'Playlist', function($stateParams, Playlist) {
                    return Playlist.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'playlist',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('playlist-detail.edit', {
            parent: 'playlist-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/playlist/playlist-dialog.html',
                    controller: 'PlaylistDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Playlist', function(Playlist) {
                            return Playlist.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('playlist.new', {
            parent: 'playlist',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/playlist/playlist-dialog.html',
                    controller: 'PlaylistDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                name: null,
                                created: null,
                                lastModified: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('playlist', null, { reload: 'playlist' });
                }, function() {
                    $state.go('playlist');
                });
            }]
        })
        .state('playlist.edit', {
            parent: 'playlist',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/playlist/playlist-dialog.html',
                    controller: 'PlaylistDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Playlist', function(Playlist) {
                            return Playlist.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('playlist', null, { reload: 'playlist' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('playlist.delete', {
            parent: 'playlist',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/playlist/playlist-delete-dialog.html',
                    controller: 'PlaylistDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Playlist', function(Playlist) {
                            return Playlist.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('playlist', null, { reload: 'playlist' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
