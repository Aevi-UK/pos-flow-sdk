( function( $ ) {
	'use strict';

	/**
	 * Hamburger icon toggle
	 */
	$( '.js-hamburger-icon' ).on( 'click', function() {
		$( '.js-hamburger-icon' ).toggleClass( 'is-active' );
	} );

	/**
	 * Offcanvas toggle
	 */
	$( '.js-offcanvas-toggle' ).on( 'click', function() {
		$( '.c-offcanvas' ).toggleClass( 'is-active' );
		$( '.js-logo' ).toggleClass( 'u-text-gray-300' );
	} );

	/**
	 * Post content manipulations
	 */
	$( document ).ready( function() {
		$( '.js-post' ).find( 'table' ).wrap( '<div class="o-responsive-table"></div>' );

		// augment all external links on the site to open in new tab
		var links = document.links;
		for (var i = 0, linksLength = links.length; i < linksLength; i++) {
			console.log("hi: " + links[i]);
			if (links[i].hostname != window.location.hostname) {
				links[i].target = '_blank';
				links[i].className += ' externalLink';
			}
		}
	} );
}( jQuery ) );
