'use strict';

describe('Controller Tests', function() {

    describe('Album Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockPreviousState, MockAlbum, MockSong, MockArtist;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockPreviousState = jasmine.createSpy('MockPreviousState');
            MockAlbum = jasmine.createSpy('MockAlbum');
            MockSong = jasmine.createSpy('MockSong');
            MockArtist = jasmine.createSpy('MockArtist');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity,
                'previousState': MockPreviousState,
                'Album': MockAlbum,
                'Song': MockSong,
                'Artist': MockArtist
            };
            createController = function() {
                $injector.get('$controller')("AlbumDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'medianocheApp:albumUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
